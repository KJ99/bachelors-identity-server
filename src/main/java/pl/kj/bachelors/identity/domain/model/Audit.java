package pl.kj.bachelors.identity.domain.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Calendar;

@Embeddable
public class Audit {
    @Column(name = "updated_at")
    private Calendar updatedAt;
    @Column(name = "created_at")
    private Calendar createdAt;

    public Audit() {
        this.createdAt = Calendar.getInstance();
        this.updatedAt = null;
    }

    public Calendar getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Calendar updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Calendar getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Calendar createdAt) {
        this.createdAt = createdAt;
    }
}
