package itee.licenta.monitorizare.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import itee.licenta.monitorizare.domain.enumeration.ChatRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A ChatMessage.
 */
@Entity
@Table(name = "chat_message")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ChatRole role;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "context_start_date")
    private LocalDate contextStartDate;

    @Column(name = "context_end_date")
    private LocalDate contextEndDate;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "user", "doctor" }, allowSetters = true)
    private Patient patient;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ChatMessage id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChatRole getRole() {
        return this.role;
    }

    public ChatMessage role(ChatRole role) {
        this.setRole(role);
        return this;
    }

    public void setRole(ChatRole role) {
        this.role = role;
    }

    public String getContent() {
        return this.content;
    }

    public ChatMessage content(String content) {
        this.setContent(content);
        return this;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public ChatMessage createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getContextStartDate() {
        return this.contextStartDate;
    }

    public ChatMessage contextStartDate(LocalDate contextStartDate) {
        this.setContextStartDate(contextStartDate);
        return this;
    }

    public void setContextStartDate(LocalDate contextStartDate) {
        this.contextStartDate = contextStartDate;
    }

    public LocalDate getContextEndDate() {
        return this.contextEndDate;
    }

    public ChatMessage contextEndDate(LocalDate contextEndDate) {
        this.setContextEndDate(contextEndDate);
        return this;
    }

    public void setContextEndDate(LocalDate contextEndDate) {
        this.contextEndDate = contextEndDate;
    }

    public Patient getPatient() {
        return this.patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public ChatMessage patient(Patient patient) {
        this.setPatient(patient);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChatMessage)) {
            return false;
        }
        return getId() != null && getId().equals(((ChatMessage) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ChatMessage{" +
            "id=" + getId() +
            ", role='" + getRole() + "'" +
            ", content='" + getContent() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", contextStartDate='" + getContextStartDate() + "'" +
            ", contextEndDate='" + getContextEndDate() + "'" +
            "}";
    }
}
