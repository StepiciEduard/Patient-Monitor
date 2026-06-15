package itee.licenta.monitorizare.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link itee.licenta.monitorizare.domain.AppointmentSlot} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AppointmentSlotDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;

    @NotNull
    private Boolean isAvailable;

    @NotNull
    private DoctorDTO doctor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
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
        if (!(o instanceof AppointmentSlotDTO)) {
            return false;
        }

        AppointmentSlotDTO appointmentSlotDTO = (AppointmentSlotDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, appointmentSlotDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AppointmentSlotDTO{" +
            "id=" + getId() +
            ", startTime='" + getStartTime() + "'" +
            ", endTime='" + getEndTime() + "'" +
            ", isAvailable='" + getIsAvailable() + "'" +
            ", doctor=" + getDoctor() +
            "}";
    }
}
