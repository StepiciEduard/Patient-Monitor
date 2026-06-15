package itee.licenta.monitorizare.service;

import ai.onnxruntime.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import itee.licenta.monitorizare.domain.MedicalData;
import java.io.InputStream;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class AnomalyDetectionService {

    private static final Logger LOG = LoggerFactory.getLogger(AnomalyDetectionService.class);

    private final Map<String, OrtSession> sessions = new HashMap<>();
    private final Map<String, List<String>> featureNames = new HashMap<>();
    private final Map<String, double[]> scalerMins = new HashMap<>();
    private final Map<String, double[]> scalerMaxs = new HashMap<>();
    private final Map<String, Double> thresholds = new HashMap<>();
    private final OrtEnvironment env;
    private boolean modelsLoaded = false;

    public AnomalyDetectionService() {
        this.env = OrtEnvironment.getEnvironment();
        loadModels();
    }

    private void loadModels() {
        try {
            // Load config JSON
            ClassPathResource configResource = new ClassPathResource("ml/autoencoder_config.json");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode config = mapper.readTree(configResource.getInputStream());

            for (String patientType : new String[] { "CARDIAC", "DIABETES", "RESPIRATORY" }) {
                JsonNode typeConfig = config.get(patientType);
                if (typeConfig == null) {
                    LOG.warn("No config found for patient type: {}", patientType);
                    continue;
                }

                // Load features
                List<String> features = new ArrayList<>();
                for (JsonNode f : typeConfig.get("features")) {
                    features.add(f.asText());
                }
                featureNames.put(patientType, features);

                // Load scaler params
                JsonNode minNode = typeConfig.get("scaler_min");
                JsonNode maxNode = typeConfig.get("scaler_max");
                double[] mins = new double[features.size()];
                double[] maxs = new double[features.size()];
                for (int i = 0; i < features.size(); i++) {
                    mins[i] = minNode.get(i).asDouble();
                    maxs[i] = maxNode.get(i).asDouble();
                }
                scalerMins.put(patientType, mins);
                scalerMaxs.put(patientType, maxs);

                // Load threshold
                thresholds.put(patientType, typeConfig.get("threshold").asDouble());

                // Load ONNX model
                String modelPath = "ml/autoencoder_" + patientType.toLowerCase() + ".onnx";
                ClassPathResource modelResource = new ClassPathResource(modelPath);
                byte[] modelBytes = modelResource.getInputStream().readAllBytes();
                OrtSession session = env.createSession(modelBytes, new OrtSession.SessionOptions());
                sessions.put(patientType, session);

                LOG.info(
                    "Loaded autoencoder model for {}: {} features, threshold={}",
                    patientType,
                    features.size(),
                    thresholds.get(patientType)
                );
            }

            modelsLoaded = true;
            LOG.info("All autoencoder models loaded successfully");
        } catch (Exception e) {
            LOG.error("Failed to load autoencoder models: {}", e.getMessage(), e);
            modelsLoaded = false;
        }
    }

    /**
     * Analyze a medical data reading for anomalies using the autoencoder.
     * Returns the anomaly score (reconstruction error).
     * If score > threshold, it's an anomaly.
     */
    public AnomalyResult analyze(MedicalData data, String patientType) {
        if (!modelsLoaded || !sessions.containsKey(patientType)) {
            return new AnomalyResult(false, 0.0);
        }

        try {
            List<String> features = featureNames.get(patientType);
            double[] mins = scalerMins.get(patientType);
            double[] maxs = scalerMaxs.get(patientType);
            double threshold = thresholds.get(patientType);

            // Extract feature values from MedicalData
            double[] rawValues = extractFeatures(data, features);

            // Check for null values
            for (double v : rawValues) {
                if (Double.isNaN(v)) {
                    return new AnomalyResult(false, 0.0);
                }
            }

            // Normalize using MinMaxScaler: (x - min) / (max - min)
            float[] normalized = new float[features.size()];
            for (int i = 0; i < features.size(); i++) {
                double range = maxs[i] - mins[i];
                if (range == 0) {
                    normalized[i] = 0f;
                } else {
                    normalized[i] = (float) ((rawValues[i] - mins[i]) / range);
                    normalized[i] = Math.max(0f, Math.min(1f, normalized[i]));
                }
            }

            // Run inference
            float[][] inputArray = new float[][] { normalized };
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputArray);

            String inputName = sessions.get(patientType).getInputNames().iterator().next();
            Map<String, OnnxTensor> inputs = Map.of(inputName, inputTensor);

            OrtSession.Result result = sessions.get(patientType).run(inputs);
            float[][] output = (float[][]) result.get(0).getValue();

            // Calculate reconstruction error (MSE)
            double mse = 0.0;
            for (int i = 0; i < normalized.length; i++) {
                double diff = normalized[i] - output[0][i];
                mse += diff * diff;
            }
            mse /= normalized.length;

            // Compare with threshold
            boolean isAnomaly = mse > threshold;

            inputTensor.close();
            result.close();

            LOG.debug("Autoencoder {} - MSE: {}, threshold: {}, anomaly: {}", patientType, mse, threshold, isAnomaly);

            return new AnomalyResult(isAnomaly, mse);
        } catch (Exception e) {
            LOG.error("Error during anomaly detection for {}: {}", patientType, e.getMessage());
            return new AnomalyResult(false, 0.0);
        }
    }

    private double[] extractFeatures(MedicalData data, List<String> features) {
        double[] values = new double[features.size()];
        for (int i = 0; i < features.size(); i++) {
            values[i] = getFeatureValue(data, features.get(i));
        }
        return values;
    }

    private double getFeatureValue(MedicalData data, String featureName) {
        return switch (featureName) {
            case "heart_rate" -> data.getHeartRate() != null ? data.getHeartRate() : Double.NaN;
            case "spo_2" -> data.getSpo2() != null ? data.getSpo2() : Double.NaN;
            case "temperature" -> data.getTemperature() != null ? data.getTemperature() : Double.NaN;
            case "systolic_bp" -> data.getSystolicBp() != null ? data.getSystolicBp() : Double.NaN;
            case "diastolic_bp" -> data.getDiastolicBp() != null ? data.getDiastolicBp() : Double.NaN;
            case "respiratory_rate" -> data.getRespiratoryRate() != null ? data.getRespiratoryRate() : Double.NaN;
            case "hrv" -> data.getHrv() != null ? data.getHrv() : Double.NaN;
            case "qt_interval" -> data.getQtInterval() != null ? data.getQtInterval() : Double.NaN;
            case "bnp" -> data.getBnp() != null ? data.getBnp() : Double.NaN;
            case "blood_glucose" -> data.getBloodGlucose() != null ? data.getBloodGlucose() : Double.NaN;
            case "fev_1" -> data.getFev1() != null ? data.getFev1() : Double.NaN;
            case "etco_2" -> data.getEtco2() != null ? data.getEtco2() : Double.NaN;
            default -> Double.NaN;
        };
    }

    public boolean isReady() {
        return modelsLoaded;
    }

    /**
     * Result of anomaly detection.
     */
    public record AnomalyResult(boolean isAnomaly, double score) {}
}
