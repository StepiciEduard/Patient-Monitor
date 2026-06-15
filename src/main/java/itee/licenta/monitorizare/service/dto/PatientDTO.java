package itee.licenta.monitorizare.service.dto;

import itee.licenta.monitorizare.domain.enumeration.PatientSubtype;
import itee.licenta.monitorizare.domain.enumeration.PatientType;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link itee.licenta.monitorizare.domain.Patient} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PatientDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 13, max = 13)
    private String cnp;

    @Size(max = 20)
    private String phoneNumber;

    @Size(max = 500)
    private String address;

    @NotNull
    private PatientType patientType;

    @NotNull
    private PatientSubtype patientSubtype;

    @NotNull
    private LocalDate dateOfBirth;

    @Size(max = 10)
    private String gender;

    private Double hba1c;

    private Double bmi;

    private Double fev1Baseline;

    @NotNull
    private UserDTO user;

    @NotNull
    private DoctorDTO doctor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCnp() {
        return cnp;
    }

    public void setCnp(String cnp) {
        this.cnp = cnp;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public PatientType getPatientType() {
        return patientType;
    }

    public void setPatientType(PatientType patientType) {
        this.patientType = patientType;
    }

    public PatientSubtype getPatientSubtype() {
        return patientSubtype;
    }

    public void setPatientSubtype(PatientSubtype patientSubtype) {
        this.patientSubtype = patientSubtype;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Double getHba1c() {
        return hba1c;
    }

    public void setHba1c(Double hba1c) {
        this.hba1c = hba1c;
    }

    public Double getBmi() {
        return bmi;
    }

    public void setBmi(Double bmi) {
        this.bmi = bmi;
    }

    public Double getFev1Baseline() {
        return fev1Baseline;
    }

    public void setFev1Baseline(Double fev1Baseline) {
        this.fev1Baseline = fev1Baseline;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
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
        if (!(o instanceof PatientDTO)) {
            return false;
        }

        PatientDTO patientDTO = (PatientDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, patientDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PatientDTO{" +
            "id=" + getId() +
            ", cnp='" + getCnp() + "'" +
            ", phoneNumber='" + getPhoneNumber() + "'" +
            ", address='" + getAddress() + "'" +
            ", patientType='" + getPatientType() + "'" +
            ", patientSubtype='" + getPatientSubtype() + "'" +
            ", dateOfBirth='" + getDateOfBirth() + "'" +
            ", gender='" + getGender() + "'" +
            ", hba1c=" + getHba1c() +
            ", bmi=" + getBmi() +
            ", fev1Baseline=" + getFev1Baseline() +
            ", user=" + getUser() +
            ", doctor=" + getDoctor() +
            "}";
    }
}
