package itee.licenta.monitorizare.web.rest;

import static itee.licenta.monitorizare.domain.MedicalDataAsserts.*;
import static itee.licenta.monitorizare.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import itee.licenta.monitorizare.IntegrationTest;
import itee.licenta.monitorizare.domain.MedicalData;
import itee.licenta.monitorizare.domain.Patient;
import itee.licenta.monitorizare.repository.MedicalDataRepository;
import itee.licenta.monitorizare.service.dto.MedicalDataDTO;
import itee.licenta.monitorizare.service.mapper.MedicalDataMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link MedicalDataResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class MedicalDataResourceIT {

    private static final Instant DEFAULT_TIMESTAMP = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_TIMESTAMP = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Integer DEFAULT_HEART_RATE = 1;
    private static final Integer UPDATED_HEART_RATE = 2;
    private static final Integer SMALLER_HEART_RATE = 1 - 1;

    private static final Double DEFAULT_SPO_2 = 1D;
    private static final Double UPDATED_SPO_2 = 2D;
    private static final Double SMALLER_SPO_2 = 1D - 1D;

    private static final Double DEFAULT_TEMPERATURE = 1D;
    private static final Double UPDATED_TEMPERATURE = 2D;
    private static final Double SMALLER_TEMPERATURE = 1D - 1D;

    private static final Integer DEFAULT_SYSTOLIC_BP = 1;
    private static final Integer UPDATED_SYSTOLIC_BP = 2;
    private static final Integer SMALLER_SYSTOLIC_BP = 1 - 1;

    private static final Integer DEFAULT_DIASTOLIC_BP = 1;
    private static final Integer UPDATED_DIASTOLIC_BP = 2;
    private static final Integer SMALLER_DIASTOLIC_BP = 1 - 1;

    private static final Double DEFAULT_HRV = 1D;
    private static final Double UPDATED_HRV = 2D;
    private static final Double SMALLER_HRV = 1D - 1D;

    private static final Integer DEFAULT_QT_INTERVAL = 1;
    private static final Integer UPDATED_QT_INTERVAL = 2;
    private static final Integer SMALLER_QT_INTERVAL = 1 - 1;

    private static final Double DEFAULT_BNP = 1D;
    private static final Double UPDATED_BNP = 2D;
    private static final Double SMALLER_BNP = 1D - 1D;

    private static final Double DEFAULT_BLOOD_GLUCOSE = 1D;
    private static final Double UPDATED_BLOOD_GLUCOSE = 2D;
    private static final Double SMALLER_BLOOD_GLUCOSE = 1D - 1D;

    private static final Integer DEFAULT_RESPIRATORY_RATE = 1;
    private static final Integer UPDATED_RESPIRATORY_RATE = 2;
    private static final Integer SMALLER_RESPIRATORY_RATE = 1 - 1;

    private static final Double DEFAULT_FEV_1 = 1D;
    private static final Double UPDATED_FEV_1 = 2D;
    private static final Double SMALLER_FEV_1 = 1D - 1D;

    private static final Double DEFAULT_ETCO_2 = 1D;
    private static final Double UPDATED_ETCO_2 = 2D;
    private static final Double SMALLER_ETCO_2 = 1D - 1D;

    private static final Double DEFAULT_ANOMALY_SCORE = 1D;
    private static final Double UPDATED_ANOMALY_SCORE = 2D;
    private static final Double SMALLER_ANOMALY_SCORE = 1D - 1D;

    private static final Boolean DEFAULT_IS_ANOMALY = false;
    private static final Boolean UPDATED_IS_ANOMALY = true;

    private static final String ENTITY_API_URL = "/api/medical-data";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MedicalDataRepository medicalDataRepository;

    @Autowired
    private MedicalDataMapper medicalDataMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMedicalDataMockMvc;

    private MedicalData medicalData;

    private MedicalData insertedMedicalData;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MedicalData createEntity(EntityManager em) {
        MedicalData medicalData = new MedicalData()
            .timestamp(DEFAULT_TIMESTAMP)
            .heartRate(DEFAULT_HEART_RATE)
            .spo2(DEFAULT_SPO_2)
            .temperature(DEFAULT_TEMPERATURE)
            .systolicBp(DEFAULT_SYSTOLIC_BP)
            .diastolicBp(DEFAULT_DIASTOLIC_BP)
            .hrv(DEFAULT_HRV)
            .qtInterval(DEFAULT_QT_INTERVAL)
            .bnp(DEFAULT_BNP)
            .bloodGlucose(DEFAULT_BLOOD_GLUCOSE)
            .respiratoryRate(DEFAULT_RESPIRATORY_RATE)
            .fev1(DEFAULT_FEV_1)
            .etco2(DEFAULT_ETCO_2)
            .anomalyScore(DEFAULT_ANOMALY_SCORE)
            .isAnomaly(DEFAULT_IS_ANOMALY);
        // Add required entity
        Patient patient;
        if (TestUtil.findAll(em, Patient.class).isEmpty()) {
            patient = PatientResourceIT.createEntity(em);
            em.persist(patient);
            em.flush();
        } else {
            patient = TestUtil.findAll(em, Patient.class).get(0);
        }
        medicalData.setPatient(patient);
        return medicalData;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MedicalData createUpdatedEntity(EntityManager em) {
        MedicalData updatedMedicalData = new MedicalData()
            .timestamp(UPDATED_TIMESTAMP)
            .heartRate(UPDATED_HEART_RATE)
            .spo2(UPDATED_SPO_2)
            .temperature(UPDATED_TEMPERATURE)
            .systolicBp(UPDATED_SYSTOLIC_BP)
            .diastolicBp(UPDATED_DIASTOLIC_BP)
            .hrv(UPDATED_HRV)
            .qtInterval(UPDATED_QT_INTERVAL)
            .bnp(UPDATED_BNP)
            .bloodGlucose(UPDATED_BLOOD_GLUCOSE)
            .respiratoryRate(UPDATED_RESPIRATORY_RATE)
            .fev1(UPDATED_FEV_1)
            .etco2(UPDATED_ETCO_2)
            .anomalyScore(UPDATED_ANOMALY_SCORE)
            .isAnomaly(UPDATED_IS_ANOMALY);
        // Add required entity
        Patient patient;
        if (TestUtil.findAll(em, Patient.class).isEmpty()) {
            patient = PatientResourceIT.createUpdatedEntity(em);
            em.persist(patient);
            em.flush();
        } else {
            patient = TestUtil.findAll(em, Patient.class).get(0);
        }
        updatedMedicalData.setPatient(patient);
        return updatedMedicalData;
    }

    @BeforeEach
    void initTest() {
        medicalData = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedMedicalData != null) {
            medicalDataRepository.delete(insertedMedicalData);
            insertedMedicalData = null;
        }
    }

    @Test
    @Transactional
    void createMedicalData() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the MedicalData
        MedicalDataDTO medicalDataDTO = medicalDataMapper.toDto(medicalData);
        var returnedMedicalDataDTO = om.readValue(
            restMedicalDataMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(medicalDataDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MedicalDataDTO.class
        );

        // Validate the MedicalData in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMedicalData = medicalDataMapper.toEntity(returnedMedicalDataDTO);
        assertMedicalDataUpdatableFieldsEquals(returnedMedicalData, getPersistedMedicalData(returnedMedicalData));

        insertedMedicalData = returnedMedicalData;
    }

    @Test
    @Transactional
    void createMedicalDataWithExistingId() throws Exception {
        // Create the MedicalData with an existing ID
        medicalData.setId(1L);
        MedicalDataDTO medicalDataDTO = medicalDataMapper.toDto(medicalData);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMedicalDataMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(medicalDataDTO)))
            .andExpect(status().isBadRequest());

        // Validate the MedicalData in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTimestampIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        medicalData.setTimestamp(null);

        // Create the MedicalData, which fails.
        MedicalDataDTO medicalDataDTO = medicalDataMapper.toDto(medicalData);

        restMedicalDataMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(medicalDataDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMedicalData() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList
        restMedicalDataMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(medicalData.getId().intValue())))
            .andExpect(jsonPath("$.[*].timestamp").value(hasItem(DEFAULT_TIMESTAMP.toString())))
            .andExpect(jsonPath("$.[*].heartRate").value(hasItem(DEFAULT_HEART_RATE)))
            .andExpect(jsonPath("$.[*].spo2").value(hasItem(DEFAULT_SPO_2)))
            .andExpect(jsonPath("$.[*].temperature").value(hasItem(DEFAULT_TEMPERATURE)))
            .andExpect(jsonPath("$.[*].systolicBp").value(hasItem(DEFAULT_SYSTOLIC_BP)))
            .andExpect(jsonPath("$.[*].diastolicBp").value(hasItem(DEFAULT_DIASTOLIC_BP)))
            .andExpect(jsonPath("$.[*].hrv").value(hasItem(DEFAULT_HRV)))
            .andExpect(jsonPath("$.[*].qtInterval").value(hasItem(DEFAULT_QT_INTERVAL)))
            .andExpect(jsonPath("$.[*].bnp").value(hasItem(DEFAULT_BNP)))
            .andExpect(jsonPath("$.[*].bloodGlucose").value(hasItem(DEFAULT_BLOOD_GLUCOSE)))
            .andExpect(jsonPath("$.[*].respiratoryRate").value(hasItem(DEFAULT_RESPIRATORY_RATE)))
            .andExpect(jsonPath("$.[*].fev1").value(hasItem(DEFAULT_FEV_1)))
            .andExpect(jsonPath("$.[*].etco2").value(hasItem(DEFAULT_ETCO_2)))
            .andExpect(jsonPath("$.[*].anomalyScore").value(hasItem(DEFAULT_ANOMALY_SCORE)))
            .andExpect(jsonPath("$.[*].isAnomaly").value(hasItem(DEFAULT_IS_ANOMALY)));
    }

    @Test
    @Transactional
    void getMedicalData() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get the medicalData
        restMedicalDataMockMvc
            .perform(get(ENTITY_API_URL_ID, medicalData.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(medicalData.getId().intValue()))
            .andExpect(jsonPath("$.timestamp").value(DEFAULT_TIMESTAMP.toString()))
            .andExpect(jsonPath("$.heartRate").value(DEFAULT_HEART_RATE))
            .andExpect(jsonPath("$.spo2").value(DEFAULT_SPO_2))
            .andExpect(jsonPath("$.temperature").value(DEFAULT_TEMPERATURE))
            .andExpect(jsonPath("$.systolicBp").value(DEFAULT_SYSTOLIC_BP))
            .andExpect(jsonPath("$.diastolicBp").value(DEFAULT_DIASTOLIC_BP))
            .andExpect(jsonPath("$.hrv").value(DEFAULT_HRV))
            .andExpect(jsonPath("$.qtInterval").value(DEFAULT_QT_INTERVAL))
            .andExpect(jsonPath("$.bnp").value(DEFAULT_BNP))
            .andExpect(jsonPath("$.bloodGlucose").value(DEFAULT_BLOOD_GLUCOSE))
            .andExpect(jsonPath("$.respiratoryRate").value(DEFAULT_RESPIRATORY_RATE))
            .andExpect(jsonPath("$.fev1").value(DEFAULT_FEV_1))
            .andExpect(jsonPath("$.etco2").value(DEFAULT_ETCO_2))
            .andExpect(jsonPath("$.anomalyScore").value(DEFAULT_ANOMALY_SCORE))
            .andExpect(jsonPath("$.isAnomaly").value(DEFAULT_IS_ANOMALY));
    }

    @Test
    @Transactional
    void getMedicalDataByIdFiltering() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        Long id = medicalData.getId();

        defaultMedicalDataFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultMedicalDataFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultMedicalDataFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllMedicalDataByTimestampIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where timestamp equals to
        defaultMedicalDataFiltering("timestamp.equals=" + DEFAULT_TIMESTAMP, "timestamp.equals=" + UPDATED_TIMESTAMP);
    }

    @Test
    @Transactional
    void getAllMedicalDataByTimestampIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where timestamp in
        defaultMedicalDataFiltering("timestamp.in=" + DEFAULT_TIMESTAMP + "," + UPDATED_TIMESTAMP, "timestamp.in=" + UPDATED_TIMESTAMP);
    }

    @Test
    @Transactional
    void getAllMedicalDataByTimestampIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where timestamp is not null
        defaultMedicalDataFiltering("timestamp.specified=true", "timestamp.specified=false");
    }

    @Test
    @Transactional
    void getAllMedicalDataByHeartRateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where heartRate equals to
        defaultMedicalDataFiltering("heartRate.equals=" + DEFAULT_HEART_RATE, "heartRate.equals=" + UPDATED_HEART_RATE);
    }

    @Test
    @Transactional
    void getAllMedicalDataByHeartRateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where heartRate in
        defaultMedicalDataFiltering("heartRate.in=" + DEFAULT_HEART_RATE + "," + UPDATED_HEART_RATE, "heartRate.in=" + UPDATED_HEART_RATE);
    }

    @Test
    @Transactional
    void getAllMedicalDataByHeartRateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where heartRate is not null
        defaultMedicalDataFiltering("heartRate.specified=true", "heartRate.specified=false");
    }

    @Test
    @Transactional
    void getAllMedicalDataByHeartRateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where heartRate is greater than or equal to
        defaultMedicalDataFiltering(
            "heartRate.greaterThanOrEqual=" + DEFAULT_HEART_RATE,
            "heartRate.greaterThanOrEqual=" + UPDATED_HEART_RATE
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByHeartRateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where heartRate is less than or equal to
        defaultMedicalDataFiltering("heartRate.lessThanOrEqual=" + DEFAULT_HEART_RATE, "heartRate.lessThanOrEqual=" + SMALLER_HEART_RATE);
    }

    @Test
    @Transactional
    void getAllMedicalDataByHeartRateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where heartRate is less than
        defaultMedicalDataFiltering("heartRate.lessThan=" + UPDATED_HEART_RATE, "heartRate.lessThan=" + DEFAULT_HEART_RATE);
    }

    @Test
    @Transactional
    void getAllMedicalDataByHeartRateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where heartRate is greater than
        defaultMedicalDataFiltering("heartRate.greaterThan=" + SMALLER_HEART_RATE, "heartRate.greaterThan=" + DEFAULT_HEART_RATE);
    }

    @Test
    @Transactional
    void getAllMedicalDataBySpo2IsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where spo2 equals to
        defaultMedicalDataFiltering("spo2.equals=" + DEFAULT_SPO_2, "spo2.equals=" + UPDATED_SPO_2);
    }

    @Test
    @Transactional
    void getAllMedicalDataBySpo2IsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where spo2 in
        defaultMedicalDataFiltering("spo2.in=" + DEFAULT_SPO_2 + "," + UPDATED_SPO_2, "spo2.in=" + UPDATED_SPO_2);
    }

    @Test
    @Transactional
    void getAllMedicalDataBySpo2IsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where spo2 is not null
        defaultMedicalDataFiltering("spo2.specified=true", "spo2.specified=false");
    }

    @Test
    @Transactional
    void getAllMedicalDataBySpo2IsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where spo2 is greater than or equal to
        defaultMedicalDataFiltering("spo2.greaterThanOrEqual=" + DEFAULT_SPO_2, "spo2.greaterThanOrEqual=" + UPDATED_SPO_2);
    }

    @Test
    @Transactional
    void getAllMedicalDataBySpo2IsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where spo2 is less than or equal to
        defaultMedicalDataFiltering("spo2.lessThanOrEqual=" + DEFAULT_SPO_2, "spo2.lessThanOrEqual=" + SMALLER_SPO_2);
    }

    @Test
    @Transactional
    void getAllMedicalDataBySpo2IsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where spo2 is less than
        defaultMedicalDataFiltering("spo2.lessThan=" + UPDATED_SPO_2, "spo2.lessThan=" + DEFAULT_SPO_2);
    }

    @Test
    @Transactional
    void getAllMedicalDataBySpo2IsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where spo2 is greater than
        defaultMedicalDataFiltering("spo2.greaterThan=" + SMALLER_SPO_2, "spo2.greaterThan=" + DEFAULT_SPO_2);
    }

    @Test
    @Transactional
    void getAllMedicalDataByTemperatureIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where temperature equals to
        defaultMedicalDataFiltering("temperature.equals=" + DEFAULT_TEMPERATURE, "temperature.equals=" + UPDATED_TEMPERATURE);
    }

    @Test
    @Transactional
    void getAllMedicalDataByTemperatureIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where temperature in
        defaultMedicalDataFiltering(
            "temperature.in=" + DEFAULT_TEMPERATURE + "," + UPDATED_TEMPERATURE,
            "temperature.in=" + UPDATED_TEMPERATURE
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByTemperatureIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where temperature is not null
        defaultMedicalDataFiltering("temperature.specified=true", "temperature.specified=false");
    }

    @Test
    @Transactional
    void getAllMedicalDataByTemperatureIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where temperature is greater than or equal to
        defaultMedicalDataFiltering(
            "temperature.greaterThanOrEqual=" + DEFAULT_TEMPERATURE,
            "temperature.greaterThanOrEqual=" + UPDATED_TEMPERATURE
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByTemperatureIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where temperature is less than or equal to
        defaultMedicalDataFiltering(
            "temperature.lessThanOrEqual=" + DEFAULT_TEMPERATURE,
            "temperature.lessThanOrEqual=" + SMALLER_TEMPERATURE
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByTemperatureIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where temperature is less than
        defaultMedicalDataFiltering("temperature.lessThan=" + UPDATED_TEMPERATURE, "temperature.lessThan=" + DEFAULT_TEMPERATURE);
    }

    @Test
    @Transactional
    void getAllMedicalDataByTemperatureIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where temperature is greater than
        defaultMedicalDataFiltering("temperature.greaterThan=" + SMALLER_TEMPERATURE, "temperature.greaterThan=" + DEFAULT_TEMPERATURE);
    }

    @Test
    @Transactional
    void getAllMedicalDataBySystolicBpIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where systolicBp equals to
        defaultMedicalDataFiltering("systolicBp.equals=" + DEFAULT_SYSTOLIC_BP, "systolicBp.equals=" + UPDATED_SYSTOLIC_BP);
    }

    @Test
    @Transactional
    void getAllMedicalDataBySystolicBpIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where systolicBp in
        defaultMedicalDataFiltering(
            "systolicBp.in=" + DEFAULT_SYSTOLIC_BP + "," + UPDATED_SYSTOLIC_BP,
            "systolicBp.in=" + UPDATED_SYSTOLIC_BP
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataBySystolicBpIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where systolicBp is not null
        defaultMedicalDataFiltering("systolicBp.specified=true", "systolicBp.specified=false");
    }

    @Test
    @Transactional
    void getAllMedicalDataBySystolicBpIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where systolicBp is greater than or equal to
        defaultMedicalDataFiltering(
            "systolicBp.greaterThanOrEqual=" + DEFAULT_SYSTOLIC_BP,
            "systolicBp.greaterThanOrEqual=" + UPDATED_SYSTOLIC_BP
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataBySystolicBpIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where systolicBp is less than or equal to
        defaultMedicalDataFiltering(
            "systolicBp.lessThanOrEqual=" + DEFAULT_SYSTOLIC_BP,
            "systolicBp.lessThanOrEqual=" + SMALLER_SYSTOLIC_BP
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataBySystolicBpIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where systolicBp is less than
        defaultMedicalDataFiltering("systolicBp.lessThan=" + UPDATED_SYSTOLIC_BP, "systolicBp.lessThan=" + DEFAULT_SYSTOLIC_BP);
    }

    @Test
    @Transactional
    void getAllMedicalDataBySystolicBpIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where systolicBp is greater than
        defaultMedicalDataFiltering("systolicBp.greaterThan=" + SMALLER_SYSTOLIC_BP, "systolicBp.greaterThan=" + DEFAULT_SYSTOLIC_BP);
    }

    @Test
    @Transactional
    void getAllMedicalDataByDiastolicBpIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where diastolicBp equals to
        defaultMedicalDataFiltering("diastolicBp.equals=" + DEFAULT_DIASTOLIC_BP, "diastolicBp.equals=" + UPDATED_DIASTOLIC_BP);
    }

    @Test
    @Transactional
    void getAllMedicalDataByDiastolicBpIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where diastolicBp in
        defaultMedicalDataFiltering(
            "diastolicBp.in=" + DEFAULT_DIASTOLIC_BP + "," + UPDATED_DIASTOLIC_BP,
            "diastolicBp.in=" + UPDATED_DIASTOLIC_BP
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByDiastolicBpIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where diastolicBp is not null
        defaultMedicalDataFiltering("diastolicBp.specified=true", "diastolicBp.specified=false");
    }

    @Test
    @Transactional
    void getAllMedicalDataByDiastolicBpIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where diastolicBp is greater than or equal to
        defaultMedicalDataFiltering(
            "diastolicBp.greaterThanOrEqual=" + DEFAULT_DIASTOLIC_BP,
            "diastolicBp.greaterThanOrEqual=" + UPDATED_DIASTOLIC_BP
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByDiastolicBpIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where diastolicBp is less than or equal to
        defaultMedicalDataFiltering(
            "diastolicBp.lessThanOrEqual=" + DEFAULT_DIASTOLIC_BP,
            "diastolicBp.lessThanOrEqual=" + SMALLER_DIASTOLIC_BP
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByDiastolicBpIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where diastolicBp is less than
        defaultMedicalDataFiltering("diastolicBp.lessThan=" + UPDATED_DIASTOLIC_BP, "diastolicBp.lessThan=" + DEFAULT_DIASTOLIC_BP);
    }

    @Test
    @Transactional
    void getAllMedicalDataByDiastolicBpIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where diastolicBp is greater than
        defaultMedicalDataFiltering("diastolicBp.greaterThan=" + SMALLER_DIASTOLIC_BP, "diastolicBp.greaterThan=" + DEFAULT_DIASTOLIC_BP);
    }

    @Test
    @Transactional
    void getAllMedicalDataByHrvIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where hrv equals to
        defaultMedicalDataFiltering("hrv.equals=" + DEFAULT_HRV, "hrv.equals=" + UPDATED_HRV);
    }

    @Test
    @Transactional
    void getAllMedicalDataByHrvIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where hrv in
        defaultMedicalDataFiltering("hrv.in=" + DEFAULT_HRV + "," + UPDATED_HRV, "hrv.in=" + UPDATED_HRV);
    }

    @Test
    @Transactional
    void getAllMedicalDataByHrvIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where hrv is not null
        defaultMedicalDataFiltering("hrv.specified=true", "hrv.specified=false");
    }

    @Test
    @Transactional
    void getAllMedicalDataByHrvIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where hrv is greater than or equal to
        defaultMedicalDataFiltering("hrv.greaterThanOrEqual=" + DEFAULT_HRV, "hrv.greaterThanOrEqual=" + UPDATED_HRV);
    }

    @Test
    @Transactional
    void getAllMedicalDataByHrvIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where hrv is less than or equal to
        defaultMedicalDataFiltering("hrv.lessThanOrEqual=" + DEFAULT_HRV, "hrv.lessThanOrEqual=" + SMALLER_HRV);
    }

    @Test
    @Transactional
    void getAllMedicalDataByHrvIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where hrv is less than
        defaultMedicalDataFiltering("hrv.lessThan=" + UPDATED_HRV, "hrv.lessThan=" + DEFAULT_HRV);
    }

    @Test
    @Transactional
    void getAllMedicalDataByHrvIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where hrv is greater than
        defaultMedicalDataFiltering("hrv.greaterThan=" + SMALLER_HRV, "hrv.greaterThan=" + DEFAULT_HRV);
    }

    @Test
    @Transactional
    void getAllMedicalDataByQtIntervalIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where qtInterval equals to
        defaultMedicalDataFiltering("qtInterval.equals=" + DEFAULT_QT_INTERVAL, "qtInterval.equals=" + UPDATED_QT_INTERVAL);
    }

    @Test
    @Transactional
    void getAllMedicalDataByQtIntervalIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where qtInterval in
        defaultMedicalDataFiltering(
            "qtInterval.in=" + DEFAULT_QT_INTERVAL + "," + UPDATED_QT_INTERVAL,
            "qtInterval.in=" + UPDATED_QT_INTERVAL
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByQtIntervalIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where qtInterval is not null
        defaultMedicalDataFiltering("qtInterval.specified=true", "qtInterval.specified=false");
    }

    @Test
    @Transactional
    void getAllMedicalDataByQtIntervalIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where qtInterval is greater than or equal to
        defaultMedicalDataFiltering(
            "qtInterval.greaterThanOrEqual=" + DEFAULT_QT_INTERVAL,
            "qtInterval.greaterThanOrEqual=" + UPDATED_QT_INTERVAL
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByQtIntervalIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where qtInterval is less than or equal to
        defaultMedicalDataFiltering(
            "qtInterval.lessThanOrEqual=" + DEFAULT_QT_INTERVAL,
            "qtInterval.lessThanOrEqual=" + SMALLER_QT_INTERVAL
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByQtIntervalIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where qtInterval is less than
        defaultMedicalDataFiltering("qtInterval.lessThan=" + UPDATED_QT_INTERVAL, "qtInterval.lessThan=" + DEFAULT_QT_INTERVAL);
    }

    @Test
    @Transactional
    void getAllMedicalDataByQtIntervalIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where qtInterval is greater than
        defaultMedicalDataFiltering("qtInterval.greaterThan=" + SMALLER_QT_INTERVAL, "qtInterval.greaterThan=" + DEFAULT_QT_INTERVAL);
    }

    @Test
    @Transactional
    void getAllMedicalDataByBnpIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where bnp equals to
        defaultMedicalDataFiltering("bnp.equals=" + DEFAULT_BNP, "bnp.equals=" + UPDATED_BNP);
    }

    @Test
    @Transactional
    void getAllMedicalDataByBnpIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where bnp in
        defaultMedicalDataFiltering("bnp.in=" + DEFAULT_BNP + "," + UPDATED_BNP, "bnp.in=" + UPDATED_BNP);
    }

    @Test
    @Transactional
    void getAllMedicalDataByBnpIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where bnp is not null
        defaultMedicalDataFiltering("bnp.specified=true", "bnp.specified=false");
    }

    @Test
    @Transactional
    void getAllMedicalDataByBnpIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where bnp is greater than or equal to
        defaultMedicalDataFiltering("bnp.greaterThanOrEqual=" + DEFAULT_BNP, "bnp.greaterThanOrEqual=" + UPDATED_BNP);
    }

    @Test
    @Transactional
    void getAllMedicalDataByBnpIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where bnp is less than or equal to
        defaultMedicalDataFiltering("bnp.lessThanOrEqual=" + DEFAULT_BNP, "bnp.lessThanOrEqual=" + SMALLER_BNP);
    }

    @Test
    @Transactional
    void getAllMedicalDataByBnpIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where bnp is less than
        defaultMedicalDataFiltering("bnp.lessThan=" + UPDATED_BNP, "bnp.lessThan=" + DEFAULT_BNP);
    }

    @Test
    @Transactional
    void getAllMedicalDataByBnpIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where bnp is greater than
        defaultMedicalDataFiltering("bnp.greaterThan=" + SMALLER_BNP, "bnp.greaterThan=" + DEFAULT_BNP);
    }

    @Test
    @Transactional
    void getAllMedicalDataByBloodGlucoseIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where bloodGlucose equals to
        defaultMedicalDataFiltering("bloodGlucose.equals=" + DEFAULT_BLOOD_GLUCOSE, "bloodGlucose.equals=" + UPDATED_BLOOD_GLUCOSE);
    }

    @Test
    @Transactional
    void getAllMedicalDataByBloodGlucoseIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where bloodGlucose in
        defaultMedicalDataFiltering(
            "bloodGlucose.in=" + DEFAULT_BLOOD_GLUCOSE + "," + UPDATED_BLOOD_GLUCOSE,
            "bloodGlucose.in=" + UPDATED_BLOOD_GLUCOSE
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByBloodGlucoseIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where bloodGlucose is not null
        defaultMedicalDataFiltering("bloodGlucose.specified=true", "bloodGlucose.specified=false");
    }

    @Test
    @Transactional
    void getAllMedicalDataByBloodGlucoseIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where bloodGlucose is greater than or equal to
        defaultMedicalDataFiltering(
            "bloodGlucose.greaterThanOrEqual=" + DEFAULT_BLOOD_GLUCOSE,
            "bloodGlucose.greaterThanOrEqual=" + UPDATED_BLOOD_GLUCOSE
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByBloodGlucoseIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where bloodGlucose is less than or equal to
        defaultMedicalDataFiltering(
            "bloodGlucose.lessThanOrEqual=" + DEFAULT_BLOOD_GLUCOSE,
            "bloodGlucose.lessThanOrEqual=" + SMALLER_BLOOD_GLUCOSE
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByBloodGlucoseIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where bloodGlucose is less than
        defaultMedicalDataFiltering("bloodGlucose.lessThan=" + UPDATED_BLOOD_GLUCOSE, "bloodGlucose.lessThan=" + DEFAULT_BLOOD_GLUCOSE);
    }

    @Test
    @Transactional
    void getAllMedicalDataByBloodGlucoseIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where bloodGlucose is greater than
        defaultMedicalDataFiltering(
            "bloodGlucose.greaterThan=" + SMALLER_BLOOD_GLUCOSE,
            "bloodGlucose.greaterThan=" + DEFAULT_BLOOD_GLUCOSE
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByRespiratoryRateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where respiratoryRate equals to
        defaultMedicalDataFiltering(
            "respiratoryRate.equals=" + DEFAULT_RESPIRATORY_RATE,
            "respiratoryRate.equals=" + UPDATED_RESPIRATORY_RATE
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByRespiratoryRateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where respiratoryRate in
        defaultMedicalDataFiltering(
            "respiratoryRate.in=" + DEFAULT_RESPIRATORY_RATE + "," + UPDATED_RESPIRATORY_RATE,
            "respiratoryRate.in=" + UPDATED_RESPIRATORY_RATE
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByRespiratoryRateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where respiratoryRate is not null
        defaultMedicalDataFiltering("respiratoryRate.specified=true", "respiratoryRate.specified=false");
    }

    @Test
    @Transactional
    void getAllMedicalDataByRespiratoryRateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where respiratoryRate is greater than or equal to
        defaultMedicalDataFiltering(
            "respiratoryRate.greaterThanOrEqual=" + DEFAULT_RESPIRATORY_RATE,
            "respiratoryRate.greaterThanOrEqual=" + UPDATED_RESPIRATORY_RATE
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByRespiratoryRateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where respiratoryRate is less than or equal to
        defaultMedicalDataFiltering(
            "respiratoryRate.lessThanOrEqual=" + DEFAULT_RESPIRATORY_RATE,
            "respiratoryRate.lessThanOrEqual=" + SMALLER_RESPIRATORY_RATE
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByRespiratoryRateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where respiratoryRate is less than
        defaultMedicalDataFiltering(
            "respiratoryRate.lessThan=" + UPDATED_RESPIRATORY_RATE,
            "respiratoryRate.lessThan=" + DEFAULT_RESPIRATORY_RATE
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByRespiratoryRateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where respiratoryRate is greater than
        defaultMedicalDataFiltering(
            "respiratoryRate.greaterThan=" + SMALLER_RESPIRATORY_RATE,
            "respiratoryRate.greaterThan=" + DEFAULT_RESPIRATORY_RATE
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByFev1IsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where fev1 equals to
        defaultMedicalDataFiltering("fev1.equals=" + DEFAULT_FEV_1, "fev1.equals=" + UPDATED_FEV_1);
    }

    @Test
    @Transactional
    void getAllMedicalDataByFev1IsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where fev1 in
        defaultMedicalDataFiltering("fev1.in=" + DEFAULT_FEV_1 + "," + UPDATED_FEV_1, "fev1.in=" + UPDATED_FEV_1);
    }

    @Test
    @Transactional
    void getAllMedicalDataByFev1IsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where fev1 is not null
        defaultMedicalDataFiltering("fev1.specified=true", "fev1.specified=false");
    }

    @Test
    @Transactional
    void getAllMedicalDataByFev1IsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where fev1 is greater than or equal to
        defaultMedicalDataFiltering("fev1.greaterThanOrEqual=" + DEFAULT_FEV_1, "fev1.greaterThanOrEqual=" + UPDATED_FEV_1);
    }

    @Test
    @Transactional
    void getAllMedicalDataByFev1IsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where fev1 is less than or equal to
        defaultMedicalDataFiltering("fev1.lessThanOrEqual=" + DEFAULT_FEV_1, "fev1.lessThanOrEqual=" + SMALLER_FEV_1);
    }

    @Test
    @Transactional
    void getAllMedicalDataByFev1IsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where fev1 is less than
        defaultMedicalDataFiltering("fev1.lessThan=" + UPDATED_FEV_1, "fev1.lessThan=" + DEFAULT_FEV_1);
    }

    @Test
    @Transactional
    void getAllMedicalDataByFev1IsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where fev1 is greater than
        defaultMedicalDataFiltering("fev1.greaterThan=" + SMALLER_FEV_1, "fev1.greaterThan=" + DEFAULT_FEV_1);
    }

    @Test
    @Transactional
    void getAllMedicalDataByEtco2IsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where etco2 equals to
        defaultMedicalDataFiltering("etco2.equals=" + DEFAULT_ETCO_2, "etco2.equals=" + UPDATED_ETCO_2);
    }

    @Test
    @Transactional
    void getAllMedicalDataByEtco2IsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where etco2 in
        defaultMedicalDataFiltering("etco2.in=" + DEFAULT_ETCO_2 + "," + UPDATED_ETCO_2, "etco2.in=" + UPDATED_ETCO_2);
    }

    @Test
    @Transactional
    void getAllMedicalDataByEtco2IsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where etco2 is not null
        defaultMedicalDataFiltering("etco2.specified=true", "etco2.specified=false");
    }

    @Test
    @Transactional
    void getAllMedicalDataByEtco2IsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where etco2 is greater than or equal to
        defaultMedicalDataFiltering("etco2.greaterThanOrEqual=" + DEFAULT_ETCO_2, "etco2.greaterThanOrEqual=" + UPDATED_ETCO_2);
    }

    @Test
    @Transactional
    void getAllMedicalDataByEtco2IsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where etco2 is less than or equal to
        defaultMedicalDataFiltering("etco2.lessThanOrEqual=" + DEFAULT_ETCO_2, "etco2.lessThanOrEqual=" + SMALLER_ETCO_2);
    }

    @Test
    @Transactional
    void getAllMedicalDataByEtco2IsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where etco2 is less than
        defaultMedicalDataFiltering("etco2.lessThan=" + UPDATED_ETCO_2, "etco2.lessThan=" + DEFAULT_ETCO_2);
    }

    @Test
    @Transactional
    void getAllMedicalDataByEtco2IsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where etco2 is greater than
        defaultMedicalDataFiltering("etco2.greaterThan=" + SMALLER_ETCO_2, "etco2.greaterThan=" + DEFAULT_ETCO_2);
    }

    @Test
    @Transactional
    void getAllMedicalDataByAnomalyScoreIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where anomalyScore equals to
        defaultMedicalDataFiltering("anomalyScore.equals=" + DEFAULT_ANOMALY_SCORE, "anomalyScore.equals=" + UPDATED_ANOMALY_SCORE);
    }

    @Test
    @Transactional
    void getAllMedicalDataByAnomalyScoreIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where anomalyScore in
        defaultMedicalDataFiltering(
            "anomalyScore.in=" + DEFAULT_ANOMALY_SCORE + "," + UPDATED_ANOMALY_SCORE,
            "anomalyScore.in=" + UPDATED_ANOMALY_SCORE
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByAnomalyScoreIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where anomalyScore is not null
        defaultMedicalDataFiltering("anomalyScore.specified=true", "anomalyScore.specified=false");
    }

    @Test
    @Transactional
    void getAllMedicalDataByAnomalyScoreIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where anomalyScore is greater than or equal to
        defaultMedicalDataFiltering(
            "anomalyScore.greaterThanOrEqual=" + DEFAULT_ANOMALY_SCORE,
            "anomalyScore.greaterThanOrEqual=" + UPDATED_ANOMALY_SCORE
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByAnomalyScoreIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where anomalyScore is less than or equal to
        defaultMedicalDataFiltering(
            "anomalyScore.lessThanOrEqual=" + DEFAULT_ANOMALY_SCORE,
            "anomalyScore.lessThanOrEqual=" + SMALLER_ANOMALY_SCORE
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByAnomalyScoreIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where anomalyScore is less than
        defaultMedicalDataFiltering("anomalyScore.lessThan=" + UPDATED_ANOMALY_SCORE, "anomalyScore.lessThan=" + DEFAULT_ANOMALY_SCORE);
    }

    @Test
    @Transactional
    void getAllMedicalDataByAnomalyScoreIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where anomalyScore is greater than
        defaultMedicalDataFiltering(
            "anomalyScore.greaterThan=" + SMALLER_ANOMALY_SCORE,
            "anomalyScore.greaterThan=" + DEFAULT_ANOMALY_SCORE
        );
    }

    @Test
    @Transactional
    void getAllMedicalDataByIsAnomalyIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where isAnomaly equals to
        defaultMedicalDataFiltering("isAnomaly.equals=" + DEFAULT_IS_ANOMALY, "isAnomaly.equals=" + UPDATED_IS_ANOMALY);
    }

    @Test
    @Transactional
    void getAllMedicalDataByIsAnomalyIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where isAnomaly in
        defaultMedicalDataFiltering("isAnomaly.in=" + DEFAULT_IS_ANOMALY + "," + UPDATED_IS_ANOMALY, "isAnomaly.in=" + UPDATED_IS_ANOMALY);
    }

    @Test
    @Transactional
    void getAllMedicalDataByIsAnomalyIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        // Get all the medicalDataList where isAnomaly is not null
        defaultMedicalDataFiltering("isAnomaly.specified=true", "isAnomaly.specified=false");
    }

    @Test
    @Transactional
    void getAllMedicalDataByPatientIsEqualToSomething() throws Exception {
        Patient patient;
        if (TestUtil.findAll(em, Patient.class).isEmpty()) {
            medicalDataRepository.saveAndFlush(medicalData);
            patient = PatientResourceIT.createEntity(em);
        } else {
            patient = TestUtil.findAll(em, Patient.class).get(0);
        }
        em.persist(patient);
        em.flush();
        medicalData.setPatient(patient);
        medicalDataRepository.saveAndFlush(medicalData);
        Long patientId = patient.getId();
        // Get all the medicalDataList where patient equals to patientId
        defaultMedicalDataShouldBeFound("patientId.equals=" + patientId);

        // Get all the medicalDataList where patient equals to (patientId + 1)
        defaultMedicalDataShouldNotBeFound("patientId.equals=" + (patientId + 1));
    }

    private void defaultMedicalDataFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultMedicalDataShouldBeFound(shouldBeFound);
        defaultMedicalDataShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultMedicalDataShouldBeFound(String filter) throws Exception {
        restMedicalDataMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(medicalData.getId().intValue())))
            .andExpect(jsonPath("$.[*].timestamp").value(hasItem(DEFAULT_TIMESTAMP.toString())))
            .andExpect(jsonPath("$.[*].heartRate").value(hasItem(DEFAULT_HEART_RATE)))
            .andExpect(jsonPath("$.[*].spo2").value(hasItem(DEFAULT_SPO_2)))
            .andExpect(jsonPath("$.[*].temperature").value(hasItem(DEFAULT_TEMPERATURE)))
            .andExpect(jsonPath("$.[*].systolicBp").value(hasItem(DEFAULT_SYSTOLIC_BP)))
            .andExpect(jsonPath("$.[*].diastolicBp").value(hasItem(DEFAULT_DIASTOLIC_BP)))
            .andExpect(jsonPath("$.[*].hrv").value(hasItem(DEFAULT_HRV)))
            .andExpect(jsonPath("$.[*].qtInterval").value(hasItem(DEFAULT_QT_INTERVAL)))
            .andExpect(jsonPath("$.[*].bnp").value(hasItem(DEFAULT_BNP)))
            .andExpect(jsonPath("$.[*].bloodGlucose").value(hasItem(DEFAULT_BLOOD_GLUCOSE)))
            .andExpect(jsonPath("$.[*].respiratoryRate").value(hasItem(DEFAULT_RESPIRATORY_RATE)))
            .andExpect(jsonPath("$.[*].fev1").value(hasItem(DEFAULT_FEV_1)))
            .andExpect(jsonPath("$.[*].etco2").value(hasItem(DEFAULT_ETCO_2)))
            .andExpect(jsonPath("$.[*].anomalyScore").value(hasItem(DEFAULT_ANOMALY_SCORE)))
            .andExpect(jsonPath("$.[*].isAnomaly").value(hasItem(DEFAULT_IS_ANOMALY)));

        // Check, that the count call also returns 1
        restMedicalDataMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultMedicalDataShouldNotBeFound(String filter) throws Exception {
        restMedicalDataMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restMedicalDataMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingMedicalData() throws Exception {
        // Get the medicalData
        restMedicalDataMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMedicalData() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the medicalData
        MedicalData updatedMedicalData = medicalDataRepository.findById(medicalData.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMedicalData are not directly saved in db
        em.detach(updatedMedicalData);
        updatedMedicalData
            .timestamp(UPDATED_TIMESTAMP)
            .heartRate(UPDATED_HEART_RATE)
            .spo2(UPDATED_SPO_2)
            .temperature(UPDATED_TEMPERATURE)
            .systolicBp(UPDATED_SYSTOLIC_BP)
            .diastolicBp(UPDATED_DIASTOLIC_BP)
            .hrv(UPDATED_HRV)
            .qtInterval(UPDATED_QT_INTERVAL)
            .bnp(UPDATED_BNP)
            .bloodGlucose(UPDATED_BLOOD_GLUCOSE)
            .respiratoryRate(UPDATED_RESPIRATORY_RATE)
            .fev1(UPDATED_FEV_1)
            .etco2(UPDATED_ETCO_2)
            .anomalyScore(UPDATED_ANOMALY_SCORE)
            .isAnomaly(UPDATED_IS_ANOMALY);
        MedicalDataDTO medicalDataDTO = medicalDataMapper.toDto(updatedMedicalData);

        restMedicalDataMockMvc
            .perform(
                put(ENTITY_API_URL_ID, medicalDataDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(medicalDataDTO))
            )
            .andExpect(status().isOk());

        // Validate the MedicalData in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMedicalDataToMatchAllProperties(updatedMedicalData);
    }

    @Test
    @Transactional
    void putNonExistingMedicalData() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        medicalData.setId(longCount.incrementAndGet());

        // Create the MedicalData
        MedicalDataDTO medicalDataDTO = medicalDataMapper.toDto(medicalData);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMedicalDataMockMvc
            .perform(
                put(ENTITY_API_URL_ID, medicalDataDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(medicalDataDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MedicalData in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMedicalData() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        medicalData.setId(longCount.incrementAndGet());

        // Create the MedicalData
        MedicalDataDTO medicalDataDTO = medicalDataMapper.toDto(medicalData);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMedicalDataMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(medicalDataDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MedicalData in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMedicalData() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        medicalData.setId(longCount.incrementAndGet());

        // Create the MedicalData
        MedicalDataDTO medicalDataDTO = medicalDataMapper.toDto(medicalData);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMedicalDataMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(medicalDataDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MedicalData in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMedicalDataWithPatch() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the medicalData using partial update
        MedicalData partialUpdatedMedicalData = new MedicalData();
        partialUpdatedMedicalData.setId(medicalData.getId());

        partialUpdatedMedicalData
            .timestamp(UPDATED_TIMESTAMP)
            .temperature(UPDATED_TEMPERATURE)
            .diastolicBp(UPDATED_DIASTOLIC_BP)
            .qtInterval(UPDATED_QT_INTERVAL)
            .respiratoryRate(UPDATED_RESPIRATORY_RATE)
            .fev1(UPDATED_FEV_1)
            .etco2(UPDATED_ETCO_2)
            .anomalyScore(UPDATED_ANOMALY_SCORE)
            .isAnomaly(UPDATED_IS_ANOMALY);

        restMedicalDataMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMedicalData.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMedicalData))
            )
            .andExpect(status().isOk());

        // Validate the MedicalData in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMedicalDataUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedMedicalData, medicalData),
            getPersistedMedicalData(medicalData)
        );
    }

    @Test
    @Transactional
    void fullUpdateMedicalDataWithPatch() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the medicalData using partial update
        MedicalData partialUpdatedMedicalData = new MedicalData();
        partialUpdatedMedicalData.setId(medicalData.getId());

        partialUpdatedMedicalData
            .timestamp(UPDATED_TIMESTAMP)
            .heartRate(UPDATED_HEART_RATE)
            .spo2(UPDATED_SPO_2)
            .temperature(UPDATED_TEMPERATURE)
            .systolicBp(UPDATED_SYSTOLIC_BP)
            .diastolicBp(UPDATED_DIASTOLIC_BP)
            .hrv(UPDATED_HRV)
            .qtInterval(UPDATED_QT_INTERVAL)
            .bnp(UPDATED_BNP)
            .bloodGlucose(UPDATED_BLOOD_GLUCOSE)
            .respiratoryRate(UPDATED_RESPIRATORY_RATE)
            .fev1(UPDATED_FEV_1)
            .etco2(UPDATED_ETCO_2)
            .anomalyScore(UPDATED_ANOMALY_SCORE)
            .isAnomaly(UPDATED_IS_ANOMALY);

        restMedicalDataMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMedicalData.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMedicalData))
            )
            .andExpect(status().isOk());

        // Validate the MedicalData in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMedicalDataUpdatableFieldsEquals(partialUpdatedMedicalData, getPersistedMedicalData(partialUpdatedMedicalData));
    }

    @Test
    @Transactional
    void patchNonExistingMedicalData() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        medicalData.setId(longCount.incrementAndGet());

        // Create the MedicalData
        MedicalDataDTO medicalDataDTO = medicalDataMapper.toDto(medicalData);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMedicalDataMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, medicalDataDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(medicalDataDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MedicalData in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMedicalData() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        medicalData.setId(longCount.incrementAndGet());

        // Create the MedicalData
        MedicalDataDTO medicalDataDTO = medicalDataMapper.toDto(medicalData);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMedicalDataMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(medicalDataDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MedicalData in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMedicalData() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        medicalData.setId(longCount.incrementAndGet());

        // Create the MedicalData
        MedicalDataDTO medicalDataDTO = medicalDataMapper.toDto(medicalData);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMedicalDataMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(medicalDataDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MedicalData in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMedicalData() throws Exception {
        // Initialize the database
        insertedMedicalData = medicalDataRepository.saveAndFlush(medicalData);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the medicalData
        restMedicalDataMockMvc
            .perform(delete(ENTITY_API_URL_ID, medicalData.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return medicalDataRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected MedicalData getPersistedMedicalData(MedicalData medicalData) {
        return medicalDataRepository.findById(medicalData.getId()).orElseThrow();
    }

    protected void assertPersistedMedicalDataToMatchAllProperties(MedicalData expectedMedicalData) {
        assertMedicalDataAllPropertiesEquals(expectedMedicalData, getPersistedMedicalData(expectedMedicalData));
    }

    protected void assertPersistedMedicalDataToMatchUpdatableProperties(MedicalData expectedMedicalData) {
        assertMedicalDataAllUpdatablePropertiesEquals(expectedMedicalData, getPersistedMedicalData(expectedMedicalData));
    }
}
