package pl.kj.bachelors.identity.domain.model.update;

public class UserSettingsUpdateModel {
    private boolean nightMode;

    public boolean isNightMode() {
        return nightMode;
    }

    public void setNightMode(boolean nightMode) {
        this.nightMode = nightMode;
    }
}
