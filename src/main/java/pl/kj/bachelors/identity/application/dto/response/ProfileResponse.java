package pl.kj.bachelors.identity.application.dto.response;

public class ProfileResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String pictureUrl;
    private UserSettingsResponse settings;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
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

    public UserSettingsResponse getSettings() {
        return settings;
    }

    public void setSettings(UserSettingsResponse settings) {
        this.settings = settings;
    }
}
