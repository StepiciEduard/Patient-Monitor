package itee.licenta.monitorizare.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link itee.licenta.monitorizare.domain.MedicalData} entity. This class is used
 * in {@link itee.licenta.monitorizare.web.rest.MedicalDataResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /medical-data?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MedicalDataCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private InstantFilter timestamp;

    private IntegerFilter heartRate;

    private DoubleFilter spo2;

    private DoubleFilter temperature;

    private IntegerFilter systolicBp;

    private IntegerFilter diastolicBp;

    private DoubleFilter hrv;

    private IntegerFilter qtInterval;

    private DoubleFilter bnp;

    private DoubleFilter bloodGlucose;

    private IntegerFilter respiratoryRate;

    private DoubleFilter fev1;

    private DoubleFilter etco2;

    private DoubleFilter anomalyScore;

    private BooleanFilter isAnomaly;

    private LongFilter patientId;

    private Boolean distinct;

    public MedicalDataCriteria() {}

    public MedicalDataCriteria(MedicalDataCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.timestamp = other.optionalTimestamp().map(InstantFilter::copy).orElse(null);
        this.heartRate = other.optionalHeartRate().map(IntegerFilter::copy).orElse(null);
        this.spo2 = other.optionalSpo2().map(DoubleFilter::copy).orElse(null);
        this.temperature = other.optionalTemperature().map(DoubleFilter::copy).orElse(null);
        this.systolicBp = other.optionalSystolicBp().map(IntegerFilter::copy).orElse(null);
        this.diastolicBp = other.optionalDiastolicBp().map(IntegerFilter::copy).orElse(null);
        this.hrv = other.optionalHrv().map(DoubleFilter::copy).orElse(null);
        this.qtInterval = other.optionalQtInterval().map(IntegerFilter::copy).orElse(null);
        this.bnp = other.optionalBnp().map(DoubleFilter::copy).orElse(null);
        this.bloodGlucose = other.optionalBloodGlucose().map(DoubleFilter::copy).orElse(null);
        this.respiratoryRate = other.optionalRespiratoryRate().map(IntegerFilter::copy).orElse(null);
        this.fev1 = other.optionalFev1().map(DoubleFilter::copy).orElse(null);
        this.etco2 = other.optionalEtco2().map(DoubleFilter::copy).orElse(null);
        this.anomalyScore = other.optionalAnomalyScore().map(DoubleFilter::copy).orElse(null);
        this.isAnomaly = other.optionalIsAnomaly().map(BooleanFilter::copy).orElse(null);
        this.patientId = other.optionalPatientId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public MedicalDataCriteria copy() {
        return new MedicalDataCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public Optional<LongFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public LongFilter id() {
        if (id == null) {
            setId(new LongFilter());
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public InstantFilter getTimestamp() {
        return timestamp;
    }

    public Optional<InstantFilter> optionalTimestamp() {
        return Optional.ofNullable(timestamp);
    }

    public InstantFilter timestamp() {
        if (timestamp == null) {
            setTimestamp(new InstantFilter());
        }
        return timestamp;
    }

    public void setTimestamp(InstantFilter timestamp) {
        this.timestamp = timestamp;
    }

    public IntegerFilter getHeartRate() {
        return heartRate;
    }

    public Optional<IntegerFilter> optionalHeartRate() {
        return Optional.ofNullable(heartRate);
    }

    public IntegerFilter heartRate() {
        if (heartRate == null) {
            setHeartRate(new IntegerFilter());
        }
        return heartRate;
    }

    public void setHeartRate(IntegerFilter heartRate) {
        this.heartRate = heartRate;
    }

    public DoubleFilter getSpo2() {
        return spo2;
    }

    public Optional<DoubleFilter> optionalSpo2() {
        return Optional.ofNullable(spo2);
    }

    public DoubleFilter spo2() {
        if (spo2 == null) {
            setSpo2(new DoubleFilter());
        }
        return spo2;
    }

    public void setSpo2(DoubleFilter spo2) {
        this.spo2 = spo2;
    }

    public DoubleFilter getTemperature() {
        return temperature;
    }

    public Optional<DoubleFilter> optionalTemperature() {
        return Optional.ofNullable(temperature);
    }

    public DoubleFilter temperature() {
        if (temperature == null) {
            setTemperature(new DoubleFilter());
        }
        return temperature;
    }

    public void setTemperature(DoubleFilter temperature) {
        this.temperature = temperature;
    }

    public IntegerFilter getSystolicBp() {
        return systolicBp;
    }

    public Optional<IntegerFilter> optionalSystolicBp() {
        return Optional.ofNullable(systolicBp);
    }

    public IntegerFilter systolicBp() {
        if (systolicBp == null) {
            setSystolicBp(new IntegerFilter());
        }
        return systolicBp;
    }

    public void setSystolicBp(IntegerFilter systolicBp) {
        this.systolicBp = systolicBp;
    }

    public IntegerFilter getDiastolicBp() {
        return diastolicBp;
    }

    public Optional<IntegerFilter> optionalDiastolicBp() {
        return Optional.ofNullable(diastolicBp);
    }

    public IntegerFilter diastolicBp() {
        if (diastolicBp == null) {
            setDiastolicBp(new IntegerFilter());
        }
        return diastolicBp;
    }

    public void setDiastolicBp(IntegerFilter diastolicBp) {
        this.diastolicBp = diastolicBp;
    }

    public DoubleFilter getHrv() {
        return hrv;
    }

    public Optional<DoubleFilter> optionalHrv() {
        return Optional.ofNullable(hrv);
    }

    public DoubleFilter hrv() {
        if (hrv == null) {
            setHrv(new DoubleFilter());
        }
        return hrv;
    }

    public void setHrv(DoubleFilter hrv) {
        this.hrv = hrv;
    }

    public IntegerFilter getQtInterval() {
        return qtInterval;
    }

    public Optional<IntegerFilter> optionalQtInterval() {
        return Optional.ofNullable(qtInterval);
    }

    public IntegerFilter qtInterval() {
        if (qtInterval == null) {
            setQtInterval(new IntegerFilter());
        }
        return qtInterval;
    }

    public void setQtInterval(IntegerFilter qtInterval) {
        this.qtInterval = qtInterval;
    }

    public DoubleFilter getBnp() {
        return bnp;
    }

    public Optional<DoubleFilter> optionalBnp() {
        return Optional.ofNullable(bnp);
    }

    public DoubleFilter bnp() {
        if (bnp == null) {
            setBnp(new DoubleFilter());
        }
        return bnp;
    }

    public void setBnp(DoubleFilter bnp) {
        this.bnp = bnp;
    }

    public DoubleFilter getBloodGlucose() {
        return bloodGlucose;
    }

    public Optional<DoubleFilter> optionalBloodGlucose() {
        return Optional.ofNullable(bloodGlucose);
    }

    public DoubleFilter bloodGlucose() {
        if (bloodGlucose == null) {
            setBloodGlucose(new DoubleFilter());
        }
        return bloodGlucose;
    }

    public void setBloodGlucose(DoubleFilter bloodGlucose) {
        this.bloodGlucose = bloodGlucose;
    }

    public IntegerFilter getRespiratoryRate() {
        return respiratoryRate;
    }

    public Optional<IntegerFilter> optionalRespiratoryRate() {
        return Optional.ofNullable(respiratoryRate);
    }

    public IntegerFilter respiratoryRate() {
        if (respiratoryRate == null) {
            setRespiratoryRate(new IntegerFilter());
        }
        return respiratoryRate;
    }

    public void setRespiratoryRate(IntegerFilter respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    public DoubleFilter getFev1() {
        return fev1;
    }

    public Optional<DoubleFilter> optionalFev1() {
        return Optional.ofNullable(fev1);
    }

    public DoubleFilter fev1() {
        if (fev1 == null) {
            setFev1(new DoubleFilter());
        }
        return fev1;
    }

    public void setFev1(DoubleFilter fev1) {
        this.fev1 = fev1;
    }

    public DoubleFilter getEtco2() {
        return etco2;
    }

    public Optional<DoubleFilter> optionalEtco2() {
        return Optional.ofNullable(etco2);
    }

    public DoubleFilter etco2() {
        if (etco2 == null) {
            setEtco2(new DoubleFilter());
        }
        return etco2;
    }

    public void setEtco2(DoubleFilter etco2) {
        this.etco2 = etco2;
    }

    public DoubleFilter getAnomalyScore() {
        return anomalyScore;
    }

    public Optional<DoubleFilter> optionalAnomalyScore() {
        return Optional.ofNullable(anomalyScore);
    }

    public DoubleFilter anomalyScore() {
        if (anomalyScore == null) {
            setAnomalyScore(new DoubleFilter());
        }
        return anomalyScore;
    }

    public void setAnomalyScore(DoubleFilter anomalyScore) {
        this.anomalyScore = anomalyScore;
    }

    public BooleanFilter getIsAnomaly() {
        return isAnomaly;
    }

    public Optional<BooleanFilter> optionalIsAnomaly() {
        return Optional.ofNullable(isAnomaly);
    }

    public BooleanFilter isAnomaly() {
        if (isAnomaly == null) {
            setIsAnomaly(new BooleanFilter());
        }
        return isAnomaly;
    }

    public void setIsAnomaly(BooleanFilter isAnomaly) {
        this.isAnomaly = isAnomaly;
    }

    public LongFilter getPatientId() {
        return patientId;
    }

    public Optional<LongFilter> optionalPatientId() {
        return Optional.ofNullable(patientId);
    }

    public LongFilter patientId() {
        if (patientId == null) {
            setPatientId(new LongFilter());
        }
        return patientId;
    }

    public void setPatientId(LongFilter patientId) {
        this.patientId = patientId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MedicalDataCriteria that = (MedicalDataCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(timestamp, that.timestamp) &&
            Objects.equals(heartRate, that.heartRate) &&
            Objects.equals(spo2, that.spo2) &&
            Objects.equals(temperature, that.temperature) &&
            Objects.equals(systolicBp, that.systolicBp) &&
            Objects.equals(diastolicBp, that.diastolicBp) &&
            Objects.equals(hrv, that.hrv) &&
            Objects.equals(qtInterval, that.qtInterval) &&
            Objects.equals(bnp, that.bnp) &&
            Objects.equals(bloodGlucose, that.bloodGlucose) &&
            Objects.equals(respiratoryRate, that.respiratoryRate) &&
            Objects.equals(fev1, that.fev1) &&
            Objects.equals(etco2, that.etco2) &&
            Objects.equals(anomalyScore, that.anomalyScore) &&
            Objects.equals(isAnomaly, that.isAnomaly) &&
            Objects.equals(patientId, that.patientId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            timestamp,
            heartRate,
            spo2,
            temperature,
            systolicBp,
            diastolicBp,
            hrv,
            qtInterval,
            bnp,
            bloodGlucose,
            respiratoryRate,
            fev1,
            etco2,
            anomalyScore,
            isAnomaly,
            patientId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MedicalDataCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalTimestamp().map(f -> "timestamp=" + f + ", ").orElse("") +
            optionalHeartRate().map(f -> "heartRate=" + f + ", ").orElse("") +
            optionalSpo2().map(f -> "spo2=" + f + ", ").orElse("") +
            optionalTemperature().map(f -> "temperature=" + f + ", ").orElse("") +
            optionalSystolicBp().map(f -> "systolicBp=" + f + ", ").orElse("") +
            optionalDiastolicBp().map(f -> "diastolicBp=" + f + ", ").orElse("") +
            optionalHrv().map(f -> "hrv=" + f + ", ").orElse("") +
            optionalQtInterval().map(f -> "qtInterval=" + f + ", ").orElse("") +
            optionalBnp().map(f -> "bnp=" + f + ", ").orElse("") +
            optionalBloodGlucose().map(f -> "bloodGlucose=" + f + ", ").orElse("") +
            optionalRespiratoryRate().map(f -> "respiratoryRate=" + f + ", ").orElse("") +
            optionalFev1().map(f -> "fev1=" + f + ", ").orElse("") +
            optionalEtco2().map(f -> "etco2=" + f + ", ").orElse("") +
            optionalAnomalyScore().map(f -> "anomalyScore=" + f + ", ").orElse("") +
            optionalIsAnomaly().map(f -> "isAnomaly=" + f + ", ").orElse("") +
            optionalPatientId().map(f -> "patientId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
