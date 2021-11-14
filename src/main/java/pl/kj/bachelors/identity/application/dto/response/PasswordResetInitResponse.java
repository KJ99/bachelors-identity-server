package pl.kj.bachelors.identity.application.dto.response;

public class PasswordResetInitResponse {
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
