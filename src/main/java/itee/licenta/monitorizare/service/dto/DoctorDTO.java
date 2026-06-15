package itee.licenta.monitorizare.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link itee.licenta.monitorizare.domain.Doctor} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DoctorDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 100)
    private String specialization;

    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String officeLocation;

    @NotNull
    private UserDTO user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DoctorDTO)) {
            return false;
        }

        DoctorDTO doctorDTO = (DoctorDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, doctorDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DoctorDTO{" +
            "id=" + getId() +
            ", specialization='" + getSpecialization() + "'" +
            ", phone='" + getPhone() + "'" +
            ", officeLocation='" + getOfficeLocation() + "'" +
            ", user=" + getUser() +
            "}";
    }
}
