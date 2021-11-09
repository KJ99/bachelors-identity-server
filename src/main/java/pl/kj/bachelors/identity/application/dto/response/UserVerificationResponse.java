package pl.kj.bachelors.identity.application.dto.response;

import io.swagger.annotations.ApiModel;

@ApiModel
public class UserVerificationResponse {
    private String verificationToken;

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }
}
