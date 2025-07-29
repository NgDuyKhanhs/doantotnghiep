package lms.doantotnghiep.jwt;


import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.*;
import lms.doantotnghiep.enums.AppException;
import lms.doantotnghiep.enums.ErrorConstant;
import lms.doantotnghiep.enums.TokenType;
import lms.doantotnghiep.repository.RoleRepository;
import lms.doantotnghiep.repository.UserRepository;
import lms.doantotnghiep.service.impl.UserDetailsImple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    @Value("${jwt.valid-duration}")
    long validDuration;

    @Value("${jwt.refreshable-duration}")
    long refreshableDuration;

    @Value("${jwt.reset-password-duration}")
    long resetPasswordDuration;

    @Value("${jwt.active-user-duration}")
    long activeUserDuration;

    @Value("${jwt.accessKey}")
    String accessKey;

    @Value("${jwt.refreshKey}")
    String refreshKey;
    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;
    final RoleRepository roleRepository;

    public String generateJwtToken(UserDetailsImple userDetailsImple, TokenType tokenType) {
        Date now = new Date();
        Date dateExpired = new Date(now.getTime() + getDuration(tokenType) * 1000L);
        Claims claims = Jwts.claims().setSubject(Integer.toString(userDetailsImple.getId()));
        claims.put("roles", userDetailsImple.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.toList()));
        return Jwts.builder()
                .setClaims(claims)
                .setSubject((userDetailsImple.getEmail()))
                .setIssuedAt(now)
                .setExpiration(dateExpired)
                .signWith(SignatureAlgorithm.HS512, getKey(tokenType))
                .compact();
    }

    private long getDuration(TokenType tokenType) {
        long validTime = 0;
        switch (tokenType) {
            case ACCESS_TOKEN -> {
                validTime = validDuration;
            }
            case REFRESH_TOKEN -> {
                validTime = refreshableDuration;
            }
        }
        return validTime;

    }

    public String getUserNameFromJwtToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(getKey(TokenType.ACCESS_TOKEN))
                .parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public boolean validateJwtToken(String authToken, TokenType typeToken) {
        try {
            Jwts.parser().setAllowedClockSkewSeconds(60).setSigningKey(getKey(typeToken)).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }

    public SignedJWT verifyToken(String token, TokenType tokenType) throws JOSEException, ParseException {
        String key = getKey(tokenType);
        JWSVerifier verifier = new MACVerifier(key.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();

        if (claims.getSubject() != null && !expiryTime.after(new Date())) {
            throw new AppException(ErrorConstant.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    public String extractEmail(String token, TokenType type) {
        SignedJWT signedJWT = null;
        try {
            signedJWT = verifyToken(token, type);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        String email = null;
        try {
            email = signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return email;
    }

    private String getKey(TokenType tokenType) {
        String key = "";
        switch (tokenType) {
            case ACCESS_TOKEN -> {
                key = accessKey;
            }
            case REFRESH_TOKEN -> {
                key = refreshKey;
            }
        }
        return key;

    }


}
