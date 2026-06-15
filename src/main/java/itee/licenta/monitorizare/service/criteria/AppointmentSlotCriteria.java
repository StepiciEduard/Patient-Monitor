package itee.licenta.monitorizare.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link itee.licenta.monitorizare.domain.AppointmentSlot} entity. This class is used
 * in {@link itee.licenta.monitorizare.web.rest.AppointmentSlotResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /appointment-slots?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AppointmentSlotCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private InstantFilter startTime;

    private InstantFilter endTime;

    private BooleanFilter isAvailable;

    private LongFilter doctorId;

    private Boolean distinct;

    public AppointmentSlotCriteria() {}

    public AppointmentSlotCriteria(AppointmentSlotCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.startTime = other.optionalStartTime().map(InstantFilter::copy).orElse(null);
        this.endTime = other.optionalEndTime().map(InstantFilter::copy).orElse(null);
        this.isAvailable = other.optionalIsAvailable().map(BooleanFilter::copy).orElse(null);
        this.doctorId = other.optionalDoctorId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public AppointmentSlotCriteria copy() {
        return new AppointmentSlotCriteria(this);
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

    public InstantFilter getStartTime() {
        return startTime;
    }

    public Optional<InstantFilter> optionalStartTime() {
        return Optional.ofNullable(startTime);
    }

    public InstantFilter startTime() {
        if (startTime == null) {
            setStartTime(new InstantFilter());
        }
        return startTime;
    }

    public void setStartTime(InstantFilter startTime) {
        this.startTime = startTime;
    }

    public InstantFilter getEndTime() {
        return endTime;
    }

    public Optional<InstantFilter> optionalEndTime() {
        return Optional.ofNullable(endTime);
    }

    public InstantFilter endTime() {
        if (endTime == null) {
            setEndTime(new InstantFilter());
        }
        return endTime;
    }

    public void setEndTime(InstantFilter endTime) {
        this.endTime = endTime;
    }

    public BooleanFilter getIsAvailable() {
        return isAvailable;
    }

    public Optional<BooleanFilter> optionalIsAvailable() {
        return Optional.ofNullable(isAvailable);
    }

    public BooleanFilter isAvailable() {
        if (isAvailable == null) {
            setIsAvailable(new BooleanFilter());
        }
        return isAvailable;
    }

    public void setIsAvailable(BooleanFilter isAvailable) {
        this.isAvailable = isAvailable;
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
        final AppointmentSlotCriteria that = (AppointmentSlotCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(startTime, that.startTime) &&
            Objects.equals(endTime, that.endTime) &&
            Objects.equals(isAvailable, that.isAvailable) &&
            Objects.equals(doctorId, that.doctorId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startTime, endTime, isAvailable, doctorId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AppointmentSlotCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalStartTime().map(f -> "startTime=" + f + ", ").orElse("") +
            optionalEndTime().map(f -> "endTime=" + f + ", ").orElse("") +
            optionalIsAvailable().map(f -> "isAvailable=" + f + ", ").orElse("") +
            optionalDoctorId().map(f -> "doctorId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
