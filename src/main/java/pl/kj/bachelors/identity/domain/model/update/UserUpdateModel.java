package pl.kj.bachelors.identity.domain.model.update;

import org.hibernate.validator.constraints.Length;

public class UserUpdateModel {
    @Length(min = 4, max = 120, message = "ID.002")
    private String username;
    @Length(min = 2, max = 120, message = "ID.002")
    private String firstName;
    @Length(min = 2, max = 120, message = "ID.002")
    private String lastName;
    private Integer pictureId;

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

    public Integer getPictureId() {
        return pictureId;
    }

    public void setPictureId(Integer pictureId) {
        this.pictureId = pictureId;
    }
}
