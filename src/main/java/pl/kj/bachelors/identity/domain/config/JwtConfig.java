package pl.kj.bachelors.identity.domain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret;
    private String algorithm;
    private long validTimeInMinutes;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public long getValidTimeInMinutes() {
        return validTimeInMinutes;
    }

    public void setValidTimeInMinutes(long validTimeInMinutes) {
        this.validTimeInMinutes = validTimeInMinutes;
    }
}
