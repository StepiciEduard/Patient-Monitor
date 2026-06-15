package itee.licenta.monitorizare.service.criteria;

import itee.licenta.monitorizare.domain.enumeration.PatientSubtype;
import itee.licenta.monitorizare.domain.enumeration.PatientType;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link itee.licenta.monitorizare.domain.Patient} entity. This class is used
 * in {@link itee.licenta.monitorizare.web.rest.PatientResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /patients?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PatientCriteria implements Serializable, Criteria {

    /**
     * Class for filtering PatientType
     */
    public static class PatientTypeFilter extends Filter<PatientType> {

        public PatientTypeFilter() {}

        public PatientTypeFilter(PatientTypeFilter filter) {
            super(filter);
        }

        @Override
        public PatientTypeFilter copy() {
            return new PatientTypeFilter(this);
        }
    }

    /**
     * Class for filtering PatientSubtype
     */
    public static class PatientSubtypeFilter extends Filter<PatientSubtype> {

        public PatientSubtypeFilter() {}

        public PatientSubtypeFilter(PatientSubtypeFilter filter) {
            super(filter);
        }

        @Override
        public PatientSubtypeFilter copy() {
            return new PatientSubtypeFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter cnp;

    private StringFilter phoneNumber;

    private StringFilter address;

    private PatientTypeFilter patientType;

    private PatientSubtypeFilter patientSubtype;

    private LocalDateFilter dateOfBirth;

    private StringFilter gender;

    private DoubleFilter hba1c;

    private DoubleFilter bmi;

    private DoubleFilter fev1Baseline;

    private LongFilter userId;

    private LongFilter doctorId;

    private Boolean distinct;

    public PatientCriteria() {}

    public PatientCriteria(PatientCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.cnp = other.optionalCnp().map(StringFilter::copy).orElse(null);
        this.phoneNumber = other.optionalPhoneNumber().map(StringFilter::copy).orElse(null);
        this.address = other.optionalAddress().map(StringFilter::copy).orElse(null);
        this.patientType = other.optionalPatientType().map(PatientTypeFilter::copy).orElse(null);
        this.patientSubtype = other.optionalPatientSubtype().map(PatientSubtypeFilter::copy).orElse(null);
        this.dateOfBirth = other.optionalDateOfBirth().map(LocalDateFilter::copy).orElse(null);
        this.gender = other.optionalGender().map(StringFilter::copy).orElse(null);
        this.hba1c = other.optionalHba1c().map(DoubleFilter::copy).orElse(null);
        this.bmi = other.optionalBmi().map(DoubleFilter::copy).orElse(null);
        this.fev1Baseline = other.optionalFev1Baseline().map(DoubleFilter::copy).orElse(null);
        this.userId = other.optionalUserId().map(LongFilter::copy).orElse(null);
        this.doctorId = other.optionalDoctorId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public PatientCriteria copy() {
        return new PatientCriteria(this);
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

    public StringFilter getCnp() {
        return cnp;
    }

    public Optional<StringFilter> optionalCnp() {
        return Optional.ofNullable(cnp);
    }

    public StringFilter cnp() {
        if (cnp == null) {
            setCnp(new StringFilter());
        }
        return cnp;
    }

    public void setCnp(StringFilter cnp) {
        this.cnp = cnp;
    }

    public StringFilter getPhoneNumber() {
        return phoneNumber;
    }

    public Optional<StringFilter> optionalPhoneNumber() {
        return Optional.ofNullable(phoneNumber);
    }

    public StringFilter phoneNumber() {
        if (phoneNumber == null) {
            setPhoneNumber(new StringFilter());
        }
        return phoneNumber;
    }

    public void setPhoneNumber(StringFilter phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public StringFilter getAddress() {
        return address;
    }

    public Optional<StringFilter> optionalAddress() {
        return Optional.ofNullable(address);
    }

    public StringFilter address() {
        if (address == null) {
            setAddress(new StringFilter());
        }
        return address;
    }

    public void setAddress(StringFilter address) {
        this.address = address;
    }

    public PatientTypeFilter getPatientType() {
        return patientType;
    }

    public Optional<PatientTypeFilter> optionalPatientType() {
        return Optional.ofNullable(patientType);
    }

    public PatientTypeFilter patientType() {
        if (patientType == null) {
            setPatientType(new PatientTypeFilter());
        }
        return patientType;
    }

    public void setPatientType(PatientTypeFilter patientType) {
        this.patientType = patientType;
    }

    public PatientSubtypeFilter getPatientSubtype() {
        return patientSubtype;
    }

    public Optional<PatientSubtypeFilter> optionalPatientSubtype() {
        return Optional.ofNullable(patientSubtype);
    }

    public PatientSubtypeFilter patientSubtype() {
        if (patientSubtype == null) {
            setPatientSubtype(new PatientSubtypeFilter());
        }
        return patientSubtype;
    }

    public void setPatientSubtype(PatientSubtypeFilter patientSubtype) {
        this.patientSubtype = patientSubtype;
    }

    public LocalDateFilter getDateOfBirth() {
        return dateOfBirth;
    }

    public Optional<LocalDateFilter> optionalDateOfBirth() {
        return Optional.ofNullable(dateOfBirth);
    }

    public LocalDateFilter dateOfBirth() {
        if (dateOfBirth == null) {
            setDateOfBirth(new LocalDateFilter());
        }
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDateFilter dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public StringFilter getGender() {
        return gender;
    }

    public Optional<StringFilter> optionalGender() {
        return Optional.ofNullable(gender);
    }

    public StringFilter gender() {
        if (gender == null) {
            setGender(new StringFilter());
        }
        return gender;
    }

    public void setGender(StringFilter gender) {
        this.gender = gender;
    }

    public DoubleFilter getHba1c() {
        return hba1c;
    }

    public Optional<DoubleFilter> optionalHba1c() {
        return Optional.ofNullable(hba1c);
    }

    public DoubleFilter hba1c() {
        if (hba1c == null) {
            setHba1c(new DoubleFilter());
        }
        return hba1c;
    }

    public void setHba1c(DoubleFilter hba1c) {
        this.hba1c = hba1c;
    }

    public DoubleFilter getBmi() {
        return bmi;
    }

    public Optional<DoubleFilter> optionalBmi() {
        return Optional.ofNullable(bmi);
    }

    public DoubleFilter bmi() {
        if (bmi == null) {
            setBmi(new DoubleFilter());
        }
        return bmi;
    }

    public void setBmi(DoubleFilter bmi) {
        this.bmi = bmi;
    }

    public DoubleFilter getFev1Baseline() {
        return fev1Baseline;
    }

    public Optional<DoubleFilter> optionalFev1Baseline() {
        return Optional.ofNullable(fev1Baseline);
    }

    public DoubleFilter fev1Baseline() {
        if (fev1Baseline == null) {
            setFev1Baseline(new DoubleFilter());
        }
        return fev1Baseline;
    }

    public void setFev1Baseline(DoubleFilter fev1Baseline) {
        this.fev1Baseline = fev1Baseline;
    }

    public LongFilter getUserId() {
        return userId;
    }

    public Optional<LongFilter> optionalUserId() {
        return Optional.ofNullable(userId);
    }

    public LongFilter userId() {
        if (userId == null) {
            setUserId(new LongFilter());
        }
        return userId;
    }

    public void setUserId(LongFilter userId) {
        this.userId = userId;
    }

    public LongFilter getDoctorId() {
        return doctorId;
    }

    public Optional<LongFilter> optionalDoctorId() {
        return Optional.ofNullable(doctorId);
    }

    public LongFilter doctorId() {
        if (doctorId == null) {
            setDoctorId(new LongFilter());
        }
        return doctorId;
    }

    public void setDoctorId(LongFilter doctorId) {
        this.doctorId = doctorId;
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
        final PatientCriteria that = (PatientCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(cnp, that.cnp) &&
            Objects.equals(phoneNumber, that.phoneNumber) &&
            Objects.equals(address, that.address) &&
            Objects.equals(patientType, that.patientType) &&
            Objects.equals(patientSubtype, that.patientSubtype) &&
            Objects.equals(dateOfBirth, that.dateOfBirth) &&
            Objects.equals(gender, that.gender) &&
            Objects.equals(hba1c, that.hba1c) &&
            Objects.equals(bmi, that.bmi) &&
            Objects.equals(fev1Baseline, that.fev1Baseline) &&
            Objects.equals(userId, that.userId) &&
            Objects.equals(doctorId, that.doctorId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            cnp,
            phoneNumber,
            address,
            patientType,
            patientSubtype,
            dateOfBirth,
            gender,
            hba1c,
            bmi,
            fev1Baseline,
            userId,
            doctorId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PatientCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalCnp().map(f -> "cnp=" + f + ", ").orElse("") +
            optionalPhoneNumber().map(f -> "phoneNumber=" + f + ", ").orElse("") +
            optionalAddress().map(f -> "address=" + f + ", ").orElse("") +
            optionalPatientType().map(f -> "patientType=" + f + ", ").orElse("") +
            optionalPatientSubtype().map(f -> "patientSubtype=" + f + ", ").orElse("") +
            optionalDateOfBirth().map(f -> "dateOfBirth=" + f + ", ").orElse("") +
            optionalGender().map(f -> "gender=" + f + ", ").orElse("") +
            optionalHba1c().map(f -> "hba1c=" + f + ", ").orElse("") +
            optionalBmi().map(f -> "bmi=" + f + ", ").orElse("") +
            optionalFev1Baseline().map(f -> "fev1Baseline=" + f + ", ").orElse("") +
            optionalUserId().map(f -> "userId=" + f + ", ").orElse("") +
            optionalDoctorId().map(f -> "doctorId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
