package pl.kj.bachelors.identity.application.dto.response;

import io.swagger.annotations.ApiModel;

@ApiModel
public class PasswordResetInitResponse {
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
