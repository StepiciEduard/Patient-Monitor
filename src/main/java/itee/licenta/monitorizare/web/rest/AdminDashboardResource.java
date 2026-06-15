package itee.licenta.monitorizare.web.rest;

import itee.licenta.monitorizare.domain.*;
import itee.licenta.monitorizare.domain.enumeration.PatientType;
import itee.licenta.monitorizare.repository.*;
import itee.licenta.monitorizare.security.AuthoritiesConstants;
import itee.licenta.monitorizare.web.websocket.PatientMonitorWebSocketService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@Transactional
public class AdminDashboardResource {

    private static final Logger LOG = LoggerFactory.getLogger(AdminDashboardResource.class);

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final MedicalDataRepository medicalDataRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final PatientMonitorWebSocketService webSocketService;

    public AdminDashboardResource(
        UserRepository userRepository,
        AuthorityRepository authorityRepository,
        DoctorRepository doctorRepository,
        PatientRepository patientRepository,
        MedicalDataRepository medicalDataRepository,
        NotificationRepository notificationRepository,
        PasswordEncoder passwordEncoder,
        PatientMonitorWebSocketService webSocketService
    ) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.medicalDataRepository = medicalDataRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.webSocketService = webSocketService;
    }

    // ==================== STATISTICS ====================

    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long totalUsers = userRepository.count();
        long totalDoctors = doctorRepository.count();
        List<Patient> allPatients = patientRepository.findAll();
        long totalPatients = allPatients.size();

        stats.put("totalUsers", totalUsers);
        stats.put("totalDoctors", totalDoctors);
        stats.put("totalPatients", totalPatients);

        Map<String, Long> perType = allPatients
            .stream()
            .collect(Collectors.groupingBy(p -> p.getPatientType().name(), Collectors.counting()));
        stats.put("cardiacPatients", perType.getOrDefault("CARDIAC", 0L));
        stats.put("diabetesPatients", perType.getOrDefault("DIABETES", 0L));
        stats.put("respiratoryPatients", perType.getOrDefault("RESPIRATORY", 0L));

        Instant todayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);
        long anomaliesToday = 0;
        for (Patient patient : allPatients) {
            List<MedicalData> todayData = medicalDataRepository.findByPatientIdAndTimestampAfterOrderByTimestampAsc(
                patient.getId(),
                todayStart
            );
            anomaliesToday += todayData.stream().filter(md -> Boolean.TRUE.equals(md.getIsAnomaly())).count();
        }
        stats.put("anomaliesToday", anomaliesToday);

        return ResponseEntity.ok(stats);
    }

    // ==================== DOCTORS ====================

    @GetMapping("/doctors-list")
    public ResponseEntity<List<Map<String, Object>>> getDoctorsList() {
        List<Doctor> doctors = doctorRepository.findAllWithToOneRelationships();
        List<Patient> allPatients = patientRepository.findAll();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Doctor doctor : doctors) {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("id", doctor.getId());
            d.put("userId", doctor.getUser().getId());
            d.put("login", doctor.getUser().getLogin());
            d.put("firstName", doctor.getUser().getFirstName());
            d.put("lastName", doctor.getUser().getLastName());
            d.put("email", doctor.getUser().getEmail());
            d.put("specialization", doctor.getSpecialization());
            d.put("phone", doctor.getPhone());
            d.put("officeLocation", doctor.getOfficeLocation());
            d.put("activated", doctor.getUser().isActivated());

            long patientCount = allPatients
                .stream()
                .filter(p -> p.getDoctor() != null && p.getDoctor().getId().equals(doctor.getId()))
                .count();
            d.put("patientCount", patientCount);

            result.add(d);
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/create-doctor")
    public ResponseEntity<Map<String, Object>> createDoctor(@RequestBody Map<String, String> request) {
        String login = request.get("login");
        String password = request.get("password");
        String firstName = request.get("firstName");
        String lastName = request.get("lastName");
        String email = request.get("email");
        String specialization = request.get("specialization");
        String phone = request.get("phone");
        String officeLocation = request.get("officeLocation");

        if (login == null || password == null || firstName == null || lastName == null || email == null || specialization == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Toate campurile obligatorii trebuie completate"));
        }

        if (userRepository.findOneByLogin(login.toLowerCase()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username-ul este deja folosit"));
        }

        if (userRepository.findOneByEmailIgnoreCase(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Emailul este deja folosit"));
        }

        User user = new User();
        user.setLogin(login.toLowerCase());
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email.toLowerCase());
        user.setActivated(true);
        user.setLangKey("ro");

        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
        authorityRepository.findById(AuthoritiesConstants.DOCTOR).ifPresent(authorities::add);
        user.setAuthorities(authorities);
        userRepository.save(user);

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setSpecialization(specialization);
        doctor.setPhone(phone);
        doctor.setOfficeLocation(officeLocation);
        doctorRepository.save(doctor);

        LOG.info("Admin created doctor account: {} ({} {})", login, firstName, lastName);

        try {
            webSocketService.sendAdminUpdate("DOCTOR_CREATED");
        } catch (Exception e) {
            LOG.debug("WS: {}", e.getMessage());
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", doctor.getId());
        response.put("login", user.getLogin());
        response.put("message", "Cont doctor creat cu succes");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/toggle-user/{userId}")
    public ResponseEntity<Map<String, Object>> toggleUser(@PathVariable Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        user.setActivated(!user.isActivated());
        userRepository.save(user);

        LOG.info("Admin toggled user {} activated={}", user.getLogin(), user.isActivated());

        try {
            webSocketService.sendAdminUpdate("USER_TOGGLED");
        } catch (Exception e) {
            LOG.debug("WS: {}", e.getMessage());
        }

        return ResponseEntity.ok(Map.of("login", user.getLogin(), "activated", user.isActivated()));
    }

    // ==================== DELETE DOCTOR WITH TRANSFER ====================

    @DeleteMapping("/delete-doctor/{doctorId}")
    public ResponseEntity<Map<String, Object>> deleteDoctor(
        @PathVariable Long doctorId,
        @RequestParam(required = false) Long transferToDoctorId
    ) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Doctor doctor = doctorOpt.get();
        User doctorUser = doctor.getUser();

        // Don't allow deleting admin
        if (doctorUser.getLogin().equals("admin")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nu poti sterge adminul"));
        }

        List<Patient> doctorPatients = patientRepository.findByDoctorUserLogin(doctorUser.getLogin());

        if (!doctorPatients.isEmpty()) {
            if (transferToDoctorId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Doctorul are pacienti. Selecteaza un doctor pentru transfer."));
            }

            Optional<Doctor> targetDoctorOpt = doctorRepository.findById(transferToDoctorId);
            if (targetDoctorOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Doctorul destinatie nu exista"));
            }

            Doctor targetDoctor = targetDoctorOpt.get();

            for (Patient patient : doctorPatients) {
                String spec = targetDoctor.getSpecialization().toLowerCase();
                if (spec.contains("cardio")) {
                    patient.setPatientType(PatientType.CARDIAC);
                } else if (spec.contains("diabet") || spec.contains("endocrin")) {
                    patient.setPatientType(PatientType.DIABETES);
                } else if (spec.contains("pneumo") || spec.contains("pulmo") || spec.contains("respir")) {
                    patient.setPatientType(PatientType.RESPIRATORY);
                }

                patient.setDoctor(targetDoctor);
                patientRepository.save(patient);
                LOG.info("Transferred patient {} to doctor {}", patient.getUser().getLogin(), targetDoctor.getUser().getLogin());
            }
        }

        // Delete notifications where doctor is recipient or sender
        notificationRepository.deleteByRecipientUserId(doctorUser.getId());
        notificationRepository.deleteBySenderUserId(doctorUser.getId());

        // Delete doctor entity
        doctorRepository.delete(doctor);

        // Delete user
        userRepository.delete(doctorUser);

        LOG.info("Admin deleted doctor: {} (transferred {} patients)", doctorUser.getLogin(), doctorPatients.size());

        try {
            webSocketService.sendAdminUpdate("DOCTOR_DELETED");
        } catch (Exception e) {
            LOG.debug("WS: {}", e.getMessage());
        }

        return ResponseEntity.ok(Map.of("message", "Doctor sters cu succes", "transferredPatients", doctorPatients.size()));
    }

    // ==================== DELETE PATIENT ====================

    @DeleteMapping("/delete-patient/{patientId}")
    public ResponseEntity<Map<String, Object>> deletePatient(@PathVariable Long patientId) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (patientOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Patient patient = patientOpt.get();
        User patientUser = patient.getUser();

        // Delete medical data
        medicalDataRepository.deleteByPatientId(patient.getId());

        // Delete notifications where patient's user is recipient or sender
        notificationRepository.deleteByRecipientUserId(patientUser.getId());
        notificationRepository.deleteBySenderUserId(patientUser.getId());

        // Delete notifications linked to this patient entity
        notificationRepository.deleteByPatientId(patient.getId());

        // Delete patient entity
        patientRepository.delete(patient);

        // Delete user
        userRepository.delete(patientUser);

        LOG.info("Admin deleted patient: {}", patientUser.getLogin());

        try {
            webSocketService.sendAdminUpdate("PATIENT_DELETED");
        } catch (Exception e) {
            LOG.debug("WS: {}", e.getMessage());
        }

        return ResponseEntity.ok(Map.of("message", "Pacient sters cu succes"));
    }

    // ==================== LEGACY DELETE (kept for backward compat) ====================

    @DeleteMapping("/delete-user/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();

        if (user.getLogin().equals("admin")) {
            return ResponseEntity.badRequest().build();
        }

        // Check if user is a doctor — redirect to delete-doctor flow
        Optional<Doctor> doctorOpt = doctorRepository.findByUserLogin(user.getLogin());
        if (doctorOpt.isPresent()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Foloseste endpoint-ul /delete-doctor/{doctorId} pentru stergerea doctorilor"));
        }

        // Check if user is a patient — use delete-patient flow
        Optional<Patient> patientOpt = patientRepository.findByUserLogin(user.getLogin());
        if (patientOpt.isPresent()) {
            return deletePatient(patientOpt.get().getId());
        }

        // Generic user (no doctor/patient entity) — just delete notifications and user
        notificationRepository.deleteByRecipientUserId(user.getId());
        notificationRepository.deleteBySenderUserId(user.getId());
        userRepository.delete(user);

        LOG.info("Admin deleted user: {}", user.getLogin());

        try {
            webSocketService.sendAdminUpdate("USER_DELETED");
        } catch (Exception e) {
            LOG.debug("WS: {}", e.getMessage());
        }

        return ResponseEntity.ok(Map.of("message", "User sters cu succes"));
    }

    // ==================== PATIENTS ====================

    @GetMapping("/patients-list")
    public ResponseEntity<List<Map<String, Object>>> getPatientsList(@RequestParam(required = false) Long doctorId) {
        List<Patient> patients;
        if (doctorId != null) {
            patients = patientRepository
                .findAll()
                .stream()
                .filter(p -> p.getDoctor() != null && p.getDoctor().getId().equals(doctorId))
                .collect(Collectors.toList());
        } else {
            patients = patientRepository.findAllWithToOneRelationships();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);

        for (Patient patient : patients) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("id", patient.getId());
            p.put("userId", patient.getUser().getId());
            p.put("login", patient.getUser().getLogin());
            p.put("firstName", patient.getUser().getFirstName());
            p.put("lastName", patient.getUser().getLastName());
            p.put("email", patient.getUser().getEmail());
            p.put("cnp", patient.getCnp());
            p.put("phoneNumber", patient.getPhoneNumber());
            p.put("patientType", patient.getPatientType().name());
            p.put("patientSubtype", patient.getPatientSubtype().name());
            p.put("activated", patient.getUser().isActivated());

            if (patient.getDoctor() != null && patient.getDoctor().getUser() != null) {
                p.put("doctorId", patient.getDoctor().getId());
                p.put(
                    "doctorName",
                    "Dr. " + patient.getDoctor().getUser().getLastName() + " " + patient.getDoctor().getUser().getFirstName()
                );
            } else {
                p.put("doctorId", null);
                p.put("doctorName", "Neasociat");
            }

            List<MedicalData> recentData = medicalDataRepository.findByPatientIdAndTimestampAfterOrderByTimestampAsc(
                patient.getId(),
                oneDayAgo
            );
            long anomalies = recentData.stream().filter(md -> Boolean.TRUE.equals(md.getIsAnomaly())).count();
            p.put("anomalies24h", anomalies);

            result.add(p);
        }

        return ResponseEntity.ok(result);
    }

    @PutMapping("/reassign-patient")
    public ResponseEntity<Map<String, Object>> reassignPatient(@RequestBody Map<String, Long> request) {
        Long patientId = request.get("patientId");
        Long newDoctorId = request.get("newDoctorId");

        if (patientId == null || newDoctorId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "PatientId si newDoctorId sunt obligatorii"));
        }

        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        Optional<Doctor> doctorOpt = doctorRepository.findById(newDoctorId);

        if (patientOpt.isEmpty() || doctorOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Patient patient = patientOpt.get();
        Doctor newDoctor = doctorOpt.get();

        String spec = newDoctor.getSpecialization().toLowerCase();
        if (spec.contains("cardio")) {
            patient.setPatientType(PatientType.CARDIAC);
        } else if (spec.contains("diabet") || spec.contains("endocrin")) {
            patient.setPatientType(PatientType.DIABETES);
        } else if (spec.contains("pneumo") || spec.contains("pulmo") || spec.contains("respir")) {
            patient.setPatientType(PatientType.RESPIRATORY);
        }

        patient.setDoctor(newDoctor);
        patientRepository.save(patient);

        LOG.info("Admin reassigned patient {} to doctor {}", patient.getUser().getLogin(), newDoctor.getUser().getLogin());

        try {
            webSocketService.sendAdminUpdate("PATIENT_REASSIGNED");
        } catch (Exception e) {
            LOG.debug("WS: {}", e.getMessage());
        }

        return ResponseEntity.ok(
            Map.of("message", "Pacient reasociat cu succes", "patientId", patient.getId(), "newDoctorId", newDoctor.getId())
        );
    }

    @PutMapping("/reset-password/{userId}")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.length() < 4) {
            return ResponseEntity.badRequest().body(Map.of("error", "Parola trebuie sa aiba minim 4 caractere"));
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        LOG.info("Admin reset password for user: {}", user.getLogin());

        return ResponseEntity.ok(Map.of("message", "Parola resetata cu succes pentru " + user.getLogin()));
    }
}
