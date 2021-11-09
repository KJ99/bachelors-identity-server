package pl.kj.bachelors.identity.application.dto.request;

import io.swagger.annotations.ApiModel;

@ApiModel
public class VerificationResendRequest {
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
