package itee.licenta.monitorizare.service;

import itee.licenta.monitorizare.domain.MedicalData;
import itee.licenta.monitorizare.domain.Notification;
import itee.licenta.monitorizare.domain.Patient;
import itee.licenta.monitorizare.domain.User;
import itee.licenta.monitorizare.domain.enumeration.NotificationType;
import itee.licenta.monitorizare.domain.enumeration.PatientType;
import itee.licenta.monitorizare.repository.NotificationRepository;
import itee.licenta.monitorizare.web.websocket.PatientMonitorWebSocketService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ThresholdAlertService {

    private static final Logger LOG = LoggerFactory.getLogger(ThresholdAlertService.class);

    private final NotificationRepository notificationRepository;
    private final PatientMonitorWebSocketService webSocketService;
    private final MailService mailService;

    // Email cooldown: key = "userId_notifType_patientId_date", prevents spam
    private final Map<String, Boolean> emailCooldownMap = new ConcurrentHashMap<>();

    public ThresholdAlertService(
        NotificationRepository notificationRepository,
        PatientMonitorWebSocketService webSocketService,
        MailService mailService
    ) {
        this.notificationRepository = notificationRepository;
        this.webSocketService = webSocketService;
        this.mailService = mailService;
    }

    public void checkAndAlert(MedicalData data) {
        Patient patient = data.getPatient();
        List<String> alerts = new ArrayList<>();

        if (data.getHeartRate() != null) {
            if (data.getHeartRate() > 120) alerts.add("Tahicardie severa: puls " + data.getHeartRate() + " bpm");
            else if (data.getHeartRate() > 100) alerts.add("Tahicardie: puls " + data.getHeartRate() + " bpm");
            if (data.getHeartRate() < 50) alerts.add("Bradicardie severa: puls " + data.getHeartRate() + " bpm");
            else if (data.getHeartRate() < 60) alerts.add("Bradicardie: puls " + data.getHeartRate() + " bpm");
        }

        if (data.getSystolicBp() != null) {
            if (data.getSystolicBp() > 180) alerts.add(
                "Criza hipertensiva: TA " + data.getSystolicBp() + "/" + data.getDiastolicBp() + " mmHg"
            );
            else if (data.getSystolicBp() > 140) alerts.add(
                "Hipertensiune: TA " + data.getSystolicBp() + "/" + data.getDiastolicBp() + " mmHg"
            );
            if (data.getSystolicBp() < 90) alerts.add("Hipotensiune: TA " + data.getSystolicBp() + "/" + data.getDiastolicBp() + " mmHg");
        }

        if (data.getSpo2() != null) {
            if (data.getSpo2() < 85) alerts.add("Desaturare critica: SpO2 " + data.getSpo2() + "%");
            else if (data.getSpo2() < 90) alerts.add("Hipoxemie severa: SpO2 " + data.getSpo2() + "%");
            else if (data.getSpo2() < 93) alerts.add("Hipoxemie: SpO2 " + data.getSpo2() + "%");
        }

        if (data.getTemperature() != null) {
            if (data.getTemperature() > 38.5) alerts.add("Febra ridicata: " + data.getTemperature() + " C");
            else if (data.getTemperature() > 37.5) alerts.add("Subfebrilitate: " + data.getTemperature() + " C");
            if (data.getTemperature() < 35.5) alerts.add("Hipotermie: " + data.getTemperature() + " C");
        }

        if (data.getRespiratoryRate() != null) {
            if (data.getRespiratoryRate() > 25) alerts.add("Tahipnee: " + data.getRespiratoryRate() + " resp/min");
            if (data.getRespiratoryRate() < 10) alerts.add("Bradipnee: " + data.getRespiratoryRate() + " resp/min");
        }

        if (patient.getPatientType() == PatientType.CARDIAC) {
            if (data.getHrv() != null && data.getHrv() < 15) {
                alerts.add("HRV critic scazut: " + data.getHrv() + " ms");
            }
            if (data.getQtInterval() != null && data.getQtInterval() > 500) {
                alerts.add("Interval QT prelungit: " + data.getQtInterval() + " ms - risc aritmie");
            }
            if (data.getBnp() != null) {
                if (data.getBnp() > 2000) alerts.add("BNP critic: " + data.getBnp() + " pg/mL - insuficienta cardiaca severa");
                else if (data.getBnp() > 400) alerts.add("BNP crescut: " + data.getBnp() + " pg/mL - posibila insuficienta cardiaca");
            }
        }

        if (patient.getPatientType() == PatientType.DIABETES) {
            if (data.getBloodGlucose() != null) {
                if (data.getBloodGlucose() > 400) alerts.add(
                    "Hiperglicemie critica: " + data.getBloodGlucose() + " mg/dL - risc cetoacidoza"
                );
                else if (data.getBloodGlucose() > 250) alerts.add("Hiperglicemie severa: " + data.getBloodGlucose() + " mg/dL");
                else if (data.getBloodGlucose() > 180) alerts.add("Hiperglicemie: " + data.getBloodGlucose() + " mg/dL");
                if (data.getBloodGlucose() < 70) alerts.add(
                    "Hipoglicemie: " + data.getBloodGlucose() + " mg/dL - risc pierdere cunostinta"
                );
                else if (data.getBloodGlucose() < 54) alerts.add("Hipoglicemie severa: " + data.getBloodGlucose() + " mg/dL - urgenta!");
            }
        }

        if (patient.getPatientType() == PatientType.RESPIRATORY) {
            if (data.getFev1() != null) {
                Double baseline = patient.getFev1Baseline() != null ? patient.getFev1Baseline() : 80.0;
                double fev1Percent = (data.getFev1() / baseline) * 100;
                if (fev1Percent < 30) alerts.add(
                    "FEV1 critic: " + data.getFev1() + "% (" + String.format("%.0f", fev1Percent) + "% din baseline) - obstructie severa"
                );
                else if (fev1Percent < 50) alerts.add(
                    "FEV1 scazut: " + data.getFev1() + "% (" + String.format("%.0f", fev1Percent) + "% din baseline)"
                );
            }
            if (data.getEtco2() != null) {
                if (data.getEtco2() > 70) alerts.add("EtCO2 critic: " + data.getEtco2() + " mmHg - hipercapnie severa");
                else if (data.getEtco2() > 50) alerts.add("EtCO2 crescut: " + data.getEtco2() + " mmHg - retentie CO2");
            }
        }

        if (Boolean.TRUE.equals(data.getIsAnomaly())) {
            alerts.add("Anomalie detectata (scor: " + data.getAnomalyScore() + ") - pattern neobisnuit in semnele vitale");
        }

        if (!alerts.isEmpty()) {
            String patientName = patient.getUser() != null
                ? patient.getUser().getFirstName() + " " + patient.getUser().getLastName()
                : "Pacient #" + patient.getId();

            String title = alerts.size() == 1 ? "Alerta: " + patientName : alerts.size() + " alerte: " + patientName;

            StringBuilder message = new StringBuilder();
            message.append("Pacient: ").append(patientName).append("\n");
            message.append("Ora: ").append(data.getTimestamp()).append("\n\n");
            for (String alert : alerts) {
                message.append("⚠ ").append(alert).append("\n");
            }

            NotificationType notifType = Boolean.TRUE.equals(data.getIsAnomaly())
                ? NotificationType.ANOMALY_DETECTED
                : NotificationType.THRESHOLD_ALERT;

            // Notify doctor
            if (patient.getDoctor() != null && patient.getDoctor().getUser() != null) {
                Notification doctorNotif = createNotification(
                    notifType,
                    title,
                    message.toString(),
                    patient.getDoctor().getUser(),
                    null,
                    patient,
                    data.getTimestamp()
                );
                try {
                    webSocketService.sendNotificationUpdate(doctorNotif);
                } catch (Exception e) {
                    LOG.debug("WS: {}", e.getMessage());
                }
                sendAlertEmailWithCooldown(
                    patient.getDoctor().getUser(),
                    notifType,
                    title,
                    message.toString(),
                    patientName,
                    patient.getId()
                );
            }

            // Notify patient
            if (patient.getUser() != null) {
                Notification patientNotif = createNotification(
                    notifType,
                    title,
                    message.toString(),
                    patient.getUser(),
                    null,
                    patient,
                    data.getTimestamp()
                );
                try {
                    webSocketService.sendNotificationUpdate(patientNotif);
                } catch (Exception e) {
                    LOG.debug("WS: {}", e.getMessage());
                }
                sendAlertEmailWithCooldown(patient.getUser(), notifType, title, message.toString(), patientName, patient.getId());
            }

            LOG.warn("Generated {} alert(s) for patient {} ({}): {}", alerts.size(), patientName, patient.getPatientType(), alerts);
        }
    }

    private void sendAlertEmailWithCooldown(
        User user,
        NotificationType type,
        String title,
        String alertMessage,
        String patientName,
        Long patientId
    ) {
        if (user.getEmail() == null) return;

        String today = LocalDate.now().toString();
        String cooldownKey = user.getId() + "_" + type.name() + "_" + patientId + "_" + today;

        if (emailCooldownMap.containsKey(cooldownKey)) {
            LOG.debug("Email cooldown active for {} (type={}, patient={}), skipping", user.getLogin(), type, patientId);
            return;
        }

        // Clean old entries from previous days
        emailCooldownMap.entrySet().removeIf(entry -> !entry.getKey().endsWith("_" + today));
        emailCooldownMap.put(cooldownKey, true);

        try {
            String htmlContent = buildAlertEmailHtml(title, alertMessage, patientName);
            mailService.sendEmail(user.getEmail(), "[Patient Monitor] " + title, htmlContent, false, true);
            LOG.info("Alert email sent to {} (type={}, patient={})", user.getEmail(), type, patientName);
        } catch (Exception e) {
            LOG.warn("Failed to send alert email to {}: {}", user.getEmail(), e.getMessage());
            emailCooldownMap.remove(cooldownKey);
        }
    }

    public void sendAppointmentReminderEmail(User recipient, String doctorName, String message, Long patientId) {
        if (recipient.getEmail() == null) return;

        String today = LocalDate.now().toString();
        String cooldownKey = recipient.getId() + "_APPOINTMENT_REMINDER_" + patientId + "_" + today;

        if (emailCooldownMap.containsKey(cooldownKey)) {
            LOG.debug("Appointment email cooldown active for {}, skipping", recipient.getLogin());
            return;
        }

        emailCooldownMap.entrySet().removeIf(entry -> !entry.getKey().endsWith("_" + today));
        emailCooldownMap.put(cooldownKey, true);

        try {
            String htmlContent = buildAppointmentEmailHtml(doctorName, message, recipient.getFirstName());
            mailService.sendEmail(recipient.getEmail(), "[Patient Monitor] Recomandare control medical", htmlContent, false, true);
            LOG.info("Appointment reminder email sent to {}", recipient.getEmail());
        } catch (Exception e) {
            LOG.warn("Failed to send appointment email to {}: {}", recipient.getEmail(), e.getMessage());
            emailCooldownMap.remove(cooldownKey);
        }
    }

    private String buildAlertEmailHtml(String title, String alertMessage, String patientName) {
        String[] lines = alertMessage.split("\n");
        StringBuilder alertLines = new StringBuilder();
        for (String line : lines) {
            if (line.trim().startsWith("⚠")) {
                alertLines
                    .append("<li style=\"color: #dc3545; margin-bottom: 6px;\">")
                    .append(line.trim().substring(1).trim())
                    .append("</li>");
            }
        }

        return (
            "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>" +
            "<body style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;'>" +
            "<div style='background:#1a1a2e;color:#e0e0e0;border-radius:12px;padding:24px;'>" +
            "<div style='text-align:center;margin-bottom:20px;'>" +
            "<h1 style='color:#f87171;font-size:20px;margin:0;'>&#9888;&#65039; Alerta Medicala</h1>" +
            "<p style='color:#94a3b8;font-size:14px;margin:8px 0 0;'>Patient Monitor - Notificare automata</p></div>" +
            "<div style='background:rgba(239,68,68,0.1);border:1px solid rgba(239,68,68,0.3);border-radius:8px;padding:16px;margin-bottom:16px;'>" +
            "<h2 style='color:#fca5a5;font-size:16px;margin:0 0 8px;'>" +
            title +
            "</h2>" +
            "<p style='color:#94a3b8;font-size:13px;margin:0;'>Pacient: <strong style='color:#e0e0e0;'>" +
            patientName +
            "</strong></p></div>" +
            "<div style='margin-bottom:16px;'><h3 style='color:#f87171;font-size:14px;margin:0 0 8px;'>Alerte detectate:</h3>" +
            "<ul style='list-style:none;padding:0;margin:0;'>" +
            alertLines +
            "</ul></div>" +
            "<div style='border-top:1px solid rgba(255,255,255,0.1);padding-top:12px;text-align:center;'>" +
            "<p style='color:#64748b;font-size:12px;margin:0;'>Acest email a fost generat automat de sistemul Patient Monitor.</p>" +
            "<p style='color:#64748b;font-size:12px;margin:4px 0 0;'>Veti primi maxim un email de acest tip pe zi per pacient.</p>" +
            "</div></div></body></html>"
        );
    }

    private String buildAppointmentEmailHtml(String doctorName, String message, String patientFirstName) {
        return (
            "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>" +
            "<body style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;'>" +
            "<div style='background:#1a1a2e;color:#e0e0e0;border-radius:12px;padding:24px;'>" +
            "<div style='text-align:center;margin-bottom:20px;'>" +
            "<h1 style='color:#38bdf8;font-size:20px;margin:0;'>&#128197; Recomandare Control Medical</h1>" +
            "<p style='color:#94a3b8;font-size:14px;margin:8px 0 0;'>Patient Monitor</p></div>" +
            "<div style='background:rgba(56,189,248,0.1);border:1px solid rgba(56,189,248,0.3);border-radius:8px;padding:16px;margin-bottom:16px;'>" +
            "<p style='color:#e0e0e0;font-size:14px;margin:0 0 8px;'>Stimate/Stimata <strong>" +
            patientFirstName +
            "</strong>,</p>" +
            "<p style='color:#cbd5e1;font-size:14px;margin:0;'>" +
            message +
            "</p></div>" +
            "<div style='margin-bottom:16px;'>" +
            "<p style='color:#94a3b8;font-size:13px;margin:0;'>Medic: <strong style='color:#38bdf8;'>Dr. " +
            doctorName +
            "</strong></p></div>" +
            "<div style='border-top:1px solid rgba(255,255,255,0.1);padding-top:12px;text-align:center;'>" +
            "<p style='color:#64748b;font-size:12px;margin:0;'>Accesati aplicatia Patient Monitor pentru a programa o consultatie.</p>" +
            "</div></div></body></html>"
        );
    }

    private Notification createNotification(
        NotificationType type,
        String title,
        String message,
        User recipient,
        User sender,
        Patient patient,
        Instant timestamp
    ) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setIsRead(false);
        notification.setCreatedAt(timestamp);
        notification.setRecipientUser(recipient);
        notification.setSenderUser(sender);
        notification.setRelatedPatient(patient);
        notificationRepository.save(notification);
        return notification;
    }
}
