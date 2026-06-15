package itee.licenta.monitorizare.web.rest;

import itee.licenta.monitorizare.domain.*;
import itee.licenta.monitorizare.domain.enumeration.AppointmentStatus;
import itee.licenta.monitorizare.domain.enumeration.NotificationType;
import itee.licenta.monitorizare.repository.*;
import itee.licenta.monitorizare.security.SecurityUtils;
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
@RequestMapping("/api")
@Transactional
public class PatientDashboardResource {

    private static final Logger LOG = LoggerFactory.getLogger(PatientDashboardResource.class);

    private final MedicalDataRepository medicalDataRepository;
    private final PatientRepository patientRepository;
    private final AppointmentSlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;

    public PatientDashboardResource(
        MedicalDataRepository medicalDataRepository,
        PatientRepository patientRepository,
        AppointmentSlotRepository slotRepository,
        AppointmentRepository appointmentRepository,
        NotificationRepository notificationRepository
    ) {
        this.medicalDataRepository = medicalDataRepository;
        this.patientRepository = patientRepository;
        this.slotRepository = slotRepository;
        this.appointmentRepository = appointmentRepository;
        this.notificationRepository = notificationRepository;
    }

    // ===================== DASHBOARD DATA =====================

    @GetMapping("/patient/dashboard-data")
    public ResponseEntity<Map<String, Object>> getDashboardData(@RequestParam(defaultValue = "1d") String interval) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("User not logged in"));
        LOG.debug("REST request to get dashboard data for user {} with interval {}", login, interval);

        Optional<Patient> patientOpt = patientRepository.findByUserLogin(login);
        if (patientOpt.isEmpty()) {
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

    // ===================== APPOINTMENT ENDPOINTS =====================

    /**
     * GET /api/patient/available-slots : sloturi disponibile de la doctorul pacientului
     */
    @GetMapping("/patient/available-slots")
    public ResponseEntity<List<Map<String, Object>>> getAvailableSlots() {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow();
        Optional<Patient> patientOpt = patientRepository.findByUserLogin(login);
        if (patientOpt.isEmpty()) return ResponseEntity.notFound().build();

        Patient patient = patientOpt.get();
        Doctor doctor = patient.getDoctor();

        List<AppointmentSlot> slots = slotRepository
            .findAll()
            .stream()
            .filter(s -> s.getDoctor().getId().equals(doctor.getId()))
            .filter(s -> Boolean.TRUE.equals(s.getIsAvailable()))
            .filter(s -> s.getStartTime().isAfter(Instant.now()))
            .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
            .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();
        for (AppointmentSlot slot : slots) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", slot.getId());
            item.put("startTime", slot.getStartTime().toString());
            item.put("endTime", slot.getEndTime().toString());
            item.put("doctorName", "Dr. " + doctor.getUser().getLastName() + " " + doctor.getUser().getFirstName());
            result.add(item);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/patient/book-appointment : pacientul rezerva un slot
     */

    @PostMapping("/patient/book-appointment")
    public ResponseEntity<Map<String, Object>> bookAppointment(@RequestBody Map<String, Object> request) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow();
        Optional<Patient> patientOpt = patientRepository.findByUserLogin(login);
        if (patientOpt.isEmpty()) return ResponseEntity.notFound().build();

        Patient patient = patientOpt.get();
        Long slotId = ((Number) request.get("slotId")).longValue();
        String notes = (String) request.getOrDefault("notes", "");

        Optional<AppointmentSlot> slotOpt = slotRepository.findById(slotId);
        if (slotOpt.isEmpty() || !Boolean.TRUE.equals(slotOpt.get().getIsAvailable())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Slotul nu este disponibil"));
        }

        AppointmentSlot slot = slotOpt.get();
        if (!slot.getDoctor().getId().equals(patient.getDoctor().getId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Slotul nu apartine doctorului dumneavoastra"));
        }

        Appointment appointment = new Appointment();
        appointment.setSlot(slot);
        appointment.setPatient(patient);
        appointment.setDoctor(slot.getDoctor());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setNotes(notes);
        appointment.setCreatedAt(Instant.now());
        appointmentRepository.save(appointment);

        slot.setIsAvailable(false);
        slotRepository.save(slot);

        String patientName = patient.getUser().getFirstName() + " " + patient.getUser().getLastName();
        Notification notif = new Notification();
        notif.setType(NotificationType.APPOINTMENT_REMINDER);
        notif.setTitle("Programare noua");
        notif.setMessage("Pacientul " + patientName + " a programat o consultatie pentru " + slot.getStartTime());
        notif.setIsRead(false);
        notif.setCreatedAt(Instant.now());
        notif.setRecipientUser(slot.getDoctor().getUser());
        notif.setSenderUser(patient.getUser());
        notif.setRelatedPatient(patient);
        notificationRepository.save(notif);

        LOG.info("Patient {} booked slot {} with doctor {}", login, slotId, slot.getDoctor().getUser().getLogin());
        return ResponseEntity.ok(Map.of("message", "Programare realizata cu succes", "id", appointment.getId()));
    }

    /**
     * GET /api/patient/my-appointments : programarile pacientului
     */
    @GetMapping("/patient/my-appointments")
    public ResponseEntity<List<Map<String, Object>>> getMyAppointments() {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow();
        Optional<Patient> patientOpt = patientRepository.findByUserLogin(login);
        if (patientOpt.isEmpty()) return ResponseEntity.notFound().build();

        Patient patient = patientOpt.get();
        List<Appointment> appointments = appointmentRepository
            .findAll()
            .stream()
            .filter(a -> a.getPatient().getId().equals(patient.getId()))
            .sorted((a, b) -> b.getSlot().getStartTime().compareTo(a.getSlot().getStartTime()))
            .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Appointment appt : appointments) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", appt.getId());
            item.put("status", appt.getStatus().name());
            item.put("notes", appt.getNotes());
            item.put("createdAt", appt.getCreatedAt().toString());
            item.put("startTime", appt.getSlot().getStartTime().toString());
            item.put("endTime", appt.getSlot().getEndTime().toString());
            item.put("doctorName", "Dr. " + appt.getDoctor().getUser().getLastName() + " " + appt.getDoctor().getUser().getFirstName());
            result.add(item);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * PUT /api/patient/cancel-appointment/{id} : pacientul anuleaza programarea
     */
    @PutMapping("/patient/cancel-appointment/{appointmentId}")
    public ResponseEntity<Map<String, Object>> cancelAppointment(@PathVariable Long appointmentId) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow();
        Optional<Patient> patientOpt = patientRepository.findByUserLogin(login);
        if (patientOpt.isEmpty()) return ResponseEntity.notFound().build();

        Patient patient = patientOpt.get();
        Optional<Appointment> apptOpt = appointmentRepository.findById(appointmentId);
        if (apptOpt.isEmpty() || !apptOpt.get().getPatient().getId().equals(patient.getId())) {
            return ResponseEntity.notFound().build();
        }

        Appointment appt = apptOpt.get();
        if (appt.getStatus() != AppointmentStatus.SCHEDULED) {
            return ResponseEntity.badRequest().body(Map.of("error", "Doar programarile active pot fi anulate"));
        }

        appt.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appt);

        appt.getSlot().setIsAvailable(true);
        slotRepository.save(appt.getSlot());

        // Notify doctor
        String patientName = patient.getUser().getFirstName() + " " + patient.getUser().getLastName();
        Notification notif = new Notification();
        notif.setType(NotificationType.APPOINTMENT_REMINDER);
        notif.setTitle("Programare anulata de pacient");
        notif.setMessage("Pacientul " + patientName + " a anulat programarea din " + appt.getSlot().getStartTime());
        notif.setIsRead(false);
        notif.setCreatedAt(Instant.now());
        notif.setRecipientUser(appt.getDoctor().getUser());
        notif.setSenderUser(patient.getUser());
        notif.setRelatedPatient(patient);
        notificationRepository.save(notif);

        return ResponseEntity.ok(Map.of("message", "Programare anulata cu succes"));
    }

    // ===================== HELPERS =====================

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
