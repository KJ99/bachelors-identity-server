package pl.kj.bachelors.identity.application.dto.response;

import io.swagger.annotations.ApiModel;

@ApiModel
public class LoginResponse {
    private ProfileResponse user;
    private TokenResponse token;

    public ProfileResponse getUser() {
        return user;
    }

    public void setUser(ProfileResponse user) {
        this.user = user;
    }

    public TokenResponse getToken() {
        return token;
    }

    public void setToken(TokenResponse token) {
        this.token = token;
    }
}
