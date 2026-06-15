package itee.licenta.monitorizare.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A MedicalData.
 */
@Entity
@Table(name = "medical_data")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MedicalData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "spo_2")
    private Double spo2;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "systolic_bp")
    private Integer systolicBp;

    @Column(name = "diastolic_bp")
    private Integer diastolicBp;

    @Column(name = "hrv")
    private Double hrv;

    @Column(name = "qt_interval")
    private Integer qtInterval;

    @Column(name = "bnp")
    private Double bnp;

    @Column(name = "blood_glucose")
    private Double bloodGlucose;

    @Column(name = "respiratory_rate")
    private Integer respiratoryRate;

    @Column(name = "fev_1")
    private Double fev1;

    @Column(name = "etco_2")
    private Double etco2;

    @Column(name = "anomaly_score")
    private Double anomalyScore;

    @Column(name = "is_anomaly")
    private Boolean isAnomaly;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "user", "doctor" }, allowSetters = true)
    private Patient patient;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public MedicalData id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }

    public MedicalData timestamp(Instant timestamp) {
        this.setTimestamp(timestamp);
        return this;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getHeartRate() {
        return this.heartRate;
    }

    public MedicalData heartRate(Integer heartRate) {
        this.setHeartRate(heartRate);
        return this;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public Double getSpo2() {
        return this.spo2;
    }

    public MedicalData spo2(Double spo2) {
        this.setSpo2(spo2);
        return this;
    }

    public void setSpo2(Double spo2) {
        this.spo2 = spo2;
    }

    public Double getTemperature() {
        return this.temperature;
    }

    public MedicalData temperature(Double temperature) {
        this.setTemperature(temperature);
        return this;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getSystolicBp() {
        return this.systolicBp;
    }

    public MedicalData systolicBp(Integer systolicBp) {
        this.setSystolicBp(systolicBp);
        return this;
    }

    public void setSystolicBp(Integer systolicBp) {
        this.systolicBp = systolicBp;
    }

    public Integer getDiastolicBp() {
        return this.diastolicBp;
    }

    public MedicalData diastolicBp(Integer diastolicBp) {
        this.setDiastolicBp(diastolicBp);
        return this;
    }

    public void setDiastolicBp(Integer diastolicBp) {
        this.diastolicBp = diastolicBp;
    }

    public Double getHrv() {
        return this.hrv;
    }

    public MedicalData hrv(Double hrv) {
        this.setHrv(hrv);
        return this;
    }

    public void setHrv(Double hrv) {
        this.hrv = hrv;
    }

    public Integer getQtInterval() {
        return this.qtInterval;
    }

    public MedicalData qtInterval(Integer qtInterval) {
        this.setQtInterval(qtInterval);
        return this;
    }

    public void setQtInterval(Integer qtInterval) {
        this.qtInterval = qtInterval;
    }

    public Double getBnp() {
        return this.bnp;
    }

    public MedicalData bnp(Double bnp) {
        this.setBnp(bnp);
        return this;
    }

    public void setBnp(Double bnp) {
        this.bnp = bnp;
    }

    public Double getBloodGlucose() {
        return this.bloodGlucose;
    }

    public MedicalData bloodGlucose(Double bloodGlucose) {
        this.setBloodGlucose(bloodGlucose);
        return this;
    }

    public void setBloodGlucose(Double bloodGlucose) {
        this.bloodGlucose = bloodGlucose;
    }

    public Integer getRespiratoryRate() {
        return this.respiratoryRate;
    }

    public MedicalData respiratoryRate(Integer respiratoryRate) {
        this.setRespiratoryRate(respiratoryRate);
        return this;
    }

    public void setRespiratoryRate(Integer respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    public Double getFev1() {
        return this.fev1;
    }

    public MedicalData fev1(Double fev1) {
        this.setFev1(fev1);
        return this;
    }

    public void setFev1(Double fev1) {
        this.fev1 = fev1;
    }

    public Double getEtco2() {
        return this.etco2;
    }

    public MedicalData etco2(Double etco2) {
        this.setEtco2(etco2);
        return this;
    }

    public void setEtco2(Double etco2) {
        this.etco2 = etco2;
    }

    public Double getAnomalyScore() {
        return this.anomalyScore;
    }

    public MedicalData anomalyScore(Double anomalyScore) {
        this.setAnomalyScore(anomalyScore);
        return this;
    }

    public void setAnomalyScore(Double anomalyScore) {
        this.anomalyScore = anomalyScore;
    }

    public Boolean getIsAnomaly() {
        return this.isAnomaly;
    }

    public MedicalData isAnomaly(Boolean isAnomaly) {
        this.setIsAnomaly(isAnomaly);
        return this;
    }

    public void setIsAnomaly(Boolean isAnomaly) {
        this.isAnomaly = isAnomaly;
    }

    public Patient getPatient() {
        return this.patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public MedicalData patient(Patient patient) {
        this.setPatient(patient);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MedicalData)) {
            return false;
        }
        return getId() != null && getId().equals(((MedicalData) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MedicalData{" +
            "id=" + getId() +
            ", timestamp='" + getTimestamp() + "'" +
            ", heartRate=" + getHeartRate() +
            ", spo2=" + getSpo2() +
            ", temperature=" + getTemperature() +
            ", systolicBp=" + getSystolicBp() +
            ", diastolicBp=" + getDiastolicBp() +
            ", hrv=" + getHrv() +
            ", qtInterval=" + getQtInterval() +
            ", bnp=" + getBnp() +
            ", bloodGlucose=" + getBloodGlucose() +
            ", respiratoryRate=" + getRespiratoryRate() +
            ", fev1=" + getFev1() +
            ", etco2=" + getEtco2() +
            ", anomalyScore=" + getAnomalyScore() +
            ", isAnomaly='" + getIsAnomaly() + "'" +
            "}";
    }
}
