package pl.kj.bachelors.identity.domain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt.cookie")
public class JwtCookieConfig {
    private String name;
    private int validTimeInMinutes;
    private String domain;

    public int getValidTimeInMinutes() {
        return validTimeInMinutes;
    }

    public void setValidTimeInMinutes(int validTimeInMinutes) {
        this.validTimeInMinutes = validTimeInMinutes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
