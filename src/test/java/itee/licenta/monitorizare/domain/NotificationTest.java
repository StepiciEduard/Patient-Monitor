package itee.licenta.monitorizare.domain;

import static itee.licenta.monitorizare.domain.NotificationTestSamples.*;
import static itee.licenta.monitorizare.domain.PatientTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import itee.licenta.monitorizare.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class NotificationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Notification.class);
        Notification notification1 = getNotificationSample1();
        Notification notification2 = new Notification();
        assertThat(notification1).isNotEqualTo(notification2);

        notification2.setId(notification1.getId());
        assertThat(notification1).isEqualTo(notification2);

        notification2 = getNotificationSample2();
        assertThat(notification1).isNotEqualTo(notification2);
    }

    @Test
    void relatedPatientTest() {
        Notification notification = getNotificationRandomSampleGenerator();
        Patient patientBack = getPatientRandomSampleGenerator();

        notification.setRelatedPatient(patientBack);
        assertThat(notification.getRelatedPatient()).isEqualTo(patientBack);

        notification.relatedPatient(null);
        assertThat(notification.getRelatedPatient()).isNull();
    }
}
