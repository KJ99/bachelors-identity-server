package pl.kj.bachelors.identity.application.dto.response;

public class UserSettingsResponse {
    private boolean nightMode;

    public boolean isNightMode() {
        return nightMode;
    }

    public void setNightMode(boolean nightMode) {
        this.nightMode = nightMode;
    }
}
