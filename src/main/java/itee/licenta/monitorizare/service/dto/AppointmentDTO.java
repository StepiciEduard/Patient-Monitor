package itee.licenta.monitorizare.service.dto;

import itee.licenta.monitorizare.domain.enumeration.AppointmentStatus;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link itee.licenta.monitorizare.domain.Appointment} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AppointmentDTO implements Serializable {

    private Long id;

    @NotNull
    private AppointmentStatus status;

    @Lob
    private String notes;

    @NotNull
    private Instant createdAt;

    @NotNull
    private AppointmentSlotDTO slot;

    @NotNull
    private PatientDTO patient;

    @NotNull
    private DoctorDTO doctor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public AppointmentSlotDTO getSlot() {
        return slot;
    }

    public void setSlot(AppointmentSlotDTO slot) {
        this.slot = slot;
    }

    public PatientDTO getPatient() {
        return patient;
    }

    public void setPatient(PatientDTO patient) {
        this.patient = patient;
    }

    public DoctorDTO getDoctor() {
        return doctor;
    }

    public void setDoctor(DoctorDTO doctor) {
        this.doctor = doctor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AppointmentDTO)) {
            return false;
        }

        AppointmentDTO appointmentDTO = (AppointmentDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, appointmentDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AppointmentDTO{" +
            "id=" + getId() +
            ", status='" + getStatus() + "'" +
            ", notes='" + getNotes() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", slot=" + getSlot() +
            ", patient=" + getPatient() +
            ", doctor=" + getDoctor() +
            "}";
    }
}
