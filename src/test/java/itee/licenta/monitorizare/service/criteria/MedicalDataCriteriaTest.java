package itee.licenta.monitorizare.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class MedicalDataCriteriaTest {

    @Test
    void newMedicalDataCriteriaHasAllFiltersNullTest() {
        var medicalDataCriteria = new MedicalDataCriteria();
        assertThat(medicalDataCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void medicalDataCriteriaFluentMethodsCreatesFiltersTest() {
        var medicalDataCriteria = new MedicalDataCriteria();

        setAllFilters(medicalDataCriteria);

        assertThat(medicalDataCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void medicalDataCriteriaCopyCreatesNullFilterTest() {
        var medicalDataCriteria = new MedicalDataCriteria();
        var copy = medicalDataCriteria.copy();

        assertThat(medicalDataCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(medicalDataCriteria)
        );
    }

    @Test
    void medicalDataCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var medicalDataCriteria = new MedicalDataCriteria();
        setAllFilters(medicalDataCriteria);

        var copy = medicalDataCriteria.copy();

        assertThat(medicalDataCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(medicalDataCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var medicalDataCriteria = new MedicalDataCriteria();

        assertThat(medicalDataCriteria).hasToString("MedicalDataCriteria{}");
    }

    private static void setAllFilters(MedicalDataCriteria medicalDataCriteria) {
        medicalDataCriteria.id();
        medicalDataCriteria.timestamp();
        medicalDataCriteria.heartRate();
        medicalDataCriteria.spo2();
        medicalDataCriteria.temperature();
        medicalDataCriteria.systolicBp();
        medicalDataCriteria.diastolicBp();
        medicalDataCriteria.hrv();
        medicalDataCriteria.qtInterval();
        medicalDataCriteria.bnp();
        medicalDataCriteria.bloodGlucose();
        medicalDataCriteria.respiratoryRate();
        medicalDataCriteria.fev1();
        medicalDataCriteria.etco2();
        medicalDataCriteria.anomalyScore();
        medicalDataCriteria.isAnomaly();
        medicalDataCriteria.patientId();
        medicalDataCriteria.distinct();
    }

    private static Condition<MedicalDataCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getTimestamp()) &&
                condition.apply(criteria.getHeartRate()) &&
                condition.apply(criteria.getSpo2()) &&
                condition.apply(criteria.getTemperature()) &&
                condition.apply(criteria.getSystolicBp()) &&
                condition.apply(criteria.getDiastolicBp()) &&
                condition.apply(criteria.getHrv()) &&
                condition.apply(criteria.getQtInterval()) &&
                condition.apply(criteria.getBnp()) &&
                condition.apply(criteria.getBloodGlucose()) &&
                condition.apply(criteria.getRespiratoryRate()) &&
                condition.apply(criteria.getFev1()) &&
                condition.apply(criteria.getEtco2()) &&
                condition.apply(criteria.getAnomalyScore()) &&
                condition.apply(criteria.getIsAnomaly()) &&
                condition.apply(criteria.getPatientId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<MedicalDataCriteria> copyFiltersAre(MedicalDataCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getTimestamp(), copy.getTimestamp()) &&
                condition.apply(criteria.getHeartRate(), copy.getHeartRate()) &&
                condition.apply(criteria.getSpo2(), copy.getSpo2()) &&
                condition.apply(criteria.getTemperature(), copy.getTemperature()) &&
                condition.apply(criteria.getSystolicBp(), copy.getSystolicBp()) &&
                condition.apply(criteria.getDiastolicBp(), copy.getDiastolicBp()) &&
                condition.apply(criteria.getHrv(), copy.getHrv()) &&
                condition.apply(criteria.getQtInterval(), copy.getQtInterval()) &&
                condition.apply(criteria.getBnp(), copy.getBnp()) &&
                condition.apply(criteria.getBloodGlucose(), copy.getBloodGlucose()) &&
                condition.apply(criteria.getRespiratoryRate(), copy.getRespiratoryRate()) &&
                condition.apply(criteria.getFev1(), copy.getFev1()) &&
                condition.apply(criteria.getEtco2(), copy.getEtco2()) &&
                condition.apply(criteria.getAnomalyScore(), copy.getAnomalyScore()) &&
                condition.apply(criteria.getIsAnomaly(), copy.getIsAnomaly()) &&
                condition.apply(criteria.getPatientId(), copy.getPatientId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
