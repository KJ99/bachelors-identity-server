package pl.kj.bachelors.identity.application.dto.request;

public class PasswordResetInitRequest {
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
