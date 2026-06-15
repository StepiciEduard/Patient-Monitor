package itee.licenta.monitorizare.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import itee.licenta.monitorizare.domain.enumeration.PatientSubtype;
import itee.licenta.monitorizare.domain.enumeration.PatientType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Patient.
 */
@Entity
@Table(name = "patient")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Patient implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(min = 13, max = 13)
    @Column(name = "cnp", length = 13, nullable = false, unique = true)
    private String cnp;

    @Size(max = 20)
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Size(max = 500)
    @Column(name = "address", length = 500)
    private String address;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "patient_type", nullable = false)
    private PatientType patientType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "patient_subtype", nullable = false)
    private PatientSubtype patientSubtype;

    @NotNull
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Size(max = 10)
    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "hba_1_c")
    private Double hba1c;

    @Column(name = "bmi")
    private Double bmi;

    @Column(name = "fev_1_baseline")
    private Double fev1Baseline;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JoinColumn(unique = true)
    private User user;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "user" }, allowSetters = true)
    private Doctor doctor;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Patient id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCnp() {
        return this.cnp;
    }

    public Patient cnp(String cnp) {
        this.setCnp(cnp);
        return this;
    }

    public void setCnp(String cnp) {
        this.cnp = cnp;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public Patient phoneNumber(String phoneNumber) {
        this.setPhoneNumber(phoneNumber);
        return this;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return this.address;
    }

    public Patient address(String address) {
        this.setAddress(address);
        return this;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public PatientType getPatientType() {
        return this.patientType;
    }

    public Patient patientType(PatientType patientType) {
        this.setPatientType(patientType);
        return this;
    }

    public void setPatientType(PatientType patientType) {
        this.patientType = patientType;
    }

    public PatientSubtype getPatientSubtype() {
        return this.patientSubtype;
    }

    public Patient patientSubtype(PatientSubtype patientSubtype) {
        this.setPatientSubtype(patientSubtype);
        return this;
    }

    public void setPatientSubtype(PatientSubtype patientSubtype) {
        this.patientSubtype = patientSubtype;
    }

    public LocalDate getDateOfBirth() {
        return this.dateOfBirth;
    }

    public Patient dateOfBirth(LocalDate dateOfBirth) {
        this.setDateOfBirth(dateOfBirth);
        return this;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return this.gender;
    }

    public Patient gender(String gender) {
        this.setGender(gender);
        return this;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Double getHba1c() {
        return this.hba1c;
    }

    public Patient hba1c(Double hba1c) {
        this.setHba1c(hba1c);
        return this;
    }

    public void setHba1c(Double hba1c) {
        this.hba1c = hba1c;
    }

    public Double getBmi() {
        return this.bmi;
    }

    public Patient bmi(Double bmi) {
        this.setBmi(bmi);
        return this;
    }

    public void setBmi(Double bmi) {
        this.bmi = bmi;
    }

    public Double getFev1Baseline() {
        return this.fev1Baseline;
    }

    public Patient fev1Baseline(Double fev1Baseline) {
        this.setFev1Baseline(fev1Baseline);
        return this;
    }

    public void setFev1Baseline(Double fev1Baseline) {
        this.fev1Baseline = fev1Baseline;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Patient user(User user) {
        this.setUser(user);
        return this;
    }

    public Doctor getDoctor() {
        return this.doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Patient doctor(Doctor doctor) {
        this.setDoctor(doctor);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Patient)) {
            return false;
        }
        return getId() != null && getId().equals(((Patient) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Patient{" +
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
            "}";
    }
}
