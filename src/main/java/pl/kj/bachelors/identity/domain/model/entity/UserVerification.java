package pl.kj.bachelors.identity.domain.model.entity;

import pl.kj.bachelors.identity.domain.model.embeddable.Audit;

import javax.persistence.*;
import java.util.Calendar;

@Entity
@Table(name = "user_verifications")
public class UserVerification {
    @Id
    private String token;
    private String pin;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "uid")
    private User user;
    @Column(name = "expires_at")
    private Calendar expiresAt;
    @Embedded
    private Audit audit;

    public UserVerification() {
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

    public Calendar getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Calendar expiresAt) {
        this.expiresAt = expiresAt;
    }
}
