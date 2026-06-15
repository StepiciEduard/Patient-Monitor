package itee.licenta.monitorizare.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link itee.licenta.monitorizare.domain.MedicalData} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MedicalDataDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant timestamp;

    private Integer heartRate;

    private Double spo2;

    private Double temperature;

    private Integer systolicBp;

    private Integer diastolicBp;

    private Double hrv;

    private Integer qtInterval;

    private Double bnp;

    private Double bloodGlucose;

    private Integer respiratoryRate;

    private Double fev1;

    private Double etco2;

    private Double anomalyScore;

    private Boolean isAnomaly;

    @NotNull
    private PatientDTO patient;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public Double getSpo2() {
        return spo2;
    }

    public void setSpo2(Double spo2) {
        this.spo2 = spo2;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getSystolicBp() {
        return systolicBp;
    }

    public void setSystolicBp(Integer systolicBp) {
        this.systolicBp = systolicBp;
    }

    public Integer getDiastolicBp() {
        return diastolicBp;
    }

    public void setDiastolicBp(Integer diastolicBp) {
        this.diastolicBp = diastolicBp;
    }

    public Double getHrv() {
        return hrv;
    }

    public void setHrv(Double hrv) {
        this.hrv = hrv;
    }

    public Integer getQtInterval() {
        return qtInterval;
    }

    public void setQtInterval(Integer qtInterval) {
        this.qtInterval = qtInterval;
    }

    public Double getBnp() {
        return bnp;
    }

    public void setBnp(Double bnp) {
        this.bnp = bnp;
    }

    public Double getBloodGlucose() {
        return bloodGlucose;
    }

    public void setBloodGlucose(Double bloodGlucose) {
        this.bloodGlucose = bloodGlucose;
    }

    public Integer getRespiratoryRate() {
        return respiratoryRate;
    }

    public void setRespiratoryRate(Integer respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    public Double getFev1() {
        return fev1;
    }

    public void setFev1(Double fev1) {
        this.fev1 = fev1;
    }

    public Double getEtco2() {
        return etco2;
    }

    public void setEtco2(Double etco2) {
        this.etco2 = etco2;
    }

    public Double getAnomalyScore() {
        return anomalyScore;
    }

    public void setAnomalyScore(Double anomalyScore) {
        this.anomalyScore = anomalyScore;
    }

    public Boolean getIsAnomaly() {
        return isAnomaly;
    }

    public void setIsAnomaly(Boolean isAnomaly) {
        this.isAnomaly = isAnomaly;
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
        if (!(o instanceof MedicalDataDTO)) {
            return false;
        }

        MedicalDataDTO medicalDataDTO = (MedicalDataDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, medicalDataDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MedicalDataDTO{" +
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
            ", patient=" + getPatient() +
            "}";
    }
}
