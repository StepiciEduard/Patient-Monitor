package itee.licenta.monitorizare.config;

import itee.licenta.monitorizare.domain.*;
import itee.licenta.monitorizare.domain.enumeration.*;
import itee.licenta.monitorizare.repository.*;
import itee.licenta.monitorizare.security.AuthoritiesConstants;
import itee.licenta.monitorizare.service.MedicalDataSimulationService;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final MedicalDataRepository medicalDataRepository;
    private final MedicalDataSimulationService simulationService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
        UserRepository userRepository,
        AuthorityRepository authorityRepository,
        DoctorRepository doctorRepository,
        PatientRepository patientRepository,
        MedicalDataRepository medicalDataRepository,
        MedicalDataSimulationService simulationService,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.medicalDataRepository = medicalDataRepository;
        this.simulationService = simulationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (doctorRepository.count() > 0) {
            // Doctors exist, but check if medical data needs regeneration
            if (medicalDataRepository.count() == 0) {
                LOG.info("No medical data found. Regenerating 30 days of historical data...");
                List<Patient> allPatients = patientRepository.findAll();
                seedHistoricalData(allPatients);
                LOG.info("Historical data regenerated for {} patients.", allPatients.size());
            } else {
                LOG.info("Demo data already exists, skipping initialization.");
            }
            return;
        }

        LOG.info("Creating demo data...");

        Authority roleUser = authorityRepository.findById(AuthoritiesConstants.USER).orElseThrow();
        Authority roleDoctor = authorityRepository.findById(AuthoritiesConstants.DOCTOR).orElseThrow();
        Authority rolePatient = authorityRepository.findById(AuthoritiesConstants.PATIENT).orElseThrow();

        Doctor doctor1 = createDoctor(
            "doctor.marinescu",
            "Andrei",
            "Marinescu",
            "doctor.marinescu@test.com",
            "Cardiologie",
            "0721000001",
            "Cabinet 101",
            roleUser,
            roleDoctor
        );
        Doctor doctor2 = createDoctor(
            "doctor.georgescu",
            "Maria",
            "Georgescu",
            "doctor.georgescu@test.com",
            "Diabet",
            "0721000002",
            "Cabinet 202",
            roleUser,
            roleDoctor
        );
        Doctor doctor3 = createDoctor(
            "doctor.vasilescu",
            "Ion",
            "Vasilescu",
            "doctor.vasilescu@test.com",
            "Pneumologie",
            "0721000003",
            "Cabinet 303",
            roleUser,
            roleDoctor
        );

        List<Patient> allPatients = new ArrayList<>();

        allPatients.add(
            createPatient(
                "pacient.popescu",
                "Ion",
                "Popescu",
                "pacient.popescu@test.com",
                "1850101123456",
                "0731000001",
                "Str. Florilor 1, București",
                PatientType.CARDIAC,
                PatientSubtype.STABLE,
                LocalDate.of(1985, 1, 1),
                "M",
                null,
                null,
                null,
                doctor1,
                roleUser,
                rolePatient
            )
        );
        allPatients.add(
            createPatient(
                "pacient.ionescu",
                "Maria",
                "Ionescu",
                "pacient.ionescu@test.com",
                "2900215123457",
                "0731000002",
                "Str. Rozelor 5, Cluj",
                PatientType.CARDIAC,
                PatientSubtype.BORDERLINE,
                LocalDate.of(1990, 2, 15),
                "F",
                null,
                null,
                null,
                doctor1,
                roleUser,
                rolePatient
            )
        );
        allPatients.add(
            createPatient(
                "pacient.dumitrescu",
                "Vasile",
                "Dumitrescu",
                "pacient.dumitrescu@test.com",
                "1780530123458",
                "0731000003",
                "Str. Pacii 10, Timișoara",
                PatientType.CARDIAC,
                PatientSubtype.CRITICAL,
                LocalDate.of(1978, 5, 30),
                "M",
                null,
                null,
                null,
                doctor1,
                roleUser,
                rolePatient
            )
        );

        allPatients.add(
            createPatient(
                "pacient.popa",
                "Elena",
                "Popa",
                "pacient.popa@test.com",
                "2880712123459",
                "0731000004",
                "Str. Libertății 3, Iași",
                PatientType.DIABETES,
                PatientSubtype.STABLE,
                LocalDate.of(1988, 7, 12),
                "F",
                5.4,
                23.5,
                null,
                doctor2,
                roleUser,
                rolePatient
            )
        );
        allPatients.add(
            createPatient(
                "pacient.stan",
                "Andrei",
                "Stan",
                "pacient.stan@test.com",
                "1920320123460",
                "0731000005",
                "Str. Unirii 15, Brașov",
                PatientType.DIABETES,
                PatientSubtype.BORDERLINE,
                LocalDate.of(1992, 3, 20),
                "M",
                6.1,
                27.8,
                null,
                doctor2,
                roleUser,
                rolePatient
            )
        );
        allPatients.add(
            createPatient(
                "pacient.radu",
                "Ana",
                "Radu",
                "pacient.radu@test.com",
                "2750908123461",
                "0731000006",
                "Str. Victoriei 22, Constanța",
                PatientType.DIABETES,
                PatientSubtype.CRITICAL,
                LocalDate.of(1975, 9, 8),
                "F",
                7.2,
                32.1,
                null,
                doctor2,
                roleUser,
                rolePatient
            )
        );

        allPatients.add(
            createPatient(
                "pacient.constantinescu",
                "Mihai",
                "Constantinescu",
                "pacient.constantinescu@test.com",
                "1820415123462",
                "0731000007",
                "Str. Primăverii 7, Sibiu",
                PatientType.RESPIRATORY,
                PatientSubtype.STABLE,
                LocalDate.of(1982, 4, 15),
                "M",
                null,
                null,
                92.0,
                doctor3,
                roleUser,
                rolePatient
            )
        );
        allPatients.add(
            createPatient(
                "pacient.marin",
                "Gabriela",
                "Marin",
                "pacient.marin@test.com",
                "2950625123463",
                "0731000008",
                "Str. Teilor 12, Oradea",
                PatientType.RESPIRATORY,
                PatientSubtype.BORDERLINE,
                LocalDate.of(1995, 6, 25),
                "F",
                null,
                null,
                68.0,
                doctor3,
                roleUser,
                rolePatient
            )
        );
        allPatients.add(
            createPatient(
                "pacient.stoica",
                "Dan",
                "Stoica",
                "pacient.stoica@test.com",
                "1700103123464",
                "0731000009",
                "Str. Gării 30, Craiova",
                PatientType.RESPIRATORY,
                PatientSubtype.CRITICAL,
                LocalDate.of(1970, 1, 3),
                "M",
                null,
                null,
                45.0,
                doctor3,
                roleUser,
                rolePatient
            )
        );

        LOG.info("Demo users created. Generating 30 days of historical medical data...");
        seedHistoricalData(allPatients);

        LOG.info("Demo data created successfully! 3 doctors + 9 patients + 30 days of medical data.");
        LOG.info("Login credentials: password = 'parola123' for all demo users.");
    }

    /**
     * Generates 30 days of retrospective medical data at 15-minute intervals.
     * Uses batch inserts for performance (~25,920 records).
     * Does NOT generate notifications for historical data.
     */
    private void seedHistoricalData(List<Patient> patients) {
        Instant now = Instant.now();
        Instant startTime = now.minus(Duration.ofDays(30));
        Duration interval = Duration.ofMinutes(15);

        int totalReadings = 0;
        int batchSize = 500;
        List<MedicalData> batch = new ArrayList<>(batchSize);

        for (Patient patient : patients) {
            Instant timestamp = startTime;
            int patientReadings = 0;

            while (timestamp.isBefore(now)) {
                MedicalData data = simulationService.createMedicalData(patient, timestamp);
                batch.add(data);
                patientReadings++;
                totalReadings++;

                if (batch.size() >= batchSize) {
                    medicalDataRepository.saveAll(batch);
                    medicalDataRepository.flush();
                    batch.clear();
                }

                timestamp = timestamp.plus(interval);
            }

            LOG.info(
                "Generated {} readings for patient {} ({})",
                patientReadings,
                patient.getUser().getLastName(),
                patient.getPatientSubtype()
            );
        }

        // Save remaining batch
        if (!batch.isEmpty()) {
            medicalDataRepository.saveAll(batch);
            medicalDataRepository.flush();
        }

        LOG.info("Historical data seeding complete: {} total readings.", totalReadings);
    }

    private Doctor createDoctor(
        String login,
        String firstName,
        String lastName,
        String email,
        String specialization,
        String phone,
        String officeLocation,
        Authority roleUser,
        Authority roleDoctor
    ) {
        User user = new User();
        user.setLogin(login);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setActivated(true);
        user.setLangKey("ro");
        user.setPassword(passwordEncoder.encode("parola123"));
        user.setAuthorities(Set.of(roleUser, roleDoctor));
        user = userRepository.saveAndFlush(user);

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setSpecialization(specialization);
        doctor.setPhone(phone);
        doctor.setOfficeLocation(officeLocation);
        return doctorRepository.saveAndFlush(doctor);
    }

    private Patient createPatient(
        String login,
        String firstName,
        String lastName,
        String email,
        String cnp,
        String phoneNumber,
        String address,
        PatientType patientType,
        PatientSubtype patientSubtype,
        LocalDate dateOfBirth,
        String gender,
        Double hba1c,
        Double bmi,
        Double fev1Baseline,
        Doctor doctor,
        Authority roleUser,
        Authority rolePatient
    ) {
        User user = new User();
        user.setLogin(login);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setActivated(true);
        user.setLangKey("ro");
        user.setPassword(passwordEncoder.encode("parola123"));
        user.setAuthorities(Set.of(roleUser, rolePatient));
        user = userRepository.saveAndFlush(user);

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setDoctor(doctor);
        patient.setCnp(cnp);
        patient.setPhoneNumber(phoneNumber);
        patient.setAddress(address);
        patient.setPatientType(patientType);
        patient.setPatientSubtype(patientSubtype);
        patient.setDateOfBirth(dateOfBirth);
        patient.setGender(gender);
        patient.setHba1c(hba1c);
        patient.setBmi(bmi);
        patient.setFev1Baseline(fev1Baseline);
        return patientRepository.saveAndFlush(patient);
    }
}
