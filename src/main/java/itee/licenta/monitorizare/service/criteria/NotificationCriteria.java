package itee.licenta.monitorizare.service.criteria;

import itee.licenta.monitorizare.domain.enumeration.NotificationType;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link itee.licenta.monitorizare.domain.Notification} entity. This class is used
 * in {@link itee.licenta.monitorizare.web.rest.NotificationResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /notifications?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class NotificationCriteria implements Serializable, Criteria {

    /**
     * Class for filtering NotificationType
     */
    public static class NotificationTypeFilter extends Filter<NotificationType> {

        public NotificationTypeFilter() {}

        public NotificationTypeFilter(NotificationTypeFilter filter) {
            super(filter);
        }

        @Override
        public NotificationTypeFilter copy() {
            return new NotificationTypeFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private NotificationTypeFilter type;

    private StringFilter title;

    private BooleanFilter isRead;

    private InstantFilter createdAt;

    private LongFilter recipientUserId;

    private LongFilter senderUserId;

    private LongFilter relatedPatientId;

    private Boolean distinct;

    public NotificationCriteria() {}

    public NotificationCriteria(NotificationCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.type = other.optionalType().map(NotificationTypeFilter::copy).orElse(null);
        this.title = other.optionalTitle().map(StringFilter::copy).orElse(null);
        this.isRead = other.optionalIsRead().map(BooleanFilter::copy).orElse(null);
        this.createdAt = other.optionalCreatedAt().map(InstantFilter::copy).orElse(null);
        this.recipientUserId = other.optionalRecipientUserId().map(LongFilter::copy).orElse(null);
        this.senderUserId = other.optionalSenderUserId().map(LongFilter::copy).orElse(null);
        this.relatedPatientId = other.optionalRelatedPatientId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public NotificationCriteria copy() {
        return new NotificationCriteria(this);
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

    public NotificationTypeFilter getType() {
        return type;
    }

    public Optional<NotificationTypeFilter> optionalType() {
        return Optional.ofNullable(type);
    }

    public NotificationTypeFilter type() {
        if (type == null) {
            setType(new NotificationTypeFilter());
        }
        return type;
    }

    public void setType(NotificationTypeFilter type) {
        this.type = type;
    }

    public StringFilter getTitle() {
        return title;
    }

    public Optional<StringFilter> optionalTitle() {
        return Optional.ofNullable(title);
    }

    public StringFilter title() {
        if (title == null) {
            setTitle(new StringFilter());
        }
        return title;
    }

    public void setTitle(StringFilter title) {
        this.title = title;
    }

    public BooleanFilter getIsRead() {
        return isRead;
    }

    public Optional<BooleanFilter> optionalIsRead() {
        return Optional.ofNullable(isRead);
    }

    public BooleanFilter isRead() {
        if (isRead == null) {
            setIsRead(new BooleanFilter());
        }
        return isRead;
    }

    public void setIsRead(BooleanFilter isRead) {
        this.isRead = isRead;
    }

    public InstantFilter getCreatedAt() {
        return createdAt;
    }

    public Optional<InstantFilter> optionalCreatedAt() {
        return Optional.ofNullable(createdAt);
    }

    public InstantFilter createdAt() {
        if (createdAt == null) {
            setCreatedAt(new InstantFilter());
        }
        return createdAt;
    }

    public void setCreatedAt(InstantFilter createdAt) {
        this.createdAt = createdAt;
    }

    public LongFilter getRecipientUserId() {
        return recipientUserId;
    }

    public Optional<LongFilter> optionalRecipientUserId() {
        return Optional.ofNullable(recipientUserId);
    }

    public LongFilter recipientUserId() {
        if (recipientUserId == null) {
            setRecipientUserId(new LongFilter());
        }
        return recipientUserId;
    }

    public void setRecipientUserId(LongFilter recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public LongFilter getSenderUserId() {
        return senderUserId;
    }

    public Optional<LongFilter> optionalSenderUserId() {
        return Optional.ofNullable(senderUserId);
    }

    public LongFilter senderUserId() {
        if (senderUserId == null) {
            setSenderUserId(new LongFilter());
        }
        return senderUserId;
    }

    public void setSenderUserId(LongFilter senderUserId) {
        this.senderUserId = senderUserId;
    }

    public LongFilter getRelatedPatientId() {
        return relatedPatientId;
    }

    public Optional<LongFilter> optionalRelatedPatientId() {
        return Optional.ofNullable(relatedPatientId);
    }

    public LongFilter relatedPatientId() {
        if (relatedPatientId == null) {
            setRelatedPatientId(new LongFilter());
        }
        return relatedPatientId;
    }

    public void setRelatedPatientId(LongFilter relatedPatientId) {
        this.relatedPatientId = relatedPatientId;
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
        final NotificationCriteria that = (NotificationCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(type, that.type) &&
            Objects.equals(title, that.title) &&
            Objects.equals(isRead, that.isRead) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(recipientUserId, that.recipientUserId) &&
            Objects.equals(senderUserId, that.senderUserId) &&
            Objects.equals(relatedPatientId, that.relatedPatientId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, title, isRead, createdAt, recipientUserId, senderUserId, relatedPatientId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NotificationCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalType().map(f -> "type=" + f + ", ").orElse("") +
            optionalTitle().map(f -> "title=" + f + ", ").orElse("") +
            optionalIsRead().map(f -> "isRead=" + f + ", ").orElse("") +
            optionalCreatedAt().map(f -> "createdAt=" + f + ", ").orElse("") +
            optionalRecipientUserId().map(f -> "recipientUserId=" + f + ", ").orElse("") +
            optionalSenderUserId().map(f -> "senderUserId=" + f + ", ").orElse("") +
            optionalRelatedPatientId().map(f -> "relatedPatientId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
