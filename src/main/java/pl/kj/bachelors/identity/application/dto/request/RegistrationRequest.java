package pl.kj.bachelors.identity.application.dto.request;
import org.hibernate.validator.constraints.Length;
import pl.kj.bachelors.identity.domain.constraint.FieldValueMatch;
import pl.kj.bachelors.identity.domain.constraint.Password;

import javax.validation.constraints.Email;

@FieldValueMatch(field = "confirmPassword", target = "password", message = "ID.004")
public class RegistrationRequest {
    @Email(message = "ID.001")
    private String email;
    @Length(min = 4, max = 120, message = "ID.002")
    private String username;
    @Length(min = 2, max = 120, message = "ID.002")
    private String firstName;
    @Length(min = 2, max = 120, message = "ID.002")
    private String lastName;
    @Password(message = "ID.003")
    private String password;

    private String confirmPassword;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

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
}
