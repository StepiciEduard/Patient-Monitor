package itee.licenta.monitorizare.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class AppointmentSlotCriteriaTest {

    @Test
    void newAppointmentSlotCriteriaHasAllFiltersNullTest() {
        var appointmentSlotCriteria = new AppointmentSlotCriteria();
        assertThat(appointmentSlotCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void appointmentSlotCriteriaFluentMethodsCreatesFiltersTest() {
        var appointmentSlotCriteria = new AppointmentSlotCriteria();

        setAllFilters(appointmentSlotCriteria);

        assertThat(appointmentSlotCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void appointmentSlotCriteriaCopyCreatesNullFilterTest() {
        var appointmentSlotCriteria = new AppointmentSlotCriteria();
        var copy = appointmentSlotCriteria.copy();

        assertThat(appointmentSlotCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(appointmentSlotCriteria)
        );
    }

    @Test
    void appointmentSlotCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var appointmentSlotCriteria = new AppointmentSlotCriteria();
        setAllFilters(appointmentSlotCriteria);

        var copy = appointmentSlotCriteria.copy();

        assertThat(appointmentSlotCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(appointmentSlotCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var appointmentSlotCriteria = new AppointmentSlotCriteria();

        assertThat(appointmentSlotCriteria).hasToString("AppointmentSlotCriteria{}");
    }

    private static void setAllFilters(AppointmentSlotCriteria appointmentSlotCriteria) {
        appointmentSlotCriteria.id();
        appointmentSlotCriteria.startTime();
        appointmentSlotCriteria.endTime();
        appointmentSlotCriteria.isAvailable();
        appointmentSlotCriteria.doctorId();
        appointmentSlotCriteria.distinct();
    }

    private static Condition<AppointmentSlotCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getStartTime()) &&
                condition.apply(criteria.getEndTime()) &&
                condition.apply(criteria.getIsAvailable()) &&
                condition.apply(criteria.getDoctorId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<AppointmentSlotCriteria> copyFiltersAre(
        AppointmentSlotCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getStartTime(), copy.getStartTime()) &&
                condition.apply(criteria.getEndTime(), copy.getEndTime()) &&
                condition.apply(criteria.getIsAvailable(), copy.getIsAvailable()) &&
                condition.apply(criteria.getDoctorId(), copy.getDoctorId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
