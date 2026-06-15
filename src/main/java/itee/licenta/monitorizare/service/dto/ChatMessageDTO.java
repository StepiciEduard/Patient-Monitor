package itee.licenta.monitorizare.service.dto;

import itee.licenta.monitorizare.domain.enumeration.ChatRole;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link itee.licenta.monitorizare.domain.ChatMessage} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ChatMessageDTO implements Serializable {

    private Long id;

    @NotNull
    private ChatRole role;

    @Lob
    private String content;

    @NotNull
    private Instant createdAt;

    private LocalDate contextStartDate;

    private LocalDate contextEndDate;

    @NotNull
    private PatientDTO patient;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChatRole getRole() {
        return role;
    }

    public void setRole(ChatRole role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getContextStartDate() {
        return contextStartDate;
    }

    public void setContextStartDate(LocalDate contextStartDate) {
        this.contextStartDate = contextStartDate;
    }

    public LocalDate getContextEndDate() {
        return contextEndDate;
    }

    public void setContextEndDate(LocalDate contextEndDate) {
        this.contextEndDate = contextEndDate;
    }

    public PatientDTO getPatient() {
        return patient;
    }

    public void setPatient(PatientDTO patient) {
        this.patient = patient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChatMessageDTO)) {
            return false;
        }

        ChatMessageDTO chatMessageDTO = (ChatMessageDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, chatMessageDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ChatMessageDTO{" +
            "id=" + getId() +
            ", role='" + getRole() + "'" +
            ", content='" + getContent() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", contextStartDate='" + getContextStartDate() + "'" +
            ", contextEndDate='" + getContextEndDate() + "'" +
            ", patient=" + getPatient() +
            "}";
    }
}
