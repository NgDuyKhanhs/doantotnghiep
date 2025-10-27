package lms.doantotnghiep.configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary((Map) customCloudinaryConfig());
    }

    @Bean
    public Object customCloudinaryConfig() {
        return ObjectUtils.asMap(
                "cloud_name", "dc6nfzud7",
                "api_key", "519537396121699",
                "api_secret", "ZyriexTkDzgscaJd8fjCAm-NpKY",
                "secure", true
        );
    }

}
