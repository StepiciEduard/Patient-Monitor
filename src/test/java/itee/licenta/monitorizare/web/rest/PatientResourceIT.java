package itee.licenta.monitorizare.web.rest;

import static itee.licenta.monitorizare.domain.PatientAsserts.*;
import static itee.licenta.monitorizare.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import itee.licenta.monitorizare.IntegrationTest;
import itee.licenta.monitorizare.domain.Doctor;
import itee.licenta.monitorizare.domain.Patient;
import itee.licenta.monitorizare.domain.User;
import itee.licenta.monitorizare.domain.enumeration.PatientSubtype;
import itee.licenta.monitorizare.domain.enumeration.PatientType;
import itee.licenta.monitorizare.repository.PatientRepository;
import itee.licenta.monitorizare.repository.UserRepository;
import itee.licenta.monitorizare.service.PatientService;
import itee.licenta.monitorizare.service.dto.PatientDTO;
import itee.licenta.monitorizare.service.mapper.PatientMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link PatientResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class PatientResourceIT {

    private static final String DEFAULT_CNP = "AAAAAAAAAAAAA";
    private static final String UPDATED_CNP = "BBBBBBBBBBBBB";

    private static final String DEFAULT_PHONE_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_PHONE_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS = "BBBBBBBBBB";

    private static final PatientType DEFAULT_PATIENT_TYPE = PatientType.CARDIAC;
    private static final PatientType UPDATED_PATIENT_TYPE = PatientType.DIABETES;

    private static final PatientSubtype DEFAULT_PATIENT_SUBTYPE = PatientSubtype.STABLE;
    private static final PatientSubtype UPDATED_PATIENT_SUBTYPE = PatientSubtype.BORDERLINE;

    private static final LocalDate DEFAULT_DATE_OF_BIRTH = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE_OF_BIRTH = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_DATE_OF_BIRTH = LocalDate.ofEpochDay(-1L);

    private static final String DEFAULT_GENDER = "AAAAAAAAAA";
    private static final String UPDATED_GENDER = "BBBBBBBBBB";

    private static final Double DEFAULT_HBA_1_C = 1D;
    private static final Double UPDATED_HBA_1_C = 2D;
    private static final Double SMALLER_HBA_1_C = 1D - 1D;

    private static final Double DEFAULT_BMI = 1D;
    private static final Double UPDATED_BMI = 2D;
    private static final Double SMALLER_BMI = 1D - 1D;

    private static final Double DEFAULT_FEV_1_BASELINE = 1D;
    private static final Double UPDATED_FEV_1_BASELINE = 2D;
    private static final Double SMALLER_FEV_1_BASELINE = 1D - 1D;

    private static final String ENTITY_API_URL = "/api/patients";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private PatientRepository patientRepositoryMock;

    @Autowired
    private PatientMapper patientMapper;

    @Mock
    private PatientService patientServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPatientMockMvc;

    private Patient patient;

    private Patient insertedPatient;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Patient createEntity(EntityManager em) {
        Patient patient = new Patient()
            .cnp(DEFAULT_CNP)
            .phoneNumber(DEFAULT_PHONE_NUMBER)
            .address(DEFAULT_ADDRESS)
            .patientType(DEFAULT_PATIENT_TYPE)
            .patientSubtype(DEFAULT_PATIENT_SUBTYPE)
            .dateOfBirth(DEFAULT_DATE_OF_BIRTH)
            .gender(DEFAULT_GENDER)
            .hba1c(DEFAULT_HBA_1_C)
            .bmi(DEFAULT_BMI)
            .fev1Baseline(DEFAULT_FEV_1_BASELINE);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        patient.setUser(user);
        // Add required entity
        Doctor doctor;
        if (TestUtil.findAll(em, Doctor.class).isEmpty()) {
            doctor = DoctorResourceIT.createEntity(em);
            em.persist(doctor);
            em.flush();
        } else {
            doctor = TestUtil.findAll(em, Doctor.class).get(0);
        }
        patient.setDoctor(doctor);
        return patient;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Patient createUpdatedEntity(EntityManager em) {
        Patient updatedPatient = new Patient()
            .cnp(UPDATED_CNP)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .address(UPDATED_ADDRESS)
            .patientType(UPDATED_PATIENT_TYPE)
            .patientSubtype(UPDATED_PATIENT_SUBTYPE)
            .dateOfBirth(UPDATED_DATE_OF_BIRTH)
            .gender(UPDATED_GENDER)
            .hba1c(UPDATED_HBA_1_C)
            .bmi(UPDATED_BMI)
            .fev1Baseline(UPDATED_FEV_1_BASELINE);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedPatient.setUser(user);
        // Add required entity
        Doctor doctor;
        if (TestUtil.findAll(em, Doctor.class).isEmpty()) {
            doctor = DoctorResourceIT.createUpdatedEntity(em);
            em.persist(doctor);
            em.flush();
        } else {
            doctor = TestUtil.findAll(em, Doctor.class).get(0);
        }
        updatedPatient.setDoctor(doctor);
        return updatedPatient;
    }

    @BeforeEach
    void initTest() {
        patient = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedPatient != null) {
            patientRepository.delete(insertedPatient);
            insertedPatient = null;
        }
    }

    @Test
    @Transactional
    void createPatient() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Patient
        PatientDTO patientDTO = patientMapper.toDto(patient);
        var returnedPatientDTO = om.readValue(
            restPatientMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(patientDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PatientDTO.class
        );

        // Validate the Patient in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPatient = patientMapper.toEntity(returnedPatientDTO);
        assertPatientUpdatableFieldsEquals(returnedPatient, getPersistedPatient(returnedPatient));

        insertedPatient = returnedPatient;
    }

    @Test
    @Transactional
    void createPatientWithExistingId() throws Exception {
        // Create the Patient with an existing ID
        patient.setId(1L);
        PatientDTO patientDTO = patientMapper.toDto(patient);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPatientMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(patientDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Patient in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkCnpIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        patient.setCnp(null);

        // Create the Patient, which fails.
        PatientDTO patientDTO = patientMapper.toDto(patient);

        restPatientMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(patientDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPatientTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        patient.setPatientType(null);

        // Create the Patient, which fails.
        PatientDTO patientDTO = patientMapper.toDto(patient);

        restPatientMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(patientDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPatientSubtypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        patient.setPatientSubtype(null);

        // Create the Patient, which fails.
        PatientDTO patientDTO = patientMapper.toDto(patient);

        restPatientMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(patientDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDateOfBirthIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        patient.setDateOfBirth(null);

        // Create the Patient, which fails.
        PatientDTO patientDTO = patientMapper.toDto(patient);

        restPatientMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(patientDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllPatients() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList
        restPatientMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(patient.getId().intValue())))
            .andExpect(jsonPath("$.[*].cnp").value(hasItem(DEFAULT_CNP)))
            .andExpect(jsonPath("$.[*].phoneNumber").value(hasItem(DEFAULT_PHONE_NUMBER)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].patientType").value(hasItem(DEFAULT_PATIENT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].patientSubtype").value(hasItem(DEFAULT_PATIENT_SUBTYPE.toString())))
            .andExpect(jsonPath("$.[*].dateOfBirth").value(hasItem(DEFAULT_DATE_OF_BIRTH.toString())))
            .andExpect(jsonPath("$.[*].gender").value(hasItem(DEFAULT_GENDER)))
            .andExpect(jsonPath("$.[*].hba1c").value(hasItem(DEFAULT_HBA_1_C)))
            .andExpect(jsonPath("$.[*].bmi").value(hasItem(DEFAULT_BMI)))
            .andExpect(jsonPath("$.[*].fev1Baseline").value(hasItem(DEFAULT_FEV_1_BASELINE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPatientsWithEagerRelationshipsIsEnabled() throws Exception {
        when(patientServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPatientMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(patientServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPatientsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(patientServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPatientMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(patientRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getPatient() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get the patient
        restPatientMockMvc
            .perform(get(ENTITY_API_URL_ID, patient.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(patient.getId().intValue()))
            .andExpect(jsonPath("$.cnp").value(DEFAULT_CNP))
            .andExpect(jsonPath("$.phoneNumber").value(DEFAULT_PHONE_NUMBER))
            .andExpect(jsonPath("$.address").value(DEFAULT_ADDRESS))
            .andExpect(jsonPath("$.patientType").value(DEFAULT_PATIENT_TYPE.toString()))
            .andExpect(jsonPath("$.patientSubtype").value(DEFAULT_PATIENT_SUBTYPE.toString()))
            .andExpect(jsonPath("$.dateOfBirth").value(DEFAULT_DATE_OF_BIRTH.toString()))
            .andExpect(jsonPath("$.gender").value(DEFAULT_GENDER))
            .andExpect(jsonPath("$.hba1c").value(DEFAULT_HBA_1_C))
            .andExpect(jsonPath("$.bmi").value(DEFAULT_BMI))
            .andExpect(jsonPath("$.fev1Baseline").value(DEFAULT_FEV_1_BASELINE));
    }

    @Test
    @Transactional
    void getPatientsByIdFiltering() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        Long id = patient.getId();

        defaultPatientFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultPatientFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultPatientFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllPatientsByCnpIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where cnp equals to
        defaultPatientFiltering("cnp.equals=" + DEFAULT_CNP, "cnp.equals=" + UPDATED_CNP);
    }

    @Test
    @Transactional
    void getAllPatientsByCnpIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where cnp in
        defaultPatientFiltering("cnp.in=" + DEFAULT_CNP + "," + UPDATED_CNP, "cnp.in=" + UPDATED_CNP);
    }

    @Test
    @Transactional
    void getAllPatientsByCnpIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where cnp is not null
        defaultPatientFiltering("cnp.specified=true", "cnp.specified=false");
    }

    @Test
    @Transactional
    void getAllPatientsByCnpContainsSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where cnp contains
        defaultPatientFiltering("cnp.contains=" + DEFAULT_CNP, "cnp.contains=" + UPDATED_CNP);
    }

    @Test
    @Transactional
    void getAllPatientsByCnpNotContainsSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where cnp does not contain
        defaultPatientFiltering("cnp.doesNotContain=" + UPDATED_CNP, "cnp.doesNotContain=" + DEFAULT_CNP);
    }

    @Test
    @Transactional
    void getAllPatientsByPhoneNumberIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where phoneNumber equals to
        defaultPatientFiltering("phoneNumber.equals=" + DEFAULT_PHONE_NUMBER, "phoneNumber.equals=" + UPDATED_PHONE_NUMBER);
    }

    @Test
    @Transactional
    void getAllPatientsByPhoneNumberIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where phoneNumber in
        defaultPatientFiltering(
            "phoneNumber.in=" + DEFAULT_PHONE_NUMBER + "," + UPDATED_PHONE_NUMBER,
            "phoneNumber.in=" + UPDATED_PHONE_NUMBER
        );
    }

    @Test
    @Transactional
    void getAllPatientsByPhoneNumberIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where phoneNumber is not null
        defaultPatientFiltering("phoneNumber.specified=true", "phoneNumber.specified=false");
    }

    @Test
    @Transactional
    void getAllPatientsByPhoneNumberContainsSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where phoneNumber contains
        defaultPatientFiltering("phoneNumber.contains=" + DEFAULT_PHONE_NUMBER, "phoneNumber.contains=" + UPDATED_PHONE_NUMBER);
    }

    @Test
    @Transactional
    void getAllPatientsByPhoneNumberNotContainsSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where phoneNumber does not contain
        defaultPatientFiltering("phoneNumber.doesNotContain=" + UPDATED_PHONE_NUMBER, "phoneNumber.doesNotContain=" + DEFAULT_PHONE_NUMBER);
    }

    @Test
    @Transactional
    void getAllPatientsByAddressIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where address equals to
        defaultPatientFiltering("address.equals=" + DEFAULT_ADDRESS, "address.equals=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    void getAllPatientsByAddressIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where address in
        defaultPatientFiltering("address.in=" + DEFAULT_ADDRESS + "," + UPDATED_ADDRESS, "address.in=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    void getAllPatientsByAddressIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where address is not null
        defaultPatientFiltering("address.specified=true", "address.specified=false");
    }

    @Test
    @Transactional
    void getAllPatientsByAddressContainsSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where address contains
        defaultPatientFiltering("address.contains=" + DEFAULT_ADDRESS, "address.contains=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    void getAllPatientsByAddressNotContainsSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where address does not contain
        defaultPatientFiltering("address.doesNotContain=" + UPDATED_ADDRESS, "address.doesNotContain=" + DEFAULT_ADDRESS);
    }

    @Test
    @Transactional
    void getAllPatientsByPatientTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where patientType equals to
        defaultPatientFiltering("patientType.equals=" + DEFAULT_PATIENT_TYPE, "patientType.equals=" + UPDATED_PATIENT_TYPE);
    }

    @Test
    @Transactional
    void getAllPatientsByPatientTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where patientType in
        defaultPatientFiltering(
            "patientType.in=" + DEFAULT_PATIENT_TYPE + "," + UPDATED_PATIENT_TYPE,
            "patientType.in=" + UPDATED_PATIENT_TYPE
        );
    }

    @Test
    @Transactional
    void getAllPatientsByPatientTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where patientType is not null
        defaultPatientFiltering("patientType.specified=true", "patientType.specified=false");
    }

    @Test
    @Transactional
    void getAllPatientsByPatientSubtypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where patientSubtype equals to
        defaultPatientFiltering("patientSubtype.equals=" + DEFAULT_PATIENT_SUBTYPE, "patientSubtype.equals=" + UPDATED_PATIENT_SUBTYPE);
    }

    @Test
    @Transactional
    void getAllPatientsByPatientSubtypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where patientSubtype in
        defaultPatientFiltering(
            "patientSubtype.in=" + DEFAULT_PATIENT_SUBTYPE + "," + UPDATED_PATIENT_SUBTYPE,
            "patientSubtype.in=" + UPDATED_PATIENT_SUBTYPE
        );
    }

    @Test
    @Transactional
    void getAllPatientsByPatientSubtypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where patientSubtype is not null
        defaultPatientFiltering("patientSubtype.specified=true", "patientSubtype.specified=false");
    }

    @Test
    @Transactional
    void getAllPatientsByDateOfBirthIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where dateOfBirth equals to
        defaultPatientFiltering("dateOfBirth.equals=" + DEFAULT_DATE_OF_BIRTH, "dateOfBirth.equals=" + UPDATED_DATE_OF_BIRTH);
    }

    @Test
    @Transactional
    void getAllPatientsByDateOfBirthIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where dateOfBirth in
        defaultPatientFiltering(
            "dateOfBirth.in=" + DEFAULT_DATE_OF_BIRTH + "," + UPDATED_DATE_OF_BIRTH,
            "dateOfBirth.in=" + UPDATED_DATE_OF_BIRTH
        );
    }

    @Test
    @Transactional
    void getAllPatientsByDateOfBirthIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where dateOfBirth is not null
        defaultPatientFiltering("dateOfBirth.specified=true", "dateOfBirth.specified=false");
    }

    @Test
    @Transactional
    void getAllPatientsByDateOfBirthIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where dateOfBirth is greater than or equal to
        defaultPatientFiltering(
            "dateOfBirth.greaterThanOrEqual=" + DEFAULT_DATE_OF_BIRTH,
            "dateOfBirth.greaterThanOrEqual=" + UPDATED_DATE_OF_BIRTH
        );
    }

    @Test
    @Transactional
    void getAllPatientsByDateOfBirthIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where dateOfBirth is less than or equal to
        defaultPatientFiltering(
            "dateOfBirth.lessThanOrEqual=" + DEFAULT_DATE_OF_BIRTH,
            "dateOfBirth.lessThanOrEqual=" + SMALLER_DATE_OF_BIRTH
        );
    }

    @Test
    @Transactional
    void getAllPatientsByDateOfBirthIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where dateOfBirth is less than
        defaultPatientFiltering("dateOfBirth.lessThan=" + UPDATED_DATE_OF_BIRTH, "dateOfBirth.lessThan=" + DEFAULT_DATE_OF_BIRTH);
    }

    @Test
    @Transactional
    void getAllPatientsByDateOfBirthIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where dateOfBirth is greater than
        defaultPatientFiltering("dateOfBirth.greaterThan=" + SMALLER_DATE_OF_BIRTH, "dateOfBirth.greaterThan=" + DEFAULT_DATE_OF_BIRTH);
    }

    @Test
    @Transactional
    void getAllPatientsByGenderIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where gender equals to
        defaultPatientFiltering("gender.equals=" + DEFAULT_GENDER, "gender.equals=" + UPDATED_GENDER);
    }

    @Test
    @Transactional
    void getAllPatientsByGenderIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where gender in
        defaultPatientFiltering("gender.in=" + DEFAULT_GENDER + "," + UPDATED_GENDER, "gender.in=" + UPDATED_GENDER);
    }

    @Test
    @Transactional
    void getAllPatientsByGenderIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where gender is not null
        defaultPatientFiltering("gender.specified=true", "gender.specified=false");
    }

    @Test
    @Transactional
    void getAllPatientsByGenderContainsSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where gender contains
        defaultPatientFiltering("gender.contains=" + DEFAULT_GENDER, "gender.contains=" + UPDATED_GENDER);
    }

    @Test
    @Transactional
    void getAllPatientsByGenderNotContainsSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where gender does not contain
        defaultPatientFiltering("gender.doesNotContain=" + UPDATED_GENDER, "gender.doesNotContain=" + DEFAULT_GENDER);
    }

    @Test
    @Transactional
    void getAllPatientsByHba1cIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where hba1c equals to
        defaultPatientFiltering("hba1c.equals=" + DEFAULT_HBA_1_C, "hba1c.equals=" + UPDATED_HBA_1_C);
    }

    @Test
    @Transactional
    void getAllPatientsByHba1cIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where hba1c in
        defaultPatientFiltering("hba1c.in=" + DEFAULT_HBA_1_C + "," + UPDATED_HBA_1_C, "hba1c.in=" + UPDATED_HBA_1_C);
    }

    @Test
    @Transactional
    void getAllPatientsByHba1cIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where hba1c is not null
        defaultPatientFiltering("hba1c.specified=true", "hba1c.specified=false");
    }

    @Test
    @Transactional
    void getAllPatientsByHba1cIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where hba1c is greater than or equal to
        defaultPatientFiltering("hba1c.greaterThanOrEqual=" + DEFAULT_HBA_1_C, "hba1c.greaterThanOrEqual=" + UPDATED_HBA_1_C);
    }

    @Test
    @Transactional
    void getAllPatientsByHba1cIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where hba1c is less than or equal to
        defaultPatientFiltering("hba1c.lessThanOrEqual=" + DEFAULT_HBA_1_C, "hba1c.lessThanOrEqual=" + SMALLER_HBA_1_C);
    }

    @Test
    @Transactional
    void getAllPatientsByHba1cIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where hba1c is less than
        defaultPatientFiltering("hba1c.lessThan=" + UPDATED_HBA_1_C, "hba1c.lessThan=" + DEFAULT_HBA_1_C);
    }

    @Test
    @Transactional
    void getAllPatientsByHba1cIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where hba1c is greater than
        defaultPatientFiltering("hba1c.greaterThan=" + SMALLER_HBA_1_C, "hba1c.greaterThan=" + DEFAULT_HBA_1_C);
    }

    @Test
    @Transactional
    void getAllPatientsByBmiIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where bmi equals to
        defaultPatientFiltering("bmi.equals=" + DEFAULT_BMI, "bmi.equals=" + UPDATED_BMI);
    }

    @Test
    @Transactional
    void getAllPatientsByBmiIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where bmi in
        defaultPatientFiltering("bmi.in=" + DEFAULT_BMI + "," + UPDATED_BMI, "bmi.in=" + UPDATED_BMI);
    }

    @Test
    @Transactional
    void getAllPatientsByBmiIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where bmi is not null
        defaultPatientFiltering("bmi.specified=true", "bmi.specified=false");
    }

    @Test
    @Transactional
    void getAllPatientsByBmiIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where bmi is greater than or equal to
        defaultPatientFiltering("bmi.greaterThanOrEqual=" + DEFAULT_BMI, "bmi.greaterThanOrEqual=" + UPDATED_BMI);
    }

    @Test
    @Transactional
    void getAllPatientsByBmiIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where bmi is less than or equal to
        defaultPatientFiltering("bmi.lessThanOrEqual=" + DEFAULT_BMI, "bmi.lessThanOrEqual=" + SMALLER_BMI);
    }

    @Test
    @Transactional
    void getAllPatientsByBmiIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where bmi is less than
        defaultPatientFiltering("bmi.lessThan=" + UPDATED_BMI, "bmi.lessThan=" + DEFAULT_BMI);
    }

    @Test
    @Transactional
    void getAllPatientsByBmiIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where bmi is greater than
        defaultPatientFiltering("bmi.greaterThan=" + SMALLER_BMI, "bmi.greaterThan=" + DEFAULT_BMI);
    }

    @Test
    @Transactional
    void getAllPatientsByFev1BaselineIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where fev1Baseline equals to
        defaultPatientFiltering("fev1Baseline.equals=" + DEFAULT_FEV_1_BASELINE, "fev1Baseline.equals=" + UPDATED_FEV_1_BASELINE);
    }

    @Test
    @Transactional
    void getAllPatientsByFev1BaselineIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where fev1Baseline in
        defaultPatientFiltering(
            "fev1Baseline.in=" + DEFAULT_FEV_1_BASELINE + "," + UPDATED_FEV_1_BASELINE,
            "fev1Baseline.in=" + UPDATED_FEV_1_BASELINE
        );
    }

    @Test
    @Transactional
    void getAllPatientsByFev1BaselineIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where fev1Baseline is not null
        defaultPatientFiltering("fev1Baseline.specified=true", "fev1Baseline.specified=false");
    }

    @Test
    @Transactional
    void getAllPatientsByFev1BaselineIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where fev1Baseline is greater than or equal to
        defaultPatientFiltering(
            "fev1Baseline.greaterThanOrEqual=" + DEFAULT_FEV_1_BASELINE,
            "fev1Baseline.greaterThanOrEqual=" + UPDATED_FEV_1_BASELINE
        );
    }

    @Test
    @Transactional
    void getAllPatientsByFev1BaselineIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where fev1Baseline is less than or equal to
        defaultPatientFiltering(
            "fev1Baseline.lessThanOrEqual=" + DEFAULT_FEV_1_BASELINE,
            "fev1Baseline.lessThanOrEqual=" + SMALLER_FEV_1_BASELINE
        );
    }

    @Test
    @Transactional
    void getAllPatientsByFev1BaselineIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where fev1Baseline is less than
        defaultPatientFiltering("fev1Baseline.lessThan=" + UPDATED_FEV_1_BASELINE, "fev1Baseline.lessThan=" + DEFAULT_FEV_1_BASELINE);
    }

    @Test
    @Transactional
    void getAllPatientsByFev1BaselineIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList where fev1Baseline is greater than
        defaultPatientFiltering("fev1Baseline.greaterThan=" + SMALLER_FEV_1_BASELINE, "fev1Baseline.greaterThan=" + DEFAULT_FEV_1_BASELINE);
    }

    @Test
    @Transactional
    void getAllPatientsByUserIsEqualToSomething() throws Exception {
        // Get already existing entity
        User user = patient.getUser();
        patientRepository.saveAndFlush(patient);
        Long userId = user.getId();
        // Get all the patientList where user equals to userId
        defaultPatientShouldBeFound("userId.equals=" + userId);

        // Get all the patientList where user equals to (userId + 1)
        defaultPatientShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    @Test
    @Transactional
    void getAllPatientsByDoctorIsEqualToSomething() throws Exception {
        Doctor doctor;
        if (TestUtil.findAll(em, Doctor.class).isEmpty()) {
            patientRepository.saveAndFlush(patient);
            doctor = DoctorResourceIT.createEntity(em);
        } else {
            doctor = TestUtil.findAll(em, Doctor.class).get(0);
        }
        em.persist(doctor);
        em.flush();
        patient.setDoctor(doctor);
        patientRepository.saveAndFlush(patient);
        Long doctorId = doctor.getId();
        // Get all the patientList where doctor equals to doctorId
        defaultPatientShouldBeFound("doctorId.equals=" + doctorId);

        // Get all the patientList where doctor equals to (doctorId + 1)
        defaultPatientShouldNotBeFound("doctorId.equals=" + (doctorId + 1));
    }

    private void defaultPatientFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultPatientShouldBeFound(shouldBeFound);
        defaultPatientShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultPatientShouldBeFound(String filter) throws Exception {
        restPatientMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(patient.getId().intValue())))
            .andExpect(jsonPath("$.[*].cnp").value(hasItem(DEFAULT_CNP)))
            .andExpect(jsonPath("$.[*].phoneNumber").value(hasItem(DEFAULT_PHONE_NUMBER)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].patientType").value(hasItem(DEFAULT_PATIENT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].patientSubtype").value(hasItem(DEFAULT_PATIENT_SUBTYPE.toString())))
            .andExpect(jsonPath("$.[*].dateOfBirth").value(hasItem(DEFAULT_DATE_OF_BIRTH.toString())))
            .andExpect(jsonPath("$.[*].gender").value(hasItem(DEFAULT_GENDER)))
            .andExpect(jsonPath("$.[*].hba1c").value(hasItem(DEFAULT_HBA_1_C)))
            .andExpect(jsonPath("$.[*].bmi").value(hasItem(DEFAULT_BMI)))
            .andExpect(jsonPath("$.[*].fev1Baseline").value(hasItem(DEFAULT_FEV_1_BASELINE)));

        // Check, that the count call also returns 1
        restPatientMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultPatientShouldNotBeFound(String filter) throws Exception {
        restPatientMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restPatientMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingPatient() throws Exception {
        // Get the patient
        restPatientMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPatient() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the patient
        Patient updatedPatient = patientRepository.findById(patient.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPatient are not directly saved in db
        em.detach(updatedPatient);
        updatedPatient
            .cnp(UPDATED_CNP)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .address(UPDATED_ADDRESS)
            .patientType(UPDATED_PATIENT_TYPE)
            .patientSubtype(UPDATED_PATIENT_SUBTYPE)
            .dateOfBirth(UPDATED_DATE_OF_BIRTH)
            .gender(UPDATED_GENDER)
            .hba1c(UPDATED_HBA_1_C)
            .bmi(UPDATED_BMI)
            .fev1Baseline(UPDATED_FEV_1_BASELINE);
        PatientDTO patientDTO = patientMapper.toDto(updatedPatient);

        restPatientMockMvc
            .perform(
                put(ENTITY_API_URL_ID, patientDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(patientDTO))
            )
            .andExpect(status().isOk());

        // Validate the Patient in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPatientToMatchAllProperties(updatedPatient);
    }

    @Test
    @Transactional
    void putNonExistingPatient() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        patient.setId(longCount.incrementAndGet());

        // Create the Patient
        PatientDTO patientDTO = patientMapper.toDto(patient);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPatientMockMvc
            .perform(
                put(ENTITY_API_URL_ID, patientDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(patientDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Patient in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPatient() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        patient.setId(longCount.incrementAndGet());

        // Create the Patient
        PatientDTO patientDTO = patientMapper.toDto(patient);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPatientMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(patientDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Patient in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPatient() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        patient.setId(longCount.incrementAndGet());

        // Create the Patient
        PatientDTO patientDTO = patientMapper.toDto(patient);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPatientMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(patientDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Patient in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePatientWithPatch() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the patient using partial update
        Patient partialUpdatedPatient = new Patient();
        partialUpdatedPatient.setId(patient.getId());

        partialUpdatedPatient
            .cnp(UPDATED_CNP)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .patientSubtype(UPDATED_PATIENT_SUBTYPE)
            .dateOfBirth(UPDATED_DATE_OF_BIRTH)
            .hba1c(UPDATED_HBA_1_C)
            .bmi(UPDATED_BMI)
            .fev1Baseline(UPDATED_FEV_1_BASELINE);

        restPatientMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPatient.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPatient))
            )
            .andExpect(status().isOk());

        // Validate the Patient in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPatientUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedPatient, patient), getPersistedPatient(patient));
    }

    @Test
    @Transactional
    void fullUpdatePatientWithPatch() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the patient using partial update
        Patient partialUpdatedPatient = new Patient();
        partialUpdatedPatient.setId(patient.getId());

        partialUpdatedPatient
            .cnp(UPDATED_CNP)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .address(UPDATED_ADDRESS)
            .patientType(UPDATED_PATIENT_TYPE)
            .patientSubtype(UPDATED_PATIENT_SUBTYPE)
            .dateOfBirth(UPDATED_DATE_OF_BIRTH)
            .gender(UPDATED_GENDER)
            .hba1c(UPDATED_HBA_1_C)
            .bmi(UPDATED_BMI)
            .fev1Baseline(UPDATED_FEV_1_BASELINE);

        restPatientMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPatient.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPatient))
            )
            .andExpect(status().isOk());

        // Validate the Patient in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPatientUpdatableFieldsEquals(partialUpdatedPatient, getPersistedPatient(partialUpdatedPatient));
    }

    @Test
    @Transactional
    void patchNonExistingPatient() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        patient.setId(longCount.incrementAndGet());

        // Create the Patient
        PatientDTO patientDTO = patientMapper.toDto(patient);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPatientMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, patientDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(patientDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Patient in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPatient() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        patient.setId(longCount.incrementAndGet());

        // Create the Patient
        PatientDTO patientDTO = patientMapper.toDto(patient);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPatientMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(patientDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Patient in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPatient() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        patient.setId(longCount.incrementAndGet());

        // Create the Patient
        PatientDTO patientDTO = patientMapper.toDto(patient);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPatientMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(patientDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Patient in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePatient() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the patient
        restPatientMockMvc
            .perform(delete(ENTITY_API_URL_ID, patient.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return patientRepository.count();
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

    protected Patient getPersistedPatient(Patient patient) {
        return patientRepository.findById(patient.getId()).orElseThrow();
    }

    protected void assertPersistedPatientToMatchAllProperties(Patient expectedPatient) {
        assertPatientAllPropertiesEquals(expectedPatient, getPersistedPatient(expectedPatient));
    }

    protected void assertPersistedPatientToMatchUpdatableProperties(Patient expectedPatient) {
        assertPatientAllUpdatablePropertiesEquals(expectedPatient, getPersistedPatient(expectedPatient));
    }
}
