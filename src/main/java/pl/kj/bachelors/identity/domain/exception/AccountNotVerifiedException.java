package pl.kj.bachelors.identity.domain.exception;

import pl.kj.bachelors.identity.domain.model.entity.UserVerification;

public class AccountNotVerifiedException extends Throwable {
    private String code = "ID.031";
    protected String message = "Account is not verified. Please verify your account in order to sign in";
    private UserVerification latestVerification;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public UserVerification getLatestVerification() {
        return latestVerification;
    }

    public void setLatestVerification(UserVerification latestVerification) {
        this.latestVerification = latestVerification;
    }
}
