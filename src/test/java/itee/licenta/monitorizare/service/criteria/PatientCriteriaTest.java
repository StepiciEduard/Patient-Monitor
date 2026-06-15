package itee.licenta.monitorizare.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class PatientCriteriaTest {

    @Test
    void newPatientCriteriaHasAllFiltersNullTest() {
        var patientCriteria = new PatientCriteria();
        assertThat(patientCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void patientCriteriaFluentMethodsCreatesFiltersTest() {
        var patientCriteria = new PatientCriteria();

        setAllFilters(patientCriteria);

        assertThat(patientCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void patientCriteriaCopyCreatesNullFilterTest() {
        var patientCriteria = new PatientCriteria();
        var copy = patientCriteria.copy();

        assertThat(patientCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(patientCriteria)
        );
    }

    @Test
    void patientCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var patientCriteria = new PatientCriteria();
        setAllFilters(patientCriteria);

        var copy = patientCriteria.copy();

        assertThat(patientCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(patientCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var patientCriteria = new PatientCriteria();

        assertThat(patientCriteria).hasToString("PatientCriteria{}");
    }

    private static void setAllFilters(PatientCriteria patientCriteria) {
        patientCriteria.id();
        patientCriteria.cnp();
        patientCriteria.phoneNumber();
        patientCriteria.address();
        patientCriteria.patientType();
        patientCriteria.patientSubtype();
        patientCriteria.dateOfBirth();
        patientCriteria.gender();
        patientCriteria.hba1c();
        patientCriteria.bmi();
        patientCriteria.fev1Baseline();
        patientCriteria.userId();
        patientCriteria.doctorId();
        patientCriteria.distinct();
    }

    private static Condition<PatientCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getCnp()) &&
                condition.apply(criteria.getPhoneNumber()) &&
                condition.apply(criteria.getAddress()) &&
                condition.apply(criteria.getPatientType()) &&
                condition.apply(criteria.getPatientSubtype()) &&
                condition.apply(criteria.getDateOfBirth()) &&
                condition.apply(criteria.getGender()) &&
                condition.apply(criteria.getHba1c()) &&
                condition.apply(criteria.getBmi()) &&
                condition.apply(criteria.getFev1Baseline()) &&
                condition.apply(criteria.getUserId()) &&
                condition.apply(criteria.getDoctorId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<PatientCriteria> copyFiltersAre(PatientCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getCnp(), copy.getCnp()) &&
                condition.apply(criteria.getPhoneNumber(), copy.getPhoneNumber()) &&
                condition.apply(criteria.getAddress(), copy.getAddress()) &&
                condition.apply(criteria.getPatientType(), copy.getPatientType()) &&
                condition.apply(criteria.getPatientSubtype(), copy.getPatientSubtype()) &&
                condition.apply(criteria.getDateOfBirth(), copy.getDateOfBirth()) &&
                condition.apply(criteria.getGender(), copy.getGender()) &&
                condition.apply(criteria.getHba1c(), copy.getHba1c()) &&
                condition.apply(criteria.getBmi(), copy.getBmi()) &&
                condition.apply(criteria.getFev1Baseline(), copy.getFev1Baseline()) &&
                condition.apply(criteria.getUserId(), copy.getUserId()) &&
                condition.apply(criteria.getDoctorId(), copy.getDoctorId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
