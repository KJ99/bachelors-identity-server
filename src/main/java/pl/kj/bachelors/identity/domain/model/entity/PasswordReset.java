package pl.kj.bachelors.identity.domain.model.entity;

import pl.kj.bachelors.identity.domain.model.embeddable.Audit;

import javax.persistence.*;
import java.util.Calendar;

@Table(name = "password_resets")
@Entity
public class PasswordReset {
    @Id
    private String token;
    private String pin;
    @Column(name = "expires_at")
    private Calendar expiresAt;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "uid")
    private User user;
    @Embedded
    private Audit audit;

    public PasswordReset() {
        this.audit = new Audit();
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

    public Calendar getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Calendar expiresAt) {
        this.expiresAt = expiresAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }
}
