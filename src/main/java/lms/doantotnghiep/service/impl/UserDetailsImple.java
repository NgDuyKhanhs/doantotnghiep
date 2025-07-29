package lms.doantotnghiep.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lms.doantotnghiep.domain.Class;
import lms.doantotnghiep.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.io.Serial;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsImple implements UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;
    private int id;
    @JsonIgnore
    private String password;
    private String username;
    private String fullname;
    private String className;
    private Integer classId;
    private String avatar;
    private LocalDate birth;
    private String email;
    private boolean active;
    private Collection<? extends GrantedAuthority> authorities;
    public static UserDetailsImple build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName().name()))
                .collect(Collectors.toList());
        return UserDetailsImple.builder()
                .id(user.getId())
                .fullname(user.getFullname())
                .password(user.getPassword())
                .avatar(user.getAvatar())
                .birth(user.getBirth())
                .email(user.getEmail())
                .authorities(authorities)
                .active(user.isActive())
                .className(user.getClassId() != null ? user.getClassId().getName() : null)
                .classId(user.getClassId() != null ? user.getClassId().getId() : null)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImple user = (UserDetailsImple) o;
        return Objects.equals(id, user.id);
    }


}
