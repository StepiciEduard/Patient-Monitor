package itee.licenta.monitorizare.web.rest;

import itee.licenta.monitorizare.domain.*;
import itee.licenta.monitorizare.domain.enumeration.AppointmentStatus;
import itee.licenta.monitorizare.domain.enumeration.NotificationType;
import itee.licenta.monitorizare.repository.*;
import itee.licenta.monitorizare.security.SecurityUtils;
import itee.licenta.monitorizare.service.ThresholdAlertService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor")
@Transactional
public class DoctorDashboardResource {

    private static final Logger LOG = LoggerFactory.getLogger(DoctorDashboardResource.class);

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final MedicalDataRepository medicalDataRepository;
    private final NotificationRepository notificationRepository;
    private final ThresholdAlertService thresholdAlertService;
    private final AppointmentSlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorDashboardResource(
        DoctorRepository doctorRepository,
        PatientRepository patientRepository,
        MedicalDataRepository medicalDataRepository,
        NotificationRepository notificationRepository,
        ThresholdAlertService thresholdAlertService,
        AppointmentSlotRepository slotRepository,
        AppointmentRepository appointmentRepository
    ) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.medicalDataRepository = medicalDataRepository;
        this.notificationRepository = notificationRepository;
        this.thresholdAlertService = thresholdAlertService;
        this.slotRepository = slotRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/my-patients")
    public ResponseEntity<Map<String, Object>> getMyPatients() {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("User not logged in"));

        Optional<Doctor> doctorOpt = doctorRepository.findByUserLogin(login);
        if (doctorOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Doctor doctor = doctorOpt.get();
        List<Patient> patients = patientRepository.findByDoctorUserLogin(login);

        List<Map<String, Object>> patientList = new ArrayList<>();
        Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);

        for (Patient patient : patients) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("id", patient.getId());
            p.put("firstName", patient.getUser().getFirstName());
            p.put("lastName", patient.getUser().getLastName());
            p.put("patientType", patient.getPatientType().name());
            p.put("patientSubtype", patient.getPatientSubtype().name());
            p.put("cnp", patient.getCnp());
            p.put("phoneNumber", patient.getPhoneNumber());
            p.put("gender", patient.getGender());
            p.put("dateOfBirth", patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : null);

            List<MedicalData> recentData = medicalDataRepository.findByPatientIdAndTimestampAfterOrderByTimestampAsc(
                patient.getId(),
                oneDayAgo
            );

            if (!recentData.isEmpty()) {
                MedicalData latest = recentData.get(recentData.size() - 1);
                Map<String, Object> latestReading = new LinkedHashMap<>();
                latestReading.put("timestamp", latest.getTimestamp().toString());
                latestReading.put("heartRate", latest.getHeartRate());
                latestReading.put("spo2", latest.getSpo2());
                latestReading.put("temperature", latest.getTemperature());
                latestReading.put("systolicBp", latest.getSystolicBp());
                latestReading.put("diastolicBp", latest.getDiastolicBp());
                latestReading.put("bloodGlucose", latest.getBloodGlucose());
                latestReading.put("respiratoryRate", latest.getRespiratoryRate());
                latestReading.put("isAnomaly", latest.getIsAnomaly());
                latestReading.put("anomalyScore", latest.getAnomalyScore());
                p.put("latestReading", latestReading);

                long anomalyCount = recentData.stream().filter(md -> Boolean.TRUE.equals(md.getIsAnomaly())).count();
                p.put("anomalies24h", anomalyCount);
            } else {
                p.put("latestReading", null);
                p.put("anomalies24h", 0);
            }

