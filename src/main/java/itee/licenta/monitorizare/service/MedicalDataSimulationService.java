package itee.licenta.monitorizare.service;

import itee.licenta.monitorizare.domain.MedicalData;
import itee.licenta.monitorizare.domain.Patient;
import itee.licenta.monitorizare.domain.enumeration.PatientSubtype;
import itee.licenta.monitorizare.domain.enumeration.PatientType;
import itee.licenta.monitorizare.repository.MedicalDataRepository;
import itee.licenta.monitorizare.repository.PatientRepository;
import itee.licenta.monitorizare.web.websocket.PatientMonitorWebSocketService;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MedicalDataSimulationService {

    private static final Logger LOG = LoggerFactory.getLogger(MedicalDataSimulationService.class);

    private final PatientRepository patientRepository;
    private final MedicalDataRepository medicalDataRepository;
    private final ThresholdAlertService thresholdAlertService;
    private final PatientMonitorWebSocketService webSocketService;
    private final AnomalyDetectionService anomalyDetectionService;

    private final Map<Long, MedicalData> lastReadings = new HashMap<>();
    private final Map<Long, Integer> crisisCountdown = new HashMap<>();
    private static final double WALK_WEIGHT = 0.3;

    public MedicalDataSimulationService(
        PatientRepository patientRepository,
        MedicalDataRepository medicalDataRepository,
        ThresholdAlertService thresholdAlertService,
        PatientMonitorWebSocketService webSocketService,
        AnomalyDetectionService anomalyDetectionService
    ) {
        this.patientRepository = patientRepository;
        this.medicalDataRepository = medicalDataRepository;
        this.thresholdAlertService = thresholdAlertService;
        this.webSocketService = webSocketService;
        this.anomalyDetectionService = anomalyDetectionService;
    }

    @Scheduled(fixedRate = 900000, initialDelay = 10000)
    public void generateMedicalData() {
        List<Patient> patients = patientRepository.findAll();
        Instant now = Instant.now();
        LOG.info("Generating medical data for {} patients at {}", patients.size(), now);
        for (Patient patient : patients) {
            MedicalData data = createMedicalData(patient, now);
            if (anomalyDetectionService.isReady()) {
                AnomalyDetectionService.AnomalyResult nnResult = anomalyDetectionService.analyze(data, patient.getPatientType().name());
                if (nnResult.isAnomaly() && !Boolean.TRUE.equals(data.getIsAnomaly())) {
                    data.setIsAnomaly(true);
                    data.setAnomalyScore(nnResult.score());
                }
            }
            medicalDataRepository.save(data);
            thresholdAlertService.checkAndAlert(data);

            // Send WebSocket update
            try {
                String patientLogin = patient.getUser() != null ? patient.getUser().getLogin() : null;
                String doctorLogin = (patient.getDoctor() != null && patient.getDoctor().getUser() != null)
                    ? patient.getDoctor().getUser().getLogin()
                    : null;
                webSocketService.sendMedicalDataUpdate(data, patient.getId(), patientLogin, doctorLogin);
            } catch (Exception e) {
                LOG.debug("WebSocket send failed (no subscribers): {}", e.getMessage());
            }
        }
        LOG.info("Medical data generation complete.");
    }

    public MedicalData createMedicalData(Patient patient, Instant timestamp) {
        MedicalData data = new MedicalData();
        data.setTimestamp(timestamp);
        data.setPatient(patient);
        data.setIsAnomaly(false);
        data.setAnomalyScore(0.0);

        PatientType type = patient.getPatientType();
        PatientSubtype subtype = patient.getPatientSubtype();

        double[] circadian = getCircadianModifiers(timestamp);
        double circadianHR = circadian[0];
        double circadianBP = circadian[1];
        double circadianGlucose = circadian[2];

        double sympathetic = generateSympathetic(subtype);
        double inflammation = generateInflammation(subtype);

        double baseTemp = 36.6 + inflammation * 1.2 + circadianHR * 0.02;
        double temp = gaussian(baseTemp, 0.15, 36.0, 39.5);
        data.setTemperature(temp);

        double tempPulseBonus = Math.max(0, (temp - 37.0)) * 8.0;
        double baseRR = 14 + sympathetic * 4 + inflammation * 3;

        boolean inCrisis = isInCrisis(patient.getId(), subtype);

        if (type == PatientType.CARDIAC) {
            generateCardiac(data, subtype, sympathetic, inflammation, tempPulseBonus, baseRR, circadianHR, circadianBP);
        } else if (type == PatientType.DIABETES) {
            generateDiabetes(data, subtype, sympathetic, inflammation, tempPulseBonus, baseRR, circadianHR, circadianBP, circadianGlucose);
        } else if (type == PatientType.RESPIRATORY) {
            generateRespiratory(data, subtype, sympathetic, inflammation, tempPulseBonus, baseRR, circadianHR, circadianBP, patient);
        }

        applyComorbidities(data, type, subtype);

        if (inCrisis) {
            injectCrisisValues(data, type, subtype);
        } else {
            maybeStartCrisis(data, type, subtype, patient.getId());
        }

        applyRandomWalk(data, patient.getId());
        lastReadings.put(patient.getId(), data);

        return data;
    }

    // ==================== CIRCADIAN RHYTHM ====================

    private double[] getCircadianModifiers(Instant timestamp) {
        ZonedDateTime zdt = timestamp.atZone(ZoneId.of("Europe/Bucharest"));
        int hour = zdt.getHour();

        double hrMod, bpMod, glucoseMod;

        if (hour >= 0 && hour < 4) {
            hrMod = -6;
            bpMod = -6;
            glucoseMod = -10;
        } else if (hour >= 4 && hour < 6) {
            hrMod = -3;
            bpMod = -2;
            glucoseMod = -5;
        } else if (hour >= 6 && hour < 9) {
            hrMod = 4;
            bpMod = 8;
            glucoseMod = 30;
        } else if (hour >= 9 && hour < 12) {
            hrMod = 2;
            bpMod = 3;
            glucoseMod = 5;
        } else if (hour >= 12 && hour < 14) {
            hrMod = 3;
            bpMod = 2;
            glucoseMod = 25;
        } else if (hour >= 14 && hour < 18) {
            hrMod = 1;
            bpMod = 1;
            glucoseMod = 0;
        } else if (hour >= 18 && hour < 21) {
            hrMod = 2;
            bpMod = 2;
            glucoseMod = 20;
        } else {
            hrMod = -2;
            bpMod = -3;
            glucoseMod = -5;
        }

        hrMod += gaussian(0, 1, -3, 3);
        bpMod += gaussian(0, 1.5, -4, 4);
        glucoseMod += gaussian(0, 5, -10, 10);

        return new double[] { hrMod, bpMod, glucoseMod };
    }

    // ==================== LATENT FACTOR GENERATORS ====================

    private double generateSympathetic(PatientSubtype subtype) {
        return switch (subtype) {
            case STABLE -> clamp(gaussian(0.15, 0.1, 0, 1), 0, 0.4);
            case BORDERLINE -> clamp(gaussian(0.4, 0.12, 0, 1), 0.15, 0.7);
            case CRITICAL -> clamp(gaussian(0.65, 0.15, 0, 1), 0.35, 1.0);
        };
    }

    private double generateInflammation(PatientSubtype subtype) {
        return switch (subtype) {
            case STABLE -> clamp(gaussian(0.05, 0.05, 0, 1), 0, 0.2);
            case BORDERLINE -> clamp(gaussian(0.2, 0.08, 0, 1), 0.05, 0.45);
            case CRITICAL -> clamp(gaussian(0.45, 0.15, 0, 1), 0.15, 0.8);
        };
    }

    // ==================== CARDIAC ====================

    private void generateCardiac(
        MedicalData data,
        PatientSubtype subtype,
        double sympathetic,
        double inflammation,
        double tempPulseBonus,
        double baseRR,
        double circadianHR,
        double circadianBP
    ) {
        double cardiacDysfunction =
            switch (subtype) {
                case STABLE -> clamp(gaussian(0.1, 0.08, 0, 1), 0, 0.3);
                case BORDERLINE -> clamp(gaussian(0.35, 0.1, 0, 1), 0.1, 0.6);
                case CRITICAL -> clamp(gaussian(0.65, 0.15, 0, 1), 0.3, 0.95);
            };

        double baseHR = 65 + sympathetic * 30 + cardiacDysfunction * 25 + tempPulseBonus + circadianHR;
        data.setHeartRate((int) gaussian(baseHR, 3, 45, 180));

        double baseSystolic = 115 + sympathetic * 25 + inflammation * 10 + circadianBP;
        if (cardiacDysfunction > 0.7) baseSystolic -= (cardiacDysfunction - 0.7) * 30;
        data.setSystolicBp((int) gaussian(baseSystolic, 4, 80, 220));

        double baseDiastolic = 75 + sympathetic * 10 + inflammation * 5 + circadianBP * 0.5;
        data.setDiastolicBp((int) gaussian(baseDiastolic, 3, 55, 120));

        double baseHRV = 65 - sympathetic * 25 - cardiacDysfunction * 20;
        data.setHrv(gaussian(baseHRV, 4.0, 3.0, 90.0));

        double baseQT = 380 + cardiacDysfunction * 120 + sympathetic * 20;
        data.setQtInterval((int) gaussian(baseQT, 10, 340, 580));

        double baseBNP = 20 + Math.pow(cardiacDysfunction * 10, 2.2);
        data.setBnp(gaussian(baseBNP, baseBNP * 0.12, 5.0, 5000.0));

        double baseSpO2 = 98.0 - sympathetic * 1.5 - cardiacDysfunction * 4;
        data.setSpo2(gaussian(baseSpO2, 0.5, 82.0, 100.0));

        double cardiacRR = baseRR + cardiacDysfunction * 6;
        data.setRespiratoryRate((int) gaussian(cardiacRR, 1.5, 10, 40));

        data.setBloodGlucose(null);
        data.setFev1(null);
        data.setEtco2(null);
    }

    // ==================== DIABETES ====================

    private void generateDiabetes(
        MedicalData data,
        PatientSubtype subtype,
        double sympathetic,
        double inflammation,
        double tempPulseBonus,
        double baseRR,
        double circadianHR,
        double circadianBP,
        double circadianGlucose
    ) {
        double insulinResistance =
            switch (subtype) {
                case STABLE -> clamp(gaussian(0.15, 0.08, 0, 1), 0, 0.35);
                case BORDERLINE -> clamp(gaussian(0.45, 0.12, 0, 1), 0.2, 0.7);
                case CRITICAL -> clamp(gaussian(0.75, 0.12, 0, 1), 0.5, 1.0);
            };

        double baseGlucose =
            85 + insulinResistance * 250 + sympathetic * 40 + inflammation * 30 + circadianGlucose * (1 + insulinResistance);
        data.setBloodGlucose(gaussian(baseGlucose, baseGlucose * 0.06, 50.0, 600.0));

        double autonomicDamage = insulinResistance * 0.4;
        double baseHR = 70 + sympathetic * 20 + autonomicDamage * 15 + tempPulseBonus + circadianHR;
        data.setHeartRate((int) gaussian(baseHR, 3, 55, 150));

        double baseSystolic = 115 + sympathetic * 15 + insulinResistance * 25 + inflammation * 8 + circadianBP;
        data.setSystolicBp((int) gaussian(baseSystolic, 4, 95, 200));

        double baseDiastolic = 75 + sympathetic * 8 + insulinResistance * 12 + circadianBP * 0.5;
        data.setDiastolicBp((int) gaussian(baseDiastolic, 3, 60, 110));

        double baseSpO2 = 97.5 - sympathetic * 0.8 - inflammation * 1.5;
        data.setSpo2(gaussian(baseSpO2, 0.4, 90.0, 100.0));

        double diabeticRR = baseRR + insulinResistance * 3;
        data.setRespiratoryRate((int) gaussian(diabeticRR, 1.5, 10, 35));

        data.setHrv(null);
        data.setQtInterval(null);
        data.setBnp(null);
        data.setFev1(null);
        data.setEtco2(null);
    }

    // ==================== RESPIRATORY ====================

    private void generateRespiratory(
        MedicalData data,
        PatientSubtype subtype,
        double sympathetic,
        double inflammation,
        double tempPulseBonus,
        double baseRR,
        double circadianHR,
        double circadianBP,
        Patient patient
    ) {
        Double fev1Baseline = patient.getFev1Baseline() != null ? patient.getFev1Baseline() : 80.0;

        double obstruction =
            switch (subtype) {
                case STABLE -> clamp(gaussian(0.1, 0.06, 0, 1), 0, 0.25);
                case BORDERLINE -> clamp(gaussian(0.4, 0.1, 0, 1), 0.15, 0.6);
                case CRITICAL -> clamp(gaussian(0.7, 0.12, 0, 1), 0.45, 0.95);
            };

        double fev1Fraction = 1.0 - obstruction * 0.7;
        double baseFEV1 = fev1Baseline * fev1Fraction;
        data.setFev1(gaussian(baseFEV1, baseFEV1 * 0.04, fev1Baseline * 0.15, fev1Baseline * 1.05));

        double baseSpO2 = 99.0 - obstruction * 12 - inflammation * 2;
        data.setSpo2(gaussian(baseSpO2, 0.8, 70.0, 100.0));

        double baseEtCO2 = 38.0 + obstruction * 30 + inflammation * 5;
        data.setEtco2(gaussian(baseEtCO2, 1.5, 30.0, 100.0));

        double respRR = baseRR + obstruction * 8 + Math.max(0, (95 - baseSpO2)) * 0.5;
        data.setRespiratoryRate((int) gaussian(respRR, 1.5, 10, 45));

        double hypoxiaDrive = Math.max(0, (95 - baseSpO2)) * 1.5;
        double baseHR = 70 + sympathetic * 15 + hypoxiaDrive + tempPulseBonus + circadianHR;
        data.setHeartRate((int) gaussian(baseHR, 3, 55, 160));

        double baseSystolic = 115 + sympathetic * 12 + hypoxiaDrive * 0.8 + circadianBP;
        data.setSystolicBp((int) gaussian(baseSystolic, 4, 95, 180));

        double baseDiastolic = 75 + sympathetic * 6 + circadianBP * 0.5;
        data.setDiastolicBp((int) gaussian(baseDiastolic, 3, 60, 105));

        data.setHrv(null);
        data.setQtInterval(null);
        data.setBnp(null);
        data.setBloodGlucose(null);
    }

    // ==================== COMORBIDITY EFFECTS ====================

    private void applyComorbidities(MedicalData data, PatientType type, PatientSubtype subtype) {
        double severity =
            switch (subtype) {
                case STABLE -> 0.2;
                case BORDERLINE -> 0.5;
                case CRITICAL -> 0.8;
            };

        if (type == PatientType.DIABETES) {
            data.setHrv(gaussian(50 - severity * 25, 5, 8, 65));
        }
        if (type == PatientType.RESPIRATORY) {
            data.setBnp(gaussian(30 + severity * 120, 20, 5, 400));
        }
        if (type == PatientType.CARDIAC) {
            data.setBloodGlucose(gaussian(95 + severity * 35, 10, 70, 200));
        }
    }

    // ==================== CRISIS EPISODES ====================

    private boolean isInCrisis(Long patientId, PatientSubtype subtype) {
        int remaining = crisisCountdown.getOrDefault(patientId, 0);
        if (remaining > 0) {
            crisisCountdown.put(patientId, remaining - 1);
            return true;
        }
        return false;
    }

    private void maybeStartCrisis(MedicalData data, PatientType type, PatientSubtype subtype, Long patientId) {
        double anomalyChance =
            switch (subtype) {
                case STABLE -> 0.02;
                case BORDERLINE -> 0.08;
                case CRITICAL -> 0.20;
            };

        if (ThreadLocalRandom.current().nextDouble() < anomalyChance) {
            int duration = ThreadLocalRandom.current().nextInt(2, 5);
            crisisCountdown.put(patientId, duration - 1);
            injectCrisisValues(data, type, subtype);
        }
    }

    private void injectCrisisValues(MedicalData data, PatientType type, PatientSubtype subtype) {
        int remaining = crisisCountdown.getOrDefault(data.getPatient().getId(), 0);
        double severity = (remaining + 1.0) / 4.0;
        severity = Math.min(1.0, severity);

        data.setIsAnomaly(true);
        data.setAnomalyScore(gaussian(0.7 + severity * 0.2, 0.05, 0.6, 1.0));

        if (type == PatientType.CARDIAC) {
            data.setHeartRate((int) gaussian(140 + severity * 40, 10, 120, 210));
            data.setSystolicBp((int) gaussian(180 + severity * 30, 8, 160, 240));
            data.setBnp(gaussian(2000 + severity * 2000, 400, 1000, 5000));
            data.setHrv(gaussian(12 - severity * 5, 2, 3, 18));
            data.setQtInterval((int) gaussian(500 + severity * 50, 15, 470, 600));
            data.setSpo2(gaussian(88 - severity * 5, 2, 75, 93));
            data.setRespiratoryRate((int) gaussian(26 + severity * 8, 2, 22, 40));
        } else if (type == PatientType.DIABETES) {
            data.setBloodGlucose(gaussian(400 + severity * 150, 40, 300, 650));
            data.setHeartRate((int) gaussian(115 + severity * 20, 8, 100, 160));
            data.setRespiratoryRate((int) gaussian(24 + severity * 6, 2, 20, 36));
            data.setSystolicBp((int) gaussian(100 - severity * 15, 6, 75, 115));
            data.setTemperature(gaussian(37.5 + severity * 0.8, 0.3, 37.0, 39.5));
        } else if (type == PatientType.RESPIRATORY) {
            data.setSpo2(gaussian(82 - severity * 8, 3, 60, 88));
            data.setFev1(gaussian(22 - severity * 8, 3, 8, 30));
            data.setEtco2(gaussian(75 + severity * 15, 4, 65, 100));
            data.setHeartRate((int) gaussian(125 + severity * 20, 8, 110, 170));
            data.setRespiratoryRate((int) gaussian(30 + severity * 8, 3, 25, 45));
        }
    }

    // ==================== RANDOM WALK ====================

    private void applyRandomWalk(MedicalData data, Long patientId) {
        MedicalData prev = lastReadings.get(patientId);
        if (prev == null) return;
        if (Boolean.TRUE.equals(data.getIsAnomaly())) return;

        data.setHeartRate(walkInt(prev.getHeartRate(), data.getHeartRate()));
        data.setSystolicBp(walkInt(prev.getSystolicBp(), data.getSystolicBp()));
        data.setDiastolicBp(walkInt(prev.getDiastolicBp(), data.getDiastolicBp()));
        data.setRespiratoryRate(walkInt(prev.getRespiratoryRate(), data.getRespiratoryRate()));
        data.setTemperature(walkDouble(prev.getTemperature(), data.getTemperature()));
        data.setSpo2(walkDouble(prev.getSpo2(), data.getSpo2()));
        data.setHrv(walkDouble(prev.getHrv(), data.getHrv()));
        data.setQtInterval(walkInt(prev.getQtInterval(), data.getQtInterval()));
        data.setBnp(walkDouble(prev.getBnp(), data.getBnp()));
        data.setBloodGlucose(walkDouble(prev.getBloodGlucose(), data.getBloodGlucose()));
        data.setFev1(walkDouble(prev.getFev1(), data.getFev1()));
        data.setEtco2(walkDouble(prev.getEtco2(), data.getEtco2()));
    }

    private Integer walkInt(Integer prev, Integer current) {
        if (prev == null || current == null) return current;
        return (int) Math.round(prev * WALK_WEIGHT + current * (1 - WALK_WEIGHT));
    }

    private Double walkDouble(Double prev, Double current) {
        if (prev == null || current == null) return current;
        double result = prev * WALK_WEIGHT + current * (1 - WALK_WEIGHT);
        return Math.round(result * 10.0) / 10.0;
    }

    // ==================== UTILITY ====================

    private double gaussian(double mean, double stdDev, double min, double max) {
        double value = mean + ThreadLocalRandom.current().nextGaussian() * stdDev;
        value = Math.max(min, Math.min(max, value));
        return Math.round(value * 10.0) / 10.0;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
