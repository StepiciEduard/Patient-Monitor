package itee.licenta.monitorizare.domain;

import static itee.licenta.monitorizare.domain.AppointmentSlotTestSamples.*;
import static itee.licenta.monitorizare.domain.DoctorTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import itee.licenta.monitorizare.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AppointmentSlotTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AppointmentSlot.class);
        AppointmentSlot appointmentSlot1 = getAppointmentSlotSample1();
        AppointmentSlot appointmentSlot2 = new AppointmentSlot();
        assertThat(appointmentSlot1).isNotEqualTo(appointmentSlot2);

        appointmentSlot2.setId(appointmentSlot1.getId());
        assertThat(appointmentSlot1).isEqualTo(appointmentSlot2);

        appointmentSlot2 = getAppointmentSlotSample2();
        assertThat(appointmentSlot1).isNotEqualTo(appointmentSlot2);
    }

    @Test
    void doctorTest() {
        AppointmentSlot appointmentSlot = getAppointmentSlotRandomSampleGenerator();
        Doctor doctorBack = getDoctorRandomSampleGenerator();

        appointmentSlot.setDoctor(doctorBack);
        assertThat(appointmentSlot.getDoctor()).isEqualTo(doctorBack);

        appointmentSlot.doctor(null);
        assertThat(appointmentSlot.getDoctor()).isNull();
    }
}
