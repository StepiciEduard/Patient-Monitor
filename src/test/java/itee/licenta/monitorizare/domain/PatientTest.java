package itee.licenta.monitorizare.domain;

import static itee.licenta.monitorizare.domain.DoctorTestSamples.*;
import static itee.licenta.monitorizare.domain.PatientTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import itee.licenta.monitorizare.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PatientTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Patient.class);
        Patient patient1 = getPatientSample1();
        Patient patient2 = new Patient();
        assertThat(patient1).isNotEqualTo(patient2);

        patient2.setId(patient1.getId());
        assertThat(patient1).isEqualTo(patient2);

        patient2 = getPatientSample2();
        assertThat(patient1).isNotEqualTo(patient2);
    }

    @Test
    void doctorTest() {
        Patient patient = getPatientRandomSampleGenerator();
        Doctor doctorBack = getDoctorRandomSampleGenerator();

        patient.setDoctor(doctorBack);
        assertThat(patient.getDoctor()).isEqualTo(doctorBack);

        patient.doctor(null);
        assertThat(patient.getDoctor()).isNull();
    }
}
