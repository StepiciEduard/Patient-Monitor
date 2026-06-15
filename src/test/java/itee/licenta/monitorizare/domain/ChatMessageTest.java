package itee.licenta.monitorizare.domain;

import static itee.licenta.monitorizare.domain.ChatMessageTestSamples.*;
import static itee.licenta.monitorizare.domain.PatientTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import itee.licenta.monitorizare.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ChatMessageTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ChatMessage.class);
        ChatMessage chatMessage1 = getChatMessageSample1();
        ChatMessage chatMessage2 = new ChatMessage();
        assertThat(chatMessage1).isNotEqualTo(chatMessage2);

        chatMessage2.setId(chatMessage1.getId());
        assertThat(chatMessage1).isEqualTo(chatMessage2);

        chatMessage2 = getChatMessageSample2();
        assertThat(chatMessage1).isNotEqualTo(chatMessage2);
    }

    @Test
    void patientTest() {
        ChatMessage chatMessage = getChatMessageRandomSampleGenerator();
        Patient patientBack = getPatientRandomSampleGenerator();

        chatMessage.setPatient(patientBack);
        assertThat(chatMessage.getPatient()).isEqualTo(patientBack);

        chatMessage.patient(null);
        assertThat(chatMessage.getPatient()).isNull();
    }
}
