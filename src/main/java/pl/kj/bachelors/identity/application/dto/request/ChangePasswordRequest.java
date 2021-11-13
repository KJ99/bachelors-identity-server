package pl.kj.bachelors.identity.application.dto.request;

import io.swagger.annotations.ApiModel;
import pl.kj.bachelors.identity.domain.constraint.FieldValueMatch;
import pl.kj.bachelors.identity.domain.constraint.Password;

@ApiModel
@FieldValueMatch(field = "confirmPassword", target = "newPassword", message = "ID.004")
public class ChangePasswordRequest {
    private String currentPassword;
    @Password(message = "ID.003")
    private String newPassword;
    private String confirmPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
