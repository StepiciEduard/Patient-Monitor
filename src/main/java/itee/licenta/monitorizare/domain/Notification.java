package itee.licenta.monitorizare.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import itee.licenta.monitorizare.domain.enumeration.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Notification.
 */
@Entity
@Table(name = "notification")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @NotNull
    @Size(max = 255)
    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Lob
    @Column(name = "message", nullable = false)
    private String message;

    @NotNull
    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(optional = false)
    @NotNull
    private User recipientUser;

    @ManyToOne(fetch = FetchType.LAZY)
    private User senderUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "user", "doctor" }, allowSetters = true)
    private Patient relatedPatient;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Notification id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NotificationType getType() {
        return this.type;
    }

    public Notification type(NotificationType type) {
        this.setType(type);
        return this;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return this.title;
    }

    public Notification title(String title) {
        this.setTitle(title);
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return this.message;
    }

    public Notification message(String message) {
        this.setMessage(message);
        return this;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getIsRead() {
        return this.isRead;
    }

    public Notification isRead(Boolean isRead) {
        this.setIsRead(isRead);
        return this;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Notification createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public User getRecipientUser() {
        return this.recipientUser;
    }

    public void setRecipientUser(User user) {
        this.recipientUser = user;
    }

    public Notification recipientUser(User user) {
        this.setRecipientUser(user);
        return this;
    }

    public User getSenderUser() {
        return this.senderUser;
    }

    public void setSenderUser(User user) {
        this.senderUser = user;
    }

    public Notification senderUser(User user) {
        this.setSenderUser(user);
        return this;
    }

    public Patient getRelatedPatient() {
        return this.relatedPatient;
    }

    public void setRelatedPatient(Patient patient) {
        this.relatedPatient = patient;
    }

    public Notification relatedPatient(Patient patient) {
        this.setRelatedPatient(patient);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Notification)) {
            return false;
        }
        return getId() != null && getId().equals(((Notification) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Notification{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", title='" + getTitle() + "'" +
            ", message='" + getMessage() + "'" +
            ", isRead='" + getIsRead() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
