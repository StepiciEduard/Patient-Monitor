package itee.licenta.monitorizare.web.websocket;

import itee.licenta.monitorizare.domain.MedicalData;
import itee.licenta.monitorizare.domain.Notification;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
public class PatientMonitorWebSocketService {

    private static final Logger LOG = LoggerFactory.getLogger(PatientMonitorWebSocketService.class);

    private final SimpMessageSendingOperations messagingTemplate;

    public PatientMonitorWebSocketService(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Send new medical data to the patient's dashboard and doctor's dashboard
     */
    public void sendMedicalDataUpdate(MedicalData data, Long patientId, String patientLogin, String doctorLogin) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "MEDICAL_DATA");
        payload.put("patientId", patientId);
        payload.put("timestamp", data.getTimestamp().toString());
        payload.put("heartRate", data.getHeartRate());
        payload.put("spo2", data.getSpo2());
        payload.put("temperature", data.getTemperature());
        payload.put("systolicBp", data.getSystolicBp());
        payload.put("diastolicBp", data.getDiastolicBp());
        payload.put("isAnomaly", data.getIsAnomaly());

        // Send to patient's topic
        messagingTemplate.convertAndSend("/topic/medical-data/" + patientId, payload);

        // Send to doctor's topic
        if (doctorLogin != null) {
            messagingTemplate.convertAndSend("/topic/doctor-updates/" + doctorLogin, payload);
        }

        // Send general dashboard update
        messagingTemplate.convertAndSend("/topic/dashboard-update", Map.of("type", "MEDICAL_DATA", "patientId", patientId));

        LOG.debug("WebSocket: sent medical data update for patient {}", patientId);
    }

    /**
     * Send notification alert to the recipient
     */
    public void sendNotificationUpdate(Notification notification) {
        if (notification.getRecipientUser() == null) return;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "NOTIFICATION");
        payload.put("id", notification.getId());
        payload.put("title", notification.getTitle());
        payload.put("message", notification.getMessage());
        payload.put("notificationType", notification.getType().name());
        payload.put("createdAt", notification.getCreatedAt().toString());

        String recipientLogin = notification.getRecipientUser().getLogin();
        messagingTemplate.convertAndSend("/topic/notifications/" + recipientLogin, payload);

        LOG.debug("WebSocket: sent notification to {}", recipientLogin);
    }

    /**
     * Send admin dashboard update signal
     */
    public void sendAdminUpdate(String updateType) {
        messagingTemplate.convertAndSend("/topic/dashboard-update", Map.of("type", updateType));
        LOG.debug("WebSocket: sent admin update signal: {}", updateType);
    }
}
