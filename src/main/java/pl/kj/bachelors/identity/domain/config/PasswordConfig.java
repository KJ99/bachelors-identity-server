package pl.kj.bachelors.identity.domain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "password")
public class PasswordConfig {
    private int minLength;
    private int maxLength;
    private int saltRounds;

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getSaltRounds() {
        return saltRounds;
    }

    public void setSaltRounds(int saltRounds) {
        this.saltRounds = saltRounds;
    }
}
