package pl.kj.bachelors.identity.domain.model.embeddable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class UserSettings {
    @Column(name = "settings_night_mode")
    private boolean nightMode = false;

    public boolean isNightMode() {
        return nightMode;
    }

    public void setNightMode(boolean nightMode) {
        this.nightMode = nightMode;
    }
}
