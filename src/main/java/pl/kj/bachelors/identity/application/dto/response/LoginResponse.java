package pl.kj.bachelors.identity.application.dto.response;

import io.swagger.annotations.ApiModel;

@ApiModel
public class LoginResponse {
    private ProfileResponse profile;

    public ProfileResponse getProfile() {
        return profile;
    }

    public void setProfile(ProfileResponse profile) {
        this.profile = profile;
    }
}
