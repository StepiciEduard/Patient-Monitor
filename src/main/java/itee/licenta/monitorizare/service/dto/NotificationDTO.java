package itee.licenta.monitorizare.service.dto;

import itee.licenta.monitorizare.domain.enumeration.NotificationType;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link itee.licenta.monitorizare.domain.Notification} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class NotificationDTO implements Serializable {

    private Long id;

    @NotNull
    private NotificationType type;

    @NotNull
    @Size(max = 255)
    private String title;

    @Lob
    private String message;

    @NotNull
    private Boolean isRead;

    @NotNull
    private Instant createdAt;

    @NotNull
    private UserDTO recipientUser;

    private UserDTO senderUser;

    private PatientDTO relatedPatient;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public UserDTO getRecipientUser() {
        return recipientUser;
    }

    public void setRecipientUser(UserDTO recipientUser) {
        this.recipientUser = recipientUser;
    }

    public UserDTO getSenderUser() {
        return senderUser;
    }

    public void setSenderUser(UserDTO senderUser) {
        this.senderUser = senderUser;
    }

    public PatientDTO getRelatedPatient() {
        return relatedPatient;
    }

    public void setRelatedPatient(PatientDTO relatedPatient) {
        this.relatedPatient = relatedPatient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NotificationDTO)) {
            return false;
        }

        NotificationDTO notificationDTO = (NotificationDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, notificationDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NotificationDTO{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", title='" + getTitle() + "'" +
            ", message='" + getMessage() + "'" +
            ", isRead='" + getIsRead() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", recipientUser=" + getRecipientUser() +
            ", senderUser=" + getSenderUser() +
            ", relatedPatient=" + getRelatedPatient() +
            "}";
    }
}