            p.put("status", determineStatus(patient, recentData));
            patientList.add(p);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("doctorName", "Dr. " + doctor.getUser().getLastName() + " " + doctor.getUser().getFirstName());
        response.put("specialization", doctor.getSpecialization());
        response.put("totalPatients", patients.size());
        response.put("patients", patientList);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/patient/{patientId}/dashboard-data")
    public ResponseEntity<Map<String, Object>> getPatientDashboardData(
        @PathVariable Long patientId,
        @RequestParam(defaultValue = "1d") String interval
    ) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("User not logged in"));

        Optional<Doctor> doctorOpt = doctorRepository.findByUserLogin(login);
        if (doctorOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Patient> patientOpt = patientRepository.findOneWithToOneRelationships(patientId);
        if (patientOpt.isEmpty() || !patientOpt.get().getDoctor().getId().equals(doctorOpt.get().getId())) {
            return ResponseEntity.notFound().build();
        }

        Patient patient = patientOpt.get();
        Instant now = Instant.now();
        Instant from =
            switch (interval) {
                case "1h" -> now.minus(1, ChronoUnit.HOURS);
                case "1d" -> now.minus(1, ChronoUnit.DAYS);
                case "7d" -> now.minus(7, ChronoUnit.DAYS);
                case "30d" -> now.minus(30, ChronoUnit.DAYS);
                default -> now.minus(1, ChronoUnit.DAYS);
            };

        List<MedicalData> data = medicalDataRepository.findByPatientIdAndTimestampAfterOrderByTimestampAsc(patient.getId(), from);

        Map<String, Object> response = new HashMap<>();
        response.put("patientType", patient.getPatientType().name());
        response.put("patientSubtype", patient.getPatientSubtype().name());
        response.put("patientName", patient.getUser().getFirstName() + " " + patient.getUser().getLastName());

        if (!data.isEmpty()) {
            MedicalData latest = data.get(data.size() - 1);
            response.put("latest", buildReading(latest));
        }

        List<Map<String, Object>> series = new ArrayList<>();
        for (MedicalData md : data) {
            series.add(buildReading(md));
        }
        response.put("series", series);
        response.put("totalReadings", data.size());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-notification")
    public ResponseEntity<Void> sendNotification(@RequestBody Map<String, Object> request) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("User not logged in"));

        Long patientId = ((Number) request.get("patientId")).longValue();
        String message = (String) request.getOrDefault("message", "Medicul dumneavoastra va recomanda un control medical.");

        Optional<Doctor> doctorOpt = doctorRepository.findByUserLogin(login);
        if (doctorOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Patient> patientOpt = patientRepository.findOneWithToOneRelationships(patientId);
        if (patientOpt.isEmpty() || !patientOpt.get().getDoctor().getId().equals(doctorOpt.get().getId())) {
            return ResponseEntity.notFound().build();
        }

        Doctor doctor = doctorOpt.get();
        Patient patient = patientOpt.get();

        Notification notification = new Notification();
        notification.setTitle("Recomandare control medical");
        notification.setMessage("Dr. " + doctor.getUser().getLastName() + ": " + message);
        notification.setType(NotificationType.APPOINTMENT_REMINDER);
        notification.setCreatedAt(Instant.now());
        notification.setIsRead(false);
        notification.setRecipientUser(patient.getUser());
        notification.setSenderUser(doctor.getUser());
        notification.setRelatedPatient(patient);
        notificationRepository.save(notification);

        LOG.info("Doctor {} sent notification to patient {}: {}", login, patient.getUser().getLogin(), message);

        String doctorName = doctor.getUser().getLastName() + " " + doctor.getUser().getFirstName();
        thresholdAlertService.sendAppointmentReminderEmail(patient.getUser(), doctorName, message, patient.getId());

        return ResponseEntity.ok().build();
    }

    // ===================== APPOINTMENT SLOT MANAGEMENT =====================

    @GetMapping("/my-slots")
    public ResponseEntity<List<Map<String, Object>>> getMySlots() {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow();
        Optional<Doctor> doctorOpt = doctorRepository.findByUserLogin(login);
        if (doctorOpt.isEmpty()) return ResponseEntity.notFound().build();

        Doctor doctor = doctorOpt.get();
        List<AppointmentSlot> slots = slotRepository
            .findAll()
            .stream()
            .filter(s -> s.getDoctor().getId().equals(doctor.getId()))
            .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
            .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();
        for (AppointmentSlot slot : slots) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", slot.getId());
            item.put("startTime", slot.getStartTime().toString());
            item.put("endTime", slot.getEndTime().toString());
            item.put("isAvailable", slot.getIsAvailable());

            Optional<Appointment> appt = appointmentRepository
                .findAll()
                .stream()
                .filter(a -> a.getSlot().getId().equals(slot.getId()) && a.getStatus() != AppointmentStatus.CANCELLED)
                .findFirst();

            if (appt.isPresent()) {
                Appointment a = appt.get();
                Map<String, Object> apptInfo = new LinkedHashMap<>();
                apptInfo.put("id", a.getId());
                apptInfo.put("status", a.getStatus().name());
                apptInfo.put("patientName", a.getPatient().getUser().getFirstName() + " " + a.getPatient().getUser().getLastName());
                apptInfo.put("patientId", a.getPatient().getId());
                apptInfo.put("notes", a.getNotes());
                item.put("appointment", apptInfo);
            }

            result.add(item);
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/create-slot")
    public ResponseEntity<Map<String, Object>> createSlot(@RequestBody Map<String, String> request) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow();
        Optional<Doctor> doctorOpt = doctorRepository.findByUserLogin(login);
        if (doctorOpt.isEmpty()) return ResponseEntity.notFound().build();

        Doctor doctor = doctorOpt.get();
        Instant startTime = Instant.parse(request.get("startTime"));
        Instant endTime = Instant.parse(request.get("endTime"));

        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ora de sfarsit trebuie sa fie dupa ora de inceput"));
        }

        AppointmentSlot slot = new AppointmentSlot();
        slot.setStartTime(startTime);
        slot.setEndTime(endTime);
        slot.setIsAvailable(true);
        slot.setDoctor(doctor);
        slotRepository.save(slot);

        LOG.info("Doctor {} created slot: {} - {}", login, startTime, endTime);
        return ResponseEntity.ok(Map.of("message", "Slot creat cu succes", "id", slot.getId()));
    }

    @DeleteMapping("/delete-slot/{slotId}")
    public ResponseEntity<Map<String, Object>> deleteSlot(@PathVariable Long slotId) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow();
        Optional<Doctor> doctorOpt = doctorRepository.findByUserLogin(login);
        if (doctorOpt.isEmpty()) return ResponseEntity.notFound().build();

        Optional<AppointmentSlot> slotOpt = slotRepository.findById(slotId);
        if (slotOpt.isEmpty() || !slotOpt.get().getDoctor().getId().equals(doctorOpt.get().getId())) {
            return ResponseEntity.notFound().build();
        }

        AppointmentSlot slot = slotOpt.get();

        List<Appointment> appointments = appointmentRepository
            .findAll()
            .stream()
            .filter(a -> a.getSlot().getId().equals(slotId) && a.getStatus() == AppointmentStatus.SCHEDULED)
            .collect(Collectors.toList());

        for (Appointment appt : appointments) {
            appt.setStatus(AppointmentStatus.CANCELLED);
            appointmentRepository.save(appt);

            Notification notif = new Notification();
            notif.setType(NotificationType.APPOINTMENT_REMINDER);
            notif.setTitle("Programare anulata");
            notif.setMessage("Programarea din " + slot.getStartTime() + " a fost anulata de catre medic.");
            notif.setIsRead(false);
            notif.setCreatedAt(Instant.now());
            notif.setRecipientUser(appt.getPatient().getUser());
            notif.setSenderUser(doctorOpt.get().getUser());
            notif.setRelatedPatient(appt.getPatient());
            notificationRepository.save(notif);
        }

        slotRepository.delete(slot);
        LOG.info("Doctor {} deleted slot {} (cancelled {} appointments)", login, slotId, appointments.size());
        return ResponseEntity.ok(Map.of("message", "Slot sters cu succes"));
    }

    @PutMapping("/complete-appointment/{appointmentId}")
    public ResponseEntity<Map<String, Object>> completeAppointment(@PathVariable Long appointmentId) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow();
        Optional<Doctor> doctorOpt = doctorRepository.findByUserLogin(login);
        if (doctorOpt.isEmpty()) return ResponseEntity.notFound().build();

        Optional<Appointment> apptOpt = appointmentRepository.findById(appointmentId);
        if (apptOpt.isEmpty() || !apptOpt.get().getDoctor().getId().equals(doctorOpt.get().getId())) {
            return ResponseEntity.notFound().build();
        }

        Appointment appt = apptOpt.get();
        appt.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appt);

        return ResponseEntity.ok(Map.of("message", "Programare finalizata"));
    }

    @PutMapping("/cancel-appointment/{appointmentId}")
    public ResponseEntity<Map<String, Object>> cancelAppointment(@PathVariable Long appointmentId) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow();
        Optional<Doctor> doctorOpt = doctorRepository.findByUserLogin(login);
        if (doctorOpt.isEmpty()) return ResponseEntity.notFound().build();

        Optional<Appointment> apptOpt = appointmentRepository.findById(appointmentId);
        if (apptOpt.isEmpty() || !apptOpt.get().getDoctor().getId().equals(doctorOpt.get().getId())) {
            return ResponseEntity.notFound().build();
        }

        Appointment appt = apptOpt.get();
        appt.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appt);

        appt.getSlot().setIsAvailable(true);
        slotRepository.save(appt.getSlot());

        Notification notif = new Notification();
        notif.setType(NotificationType.APPOINTMENT_REMINDER);
        notif.setTitle("Programare anulata de medic");
        notif.setMessage("Programarea din " + appt.getSlot().getStartTime() + " a fost anulata de catre medic.");
        notif.setIsRead(false);
        notif.setCreatedAt(Instant.now());
        notif.setRecipientUser(appt.getPatient().getUser());
        notif.setSenderUser(doctorOpt.get().getUser());
        notif.setRelatedPatient(appt.getPatient());
        notificationRepository.save(notif);

        return ResponseEntity.ok(Map.of("message", "Programare anulata"));
    }

    // ===================== HELPERS =====================

    private String determineStatus(Patient patient, List<MedicalData> recentData) {
        if (recentData.isEmpty()) return "NO_DATA";

        long anomalies = recentData.stream().filter(md -> Boolean.TRUE.equals(md.getIsAnomaly())).count();
        if (anomalies > 3) return "CRITICAL";

        MedicalData latest = recentData.get(recentData.size() - 1);
        if (latest.getHeartRate() != null && (latest.getHeartRate() > 120 || latest.getHeartRate() < 50)) return "ALERT";
        if (latest.getSpo2() != null && latest.getSpo2() < 90) return "CRITICAL";
        if (latest.getSpo2() != null && latest.getSpo2() < 93) return "ALERT";
        if (latest.getTemperature() != null && latest.getTemperature() > 38.5) return "ALERT";
        if (latest.getSystolicBp() != null && latest.getSystolicBp() > 180) return "CRITICAL";
        if (latest.getSystolicBp() != null && latest.getSystolicBp() > 140) return "ALERT";
        if (anomalies > 0) return "ALERT";

        return "STABLE";
    }

    private Map<String, Object> buildReading(MedicalData md) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("timestamp", md.getTimestamp().toString());
        m.put("heartRate", md.getHeartRate());
        m.put("spo2", md.getSpo2());
        m.put("temperature", md.getTemperature());
        m.put("systolicBp", md.getSystolicBp());
        m.put("diastolicBp", md.getDiastolicBp());
        m.put("respiratoryRate", md.getRespiratoryRate());
        m.put("hrv", md.getHrv());
        m.put("qtInterval", md.getQtInterval());
        m.put("bnp", md.getBnp());
        m.put("bloodGlucose", md.getBloodGlucose());
        m.put("fev1", md.getFev1());
        m.put("etco2", md.getEtco2());
        m.put("isAnomaly", md.getIsAnomaly());
        m.put("anomalyScore", md.getAnomalyScore());
        return m;
    }
}
