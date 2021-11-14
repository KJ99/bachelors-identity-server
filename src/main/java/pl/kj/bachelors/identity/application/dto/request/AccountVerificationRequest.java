package pl.kj.bachelors.identity.application.dto.request;

public class AccountVerificationRequest {
    private String token;
    private String pin;

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
