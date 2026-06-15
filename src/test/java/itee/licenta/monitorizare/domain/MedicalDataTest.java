package itee.licenta.monitorizare.domain;

import static itee.licenta.monitorizare.domain.MedicalDataTestSamples.*;
import static itee.licenta.monitorizare.domain.PatientTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import itee.licenta.monitorizare.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MedicalDataTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(MedicalData.class);
        MedicalData medicalData1 = getMedicalDataSample1();
        MedicalData medicalData2 = new MedicalData();
        assertThat(medicalData1).isNotEqualTo(medicalData2);

        medicalData2.setId(medicalData1.getId());
        assertThat(medicalData1).isEqualTo(medicalData2);

        medicalData2 = getMedicalDataSample2();
        assertThat(medicalData1).isNotEqualTo(medicalData2);
    }

    @Test
    void patientTest() {
        MedicalData medicalData = getMedicalDataRandomSampleGenerator();
        Patient patientBack = getPatientRandomSampleGenerator();

        medicalData.setPatient(patientBack);
        assertThat(medicalData.getPatient()).isEqualTo(patientBack);

        medicalData.patient(null);
        assertThat(medicalData.getPatient()).isNull();
    }
}
