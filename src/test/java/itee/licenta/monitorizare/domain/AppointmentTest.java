package itee.licenta.monitorizare.domain;

import static itee.licenta.monitorizare.domain.AppointmentSlotTestSamples.*;
import static itee.licenta.monitorizare.domain.AppointmentTestSamples.*;
import static itee.licenta.monitorizare.domain.DoctorTestSamples.*;
import static itee.licenta.monitorizare.domain.PatientTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import itee.licenta.monitorizare.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AppointmentTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Appointment.class);
        Appointment appointment1 = getAppointmentSample1();
        Appointment appointment2 = new Appointment();
        assertThat(appointment1).isNotEqualTo(appointment2);

        appointment2.setId(appointment1.getId());
        assertThat(appointment1).isEqualTo(appointment2);

        appointment2 = getAppointmentSample2();
        assertThat(appointment1).isNotEqualTo(appointment2);
    }

    @Test
    void slotTest() {
        Appointment appointment = getAppointmentRandomSampleGenerator();
        AppointmentSlot appointmentSlotBack = getAppointmentSlotRandomSampleGenerator();

        appointment.setSlot(appointmentSlotBack);
        assertThat(appointment.getSlot()).isEqualTo(appointmentSlotBack);

        appointment.slot(null);
        assertThat(appointment.getSlot()).isNull();
    }

    @Test
    void patientTest() {
        Appointment appointment = getAppointmentRandomSampleGenerator();
        Patient patientBack = getPatientRandomSampleGenerator();

        appointment.setPatient(patientBack);
        assertThat(appointment.getPatient()).isEqualTo(patientBack);

        appointment.patient(null);
        assertThat(appointment.getPatient()).isNull();
    }

    @Test
    void doctorTest() {
        Appointment appointment = getAppointmentRandomSampleGenerator();
        Doctor doctorBack = getDoctorRandomSampleGenerator();

        appointment.setDoctor(doctorBack);
        assertThat(appointment.getDoctor()).isEqualTo(doctorBack);

        appointment.doctor(null);
        assertThat(appointment.getDoctor()).isNull();
    }
}
