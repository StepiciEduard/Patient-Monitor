package itee.licenta.monitorizare.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class AppointmentCriteriaTest {

    @Test
    void newAppointmentCriteriaHasAllFiltersNullTest() {
        var appointmentCriteria = new AppointmentCriteria();
        assertThat(appointmentCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void appointmentCriteriaFluentMethodsCreatesFiltersTest() {
        var appointmentCriteria = new AppointmentCriteria();

        setAllFilters(appointmentCriteria);

        assertThat(appointmentCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void appointmentCriteriaCopyCreatesNullFilterTest() {
        var appointmentCriteria = new AppointmentCriteria();
        var copy = appointmentCriteria.copy();

        assertThat(appointmentCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(appointmentCriteria)
        );
    }

    @Test
    void appointmentCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var appointmentCriteria = new AppointmentCriteria();
        setAllFilters(appointmentCriteria);

        var copy = appointmentCriteria.copy();

        assertThat(appointmentCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(appointmentCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var appointmentCriteria = new AppointmentCriteria();

        assertThat(appointmentCriteria).hasToString("AppointmentCriteria{}");
    }

    private static void setAllFilters(AppointmentCriteria appointmentCriteria) {
        appointmentCriteria.id();
        appointmentCriteria.status();
        appointmentCriteria.createdAt();
        appointmentCriteria.slotId();
        appointmentCriteria.patientId();
        appointmentCriteria.doctorId();
        appointmentCriteria.distinct();
    }

    private static Condition<AppointmentCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getStatus()) &&
                condition.apply(criteria.getCreatedAt()) &&
                condition.apply(criteria.getSlotId()) &&
                condition.apply(criteria.getPatientId()) &&
                condition.apply(criteria.getDoctorId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<AppointmentCriteria> copyFiltersAre(AppointmentCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getStatus(), copy.getStatus()) &&
                condition.apply(criteria.getCreatedAt(), copy.getCreatedAt()) &&
                condition.apply(criteria.getSlotId(), copy.getSlotId()) &&
                condition.apply(criteria.getPatientId(), copy.getPatientId()) &&
                condition.apply(criteria.getDoctorId(), copy.getDoctorId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
