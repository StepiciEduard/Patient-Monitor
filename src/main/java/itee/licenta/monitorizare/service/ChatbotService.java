package itee.licenta.monitorizare.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import itee.licenta.monitorizare.config.AnthropicProperties;
import itee.licenta.monitorizare.domain.*;
import itee.licenta.monitorizare.repository.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChatbotService {

    private static final Logger LOG = LoggerFactory.getLogger(ChatbotService.class);
    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-sonnet-4-20250514";

    private final AnthropicProperties anthropicProperties;
    private final PatientRepository patientRepository;
    private final MedicalDataRepository medicalDataRepository;
    private final NotificationRepository notificationRepository;
    private final AppointmentSlotRepository slotRepository;
    private final DoctorRepository doctorRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public ChatbotService(
        AnthropicProperties anthropicProperties,
        PatientRepository patientRepository,
        MedicalDataRepository medicalDataRepository,
        NotificationRepository notificationRepository,
        AppointmentSlotRepository slotRepository,
        DoctorRepository doctorRepository,
        ObjectMapper objectMapper
    ) {
        this.anthropicProperties = anthropicProperties;
        this.patientRepository = patientRepository;
        this.medicalDataRepository = medicalDataRepository;
        this.notificationRepository = notificationRepository;
        this.slotRepository = slotRepository;
        this.doctorRepository = doctorRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public String chatAsPatient(String userLogin, String userMessage, List<Map<String, String>> conversationHistory) {
        Optional<Patient> patientOpt = patientRepository.findByUserLogin(userLogin);
        if (patientOpt.isEmpty()) {
            return "Nu am putut gasi datele pacientului. Asigurati-va ca sunteti autentificat corect.";
        }

        Patient patient = patientOpt.get();

        String patientContext = buildPatientContext(patient);
        String systemPrompt = buildPatientSystemPrompt(patient, patientContext);

        return callAnthropicApi(systemPrompt, userMessage, conversationHistory);
    }

    public String chatAsDoctor(String doctorLogin, String userMessage, List<Map<String, String>> conversationHistory) {
        String lowerMsg = userMessage.toLowerCase();
        if (isSlotCreationRequest(lowerMsg)) {
            return handleSlotCreation(doctorLogin, userMessage);
        }

        String doctorContext = buildDoctorContext(doctorLogin);
        String systemPrompt = buildDoctorSystemPrompt(doctorLogin, doctorContext);

        return callAnthropicApi(systemPrompt, userMessage, conversationHistory);
    }

    private boolean isSlotCreationRequest(String lowerMsg) {
        boolean hasAction =
            lowerMsg.contains("crea") ||
            lowerMsg.contains("fa ") ||
            lowerMsg.contains("fa-mi") ||
            lowerMsg.contains("genereaz") ||
            lowerMsg.contains("adaug") ||
            lowerMsg.contains("seteaz") ||
            lowerMsg.contains("pune") ||
            lowerMsg.contains("fă") ||
            lowerMsg.contains("vreau slot");
        boolean hasTarget = lowerMsg.contains("slot") || lowerMsg.contains("programar") || lowerMsg.contains("disponibil");
        return hasAction && hasTarget;
    }

    private String handleSlotCreation(String doctorLogin, String userMessage) {
        Optional<Doctor> doctorOpt = doctorRepository.findByUserLogin(doctorLogin);
        if (doctorOpt.isEmpty()) {
            return "Nu am putut gasi contul de doctor.";
        }

        Doctor doctor = doctorOpt.get();

        LocalDate today = LocalDate.now();

        String parsePrompt =
            "Esti un parser de programari medicale. " +
            "Data de azi este: " +
            today +
            " (zi a saptamanii: " +
            today.getDayOfWeek() +
            "). " +
            "Utilizatorul vrea sa creeze sloturi de programare. Parseaza cererea si returneaza DOAR un JSON valid, fara alte explicatii, fara backticks markdown. " +
            "Formatul JSON:\n" +
            "{\"days\":[\"MONDAY\",\"WEDNESDAY\",\"FRIDAY\"],\"startHour\":9,\"startMinute\":0,\"endHour\":14,\"endMinute\":30,\"slotDurationMinutes\":30,\"weeksAhead\":4}\n\n" +
            "Zilele in engleza: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY.\n" +
            "Daca nu se specifica durata slotului, foloseste 30 minute.\n" +
            "Daca nu se specifica numarul de saptamani, foloseste 4.\n" +
            "Returneaza DOAR JSON-ul, nimic altceva. Fara ``` sau alte caractere.";

        String jsonResponse = callAnthropicApi(parsePrompt, userMessage, null);

        try {
            String cleanJson = jsonResponse.trim();
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            }
            // Remove any text before first { and after last }
            int firstBrace = cleanJson.indexOf('{');
            int lastBrace = cleanJson.lastIndexOf('}');
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                cleanJson = cleanJson.substring(firstBrace, lastBrace + 1);
            }

            JsonNode parsed = objectMapper.readTree(cleanJson);

            List<String> days = new ArrayList<>();
            for (JsonNode day : parsed.get("days")) {
                days.add(day.asText());
            }
            int startHour = parsed.get("startHour").asInt();
            int startMinute = parsed.has("startMinute") ? parsed.get("startMinute").asInt() : 0;
            int endHour = parsed.get("endHour").asInt();
            int endMinute = parsed.has("endMinute") ? parsed.get("endMinute").asInt() : 0;
            int slotDuration = parsed.has("slotDurationMinutes") ? parsed.get("slotDurationMinutes").asInt() : 30;
            int weeksAhead = parsed.has("weeksAhead") ? parsed.get("weeksAhead").asInt() : 4;

            if (days.isEmpty()) return "Nu am putut identifica zilele dorite. Incearca din nou.";
            if (slotDuration < 10 || slotDuration > 120) return "Durata slotului trebuie sa fie intre 10 si 120 de minute.";
            if (weeksAhead < 1 || weeksAhead > 12) return "Numarul de saptamani trebuie sa fie intre 1 si 12.";

            int totalSlots = 0;
            Set<Instant> existingStarts = slotRepository
                .findAll()
                .stream()
                .filter(s -> s.getDoctor().getId().equals(doctor.getId()))
                .map(AppointmentSlot::getStartTime)
                .collect(Collectors.toSet());

            for (int week = 0; week < weeksAhead; week++) {
                for (String dayName : days) {
                    DayOfWeek targetDay = DayOfWeek.valueOf(dayName);

                    // Find next occurrence of targetDay from today
                    LocalDate slotDate = today.plusDays(1);
                    while (slotDate.getDayOfWeek() != targetDay) {
                        slotDate = slotDate.plusDays(1);
                    }
                    slotDate = slotDate.plusWeeks(week);

                    LocalTime currentTime = LocalTime.of(startHour, startMinute);
                    LocalTime endTime = LocalTime.of(endHour, endMinute);

                    while (currentTime.plusMinutes(slotDuration).compareTo(endTime) <= 0) {
                        LocalDateTime slotStart = LocalDateTime.of(slotDate, currentTime);
                        LocalDateTime slotEnd = LocalDateTime.of(slotDate, currentTime.plusMinutes(slotDuration));

                        Instant startInstant = slotStart.atZone(ZoneId.systemDefault()).toInstant();
                        Instant endInstant = slotEnd.atZone(ZoneId.systemDefault()).toInstant();

                        if (!existingStarts.contains(startInstant)) {
                            AppointmentSlot slot = new AppointmentSlot();
                            slot.setStartTime(startInstant);
                            slot.setEndTime(endInstant);
                            slot.setIsAvailable(true);
                            slot.setDoctor(doctor);
                            slotRepository.save(slot);
                            existingStarts.add(startInstant);
                            totalSlots++;
                        }

                        currentTime = currentTime.plusMinutes(slotDuration);
                    }
                }
            }

            String daysRo = days.stream().map(this::dayToRomanian).collect(Collectors.joining(", "));
            LOG.info("Doctor {} created {} slots via chatbot", doctorLogin, totalSlots);

            return String.format(
                "✅ Am creat **%d sloturi** de programare!\n\n" +
                "📅 Zile: %s\n" +
                "🕐 Interval: %02d:%02d - %02d:%02d\n" +
                "⏱ Durata slot: %d minute\n" +
                "📆 Perioada: urmatoarele %d saptamani\n\n" +
                "Pacientii tai pot acum sa rezerve aceste sloturi din pagina de Programari.",
                totalSlots,
                daysRo,
                startHour,
                startMinute,
                endHour,
                endMinute,
                slotDuration,
                weeksAhead
            );
        } catch (Exception e) {
            LOG.error("Failed to parse slot creation request: {}", e.getMessage(), e);
            return (
                "Nu am putut interpreta cererea. Incearca sa fii mai specific, de exemplu:\n\n" +
                "\"Creaza sloturi pentru luni, miercuri si vineri, intre orele 9:00-14:00, cate 30 de minute, pentru urmatoarele 4 saptamani\""
            );
        }
    }

    private String dayToRomanian(String day) {
        return switch (day) {
            case "MONDAY" -> "Luni";
            case "TUESDAY" -> "Marti";
            case "WEDNESDAY" -> "Miercuri";
            case "THURSDAY" -> "Joi";
            case "FRIDAY" -> "Vineri";
            case "SATURDAY" -> "Sambata";
            case "SUNDAY" -> "Duminica";
            default -> day;
        };
    }

    private String buildPatientContext(Patient patient) {
        StringBuilder ctx = new StringBuilder();
        String patientName = patient.getUser().getFirstName() + " " + patient.getUser().getLastName();
        ctx.append("PACIENT: ").append(patientName).append("\n");
        ctx.append("Tip: ").append(patient.getPatientType()).append("\n");
        ctx.append("Subtip: ").append(patient.getPatientSubtype()).append("\n");
        ctx.append("Gen: ").append(patient.getGender()).append("\n");
        if (patient.getDateOfBirth() != null) ctx.append("Data nasterii: ").append(patient.getDateOfBirth()).append("\n");

        Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);
        List<MedicalData> recentData = medicalDataRepository.findByPatientIdAndTimestampAfterOrderByTimestampAsc(
            patient.getId(),
            oneDayAgo
        );

        if (!recentData.isEmpty()) {
            MedicalData latest = recentData.get(recentData.size() - 1);
            ctx.append("\n--- ULTIMA CITIRE (").append(latest.getTimestamp()).append(") ---\n");
            if (latest.getHeartRate() != null) ctx.append("Puls: ").append(latest.getHeartRate()).append(" bpm\n");
            if (latest.getSpo2() != null) ctx.append("SpO2: ").append(latest.getSpo2()).append("%\n");
            if (latest.getTemperature() != null) ctx.append("Temperatura: ").append(latest.getTemperature()).append(" °C\n");
            if (latest.getSystolicBp() != null) ctx
                .append("Tensiune: ")
                .append(latest.getSystolicBp())
                .append("/")
                .append(latest.getDiastolicBp())
                .append(" mmHg\n");
            if (latest.getRespiratoryRate() != null) ctx
                .append("Frecventa respiratorie: ")
                .append(latest.getRespiratoryRate())
                .append(" resp/min\n");
            if (latest.getBloodGlucose() != null) ctx.append("Glicemie: ").append(latest.getBloodGlucose()).append(" mg/dL\n");
            if (latest.getHrv() != null) ctx.append("HRV: ").append(latest.getHrv()).append(" ms\n");
            if (latest.getQtInterval() != null) ctx.append("Interval QT: ").append(latest.getQtInterval()).append(" ms\n");
            if (latest.getBnp() != null) ctx.append("BNP: ").append(latest.getBnp()).append(" pg/mL\n");
            if (latest.getFev1() != null) ctx.append("FEV1: ").append(latest.getFev1()).append("%\n");
            if (latest.getEtco2() != null) ctx.append("EtCO2: ").append(latest.getEtco2()).append(" mmHg\n");
            if (Boolean.TRUE.equals(latest.getIsAnomaly())) ctx
                .append("⚠ ANOMALIE DETECTATA (scor: ")
                .append(latest.getAnomalyScore())
                .append(")\n");

            long anomalyCount = recentData.stream().filter(md -> Boolean.TRUE.equals(md.getIsAnomaly())).count();
            ctx.append("\n--- STATISTICI 24H ---\n");
            ctx.append("Total citiri: ").append(recentData.size()).append("\n");
            ctx.append("Anomalii: ").append(anomalyCount).append("\n");

            OptionalInt minHr = recentData.stream().filter(md -> md.getHeartRate() != null).mapToInt(MedicalData::getHeartRate).min();
            OptionalInt maxHr = recentData.stream().filter(md -> md.getHeartRate() != null).mapToInt(MedicalData::getHeartRate).max();
            if (minHr.isPresent()) ctx
                .append("Puls min/max: ")
                .append(minHr.getAsInt())
                .append("/")
                .append(maxHr.getAsInt())
                .append(" bpm\n");
        } else {
            ctx.append("\nNu exista date medicale recente.\n");
        }

        List<Notification> notifications = notificationRepository.findByRecipientUserIsCurrentUser();
        if (!notifications.isEmpty()) {
            ctx.append("\n--- ULTIMELE ALERTE ---\n");
            notifications
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .forEach(n -> ctx.append("- [").append(n.getType()).append("] ").append(n.getTitle()).append("\n"));
        }

        return ctx.toString();
    }

    private String buildPatientSystemPrompt(Patient patient, String patientContext) {
        return (
            "Esti un asistent medical AI integrat in aplicatia Patient Monitor. " +
            "Vorbesti cu un pacient si ai acces la datele lui medicale in timp real. " +
            "Raspunzi DOAR in limba romana. " +
            "Esti prietenos, empatic, dar profesional. " +
            "Poti oferi sfaturi generale de sanatate, poti explica ce inseamna valorile medicale, " +
            "poti interpreta alertele si anomaliile. " +
            "NU poti prescrie medicamente - recomanzi mereu consultul cu medicul pentru decizii medicale. " +
            "NU ai acces la datele altor pacienti. " +
            "Raspunsurile tale sunt concise (max 3-4 paragrafe). " +
            "Foloseste emoji-uri medicale unde e potrivit (💊 🩺 ❤️ 🫁 🩸). " +
            "\n\nDATELE PACIENTULUI CURENT:\n" +
            patientContext
        );
    }

    private String buildDoctorContext(String doctorLogin) {
        StringBuilder ctx = new StringBuilder();
        List<Patient> patients = patientRepository.findByDoctorUserLogin(doctorLogin);
        ctx.append("NUMAR PACIENTI: ").append(patients.size()).append("\n\n");

        Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);

        for (Patient patient : patients) {
            String name = patient.getUser().getFirstName() + " " + patient.getUser().getLastName();
            ctx.append("PACIENT: ").append(name);
            ctx.append(" | Tip: ").append(patient.getPatientType());

            List<MedicalData> recentData = medicalDataRepository.findByPatientIdAndTimestampAfterOrderByTimestampAsc(
                patient.getId(),
                oneDayAgo
            );

            if (!recentData.isEmpty()) {
                MedicalData latest = recentData.get(recentData.size() - 1);
                ctx.append(" | Puls: ").append(latest.getHeartRate());
                ctx.append(" | SpO2: ").append(latest.getSpo2());
                ctx.append(" | Temp: ").append(latest.getTemperature());
                if (latest.getSystolicBp() != null) ctx
                    .append(" | TA: ")
                    .append(latest.getSystolicBp())
                    .append("/")
                    .append(latest.getDiastolicBp());
                long anomalies = recentData.stream().filter(md -> Boolean.TRUE.equals(md.getIsAnomaly())).count();
                if (anomalies > 0) ctx.append(" | ⚠ ").append(anomalies).append(" anomalii");
            } else {
                ctx.append(" | Fara date recente");
            }
            ctx.append("\n");
        }

        return ctx.toString();
    }

    private String buildDoctorSystemPrompt(String doctorLogin, String doctorContext) {
        return (
            "Esti un asistent medical AI integrat in aplicatia Patient Monitor. " +
            "Vorbesti cu un medic si ai acces la datele pacientilor lui. " +
            "Raspunzi DOAR in limba romana. " +
            "Esti profesional si concis. " +
            "Poti sumariza starea pacientilor, identifica tendinte, semnala anomalii, " +
            "sugera ce pacienti necesita atentie imediata, si oferi recomandari clinice. " +
            "Ai acces DOAR la pacientii acestui medic. " +
            "Raspunsurile tale sunt concise si structurate. " +
            "De asemenea, poti crea sloturi de programare - daca medicul doreste sa creeze sloturi, " +
            "spune-i sa foloseasca o comanda de genul: 'Creaza sloturi pentru luni si miercuri, 9:00-14:00, cate 30 min, 4 saptamani'. " +
            "\n\nDATELE PACIENTILOR:\n" +
            doctorContext
        );
    }

    private String callAnthropicApi(String systemPrompt, String userMessage, List<Map<String, String>> conversationHistory) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            if (conversationHistory != null) {
                for (Map<String, String> msg : conversationHistory) {
                    messages.add(Map.of("role", msg.get("role"), "content", msg.get("content")));
                }
            }
            messages.add(Map.of("role", "user", "content", userMessage));

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", MODEL);
            requestBody.put("max_tokens", 1024);
            requestBody.put("system", systemPrompt);
            requestBody.put("messages", messages);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ANTHROPIC_API_URL))
                .header("Content-Type", "application/json")
                .header("x-api-key", anthropicProperties.getApiKey())
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode content = root.get("content");
                if (content != null && content.isArray() && content.size() > 0) {
                    return content.get(0).get("text").asText();
                }
                return "Nu am putut genera un raspuns.";
            } else {
                LOG.error("Anthropic API error: {} - {}", response.statusCode(), response.body());
                return "Eroare la comunicarea cu asistentul AI. Incercati din nou.";
            }
        } catch (Exception e) {
            LOG.error("Error calling Anthropic API: {}", e.getMessage(), e);
            return "Eroare tehnica. Incercati din nou mai tarziu.";
        }
    }
}
