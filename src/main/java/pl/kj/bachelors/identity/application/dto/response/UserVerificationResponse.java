package pl.kj.bachelors.identity.application.dto.response;

public class UserVerificationResponse {
    private String verificationToken;

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }
}
