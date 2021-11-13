package pl.kj.bachelors.identity.domain.model.entity;

import org.hibernate.annotations.Loader;
import pl.kj.bachelors.identity.domain.model.embeddable.Audit;

import javax.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    private String uid;
    @Column(unique = true)
    private String email;
    @Column(unique = true, name = "username")
    private String userName;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    private boolean verified;
    private boolean active;
    @Embedded
    private Audit audit;
    private String password;
    @Column(unique = true)
    private String salt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "picture_id", referencedColumnName = "id")
    private UploadedFile picture;

    public User() {
        this.audit = new Audit();
        this.verified = false;
        this.active = false;
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public UploadedFile getPicture() {
        return picture;
    }

    public void setPicture(UploadedFile picture) {
        this.picture = picture;
    }
}
