package pl.kj.bachelors.identity.application.dto.request;

import io.swagger.annotations.ApiModel;
import pl.kj.bachelors.identity.domain.constraint.FieldValueMatch;
import pl.kj.bachelors.identity.domain.constraint.Password;

@FieldValueMatch(field = "confirmPassword", target = "password", message = "ID.004")
@ApiModel
public class PasswordResetRequest {
    private String token;
    private String pin;
    @Password(message = "ID.003")
    private String password;
    private String confirmPassword;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
