package itee.licenta.monitorizare.web.rest;

import static itee.licenta.monitorizare.domain.AppointmentSlotAsserts.*;
import static itee.licenta.monitorizare.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import itee.licenta.monitorizare.IntegrationTest;
import itee.licenta.monitorizare.domain.AppointmentSlot;
import itee.licenta.monitorizare.domain.Doctor;
import itee.licenta.monitorizare.repository.AppointmentSlotRepository;
import itee.licenta.monitorizare.service.AppointmentSlotService;
import itee.licenta.monitorizare.service.dto.AppointmentSlotDTO;
import itee.licenta.monitorizare.service.mapper.AppointmentSlotMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
 * Integration tests for the {@link AppointmentSlotResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class AppointmentSlotResourceIT {

    private static final Instant DEFAULT_START_TIME = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_START_TIME = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_END_TIME = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_END_TIME = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Boolean DEFAULT_IS_AVAILABLE = false;
    private static final Boolean UPDATED_IS_AVAILABLE = true;

    private static final String ENTITY_API_URL = "/api/appointment-slots";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AppointmentSlotRepository appointmentSlotRepository;

    @Mock
    private AppointmentSlotRepository appointmentSlotRepositoryMock;

    @Autowired
    private AppointmentSlotMapper appointmentSlotMapper;

    @Mock
    private AppointmentSlotService appointmentSlotServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAppointmentSlotMockMvc;

    private AppointmentSlot appointmentSlot;

    private AppointmentSlot insertedAppointmentSlot;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AppointmentSlot createEntity(EntityManager em) {
        AppointmentSlot appointmentSlot = new AppointmentSlot()
            .startTime(DEFAULT_START_TIME)
            .endTime(DEFAULT_END_TIME)
            .isAvailable(DEFAULT_IS_AVAILABLE);
        // Add required entity
        Doctor doctor;
        if (TestUtil.findAll(em, Doctor.class).isEmpty()) {
            doctor = DoctorResourceIT.createEntity(em);
            em.persist(doctor);
            em.flush();
        } else {
            doctor = TestUtil.findAll(em, Doctor.class).get(0);
        }
        appointmentSlot.setDoctor(doctor);
        return appointmentSlot;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AppointmentSlot createUpdatedEntity(EntityManager em) {
        AppointmentSlot updatedAppointmentSlot = new AppointmentSlot()
            .startTime(UPDATED_START_TIME)
            .endTime(UPDATED_END_TIME)
            .isAvailable(UPDATED_IS_AVAILABLE);
        // Add required entity
        Doctor doctor;
        if (TestUtil.findAll(em, Doctor.class).isEmpty()) {
            doctor = DoctorResourceIT.createUpdatedEntity(em);
            em.persist(doctor);
            em.flush();
        } else {
            doctor = TestUtil.findAll(em, Doctor.class).get(0);
        }
        updatedAppointmentSlot.setDoctor(doctor);
        return updatedAppointmentSlot;
    }

    @BeforeEach
    void initTest() {
        appointmentSlot = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedAppointmentSlot != null) {
            appointmentSlotRepository.delete(insertedAppointmentSlot);
            insertedAppointmentSlot = null;
        }
    }

    @Test
    @Transactional
    void createAppointmentSlot() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the AppointmentSlot
        AppointmentSlotDTO appointmentSlotDTO = appointmentSlotMapper.toDto(appointmentSlot);
        var returnedAppointmentSlotDTO = om.readValue(
            restAppointmentSlotMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(appointmentSlotDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AppointmentSlotDTO.class
        );

        // Validate the AppointmentSlot in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAppointmentSlot = appointmentSlotMapper.toEntity(returnedAppointmentSlotDTO);
        assertAppointmentSlotUpdatableFieldsEquals(returnedAppointmentSlot, getPersistedAppointmentSlot(returnedAppointmentSlot));

        insertedAppointmentSlot = returnedAppointmentSlot;
    }

    @Test
    @Transactional
    void createAppointmentSlotWithExistingId() throws Exception {
        // Create the AppointmentSlot with an existing ID
        appointmentSlot.setId(1L);
        AppointmentSlotDTO appointmentSlotDTO = appointmentSlotMapper.toDto(appointmentSlot);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAppointmentSlotMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(appointmentSlotDTO)))
            .andExpect(status().isBadRequest());

        // Validate the AppointmentSlot in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkStartTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        appointmentSlot.setStartTime(null);

        // Create the AppointmentSlot, which fails.
        AppointmentSlotDTO appointmentSlotDTO = appointmentSlotMapper.toDto(appointmentSlot);

        restAppointmentSlotMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(appointmentSlotDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEndTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        appointmentSlot.setEndTime(null);

        // Create the AppointmentSlot, which fails.
        AppointmentSlotDTO appointmentSlotDTO = appointmentSlotMapper.toDto(appointmentSlot);

        restAppointmentSlotMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(appointmentSlotDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkIsAvailableIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        appointmentSlot.setIsAvailable(null);

        // Create the AppointmentSlot, which fails.
        AppointmentSlotDTO appointmentSlotDTO = appointmentSlotMapper.toDto(appointmentSlot);

        restAppointmentSlotMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(appointmentSlotDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAppointmentSlots() throws Exception {
        // Initialize the database
        insertedAppointmentSlot = appointmentSlotRepository.saveAndFlush(appointmentSlot);

        // Get all the appointmentSlotList
        restAppointmentSlotMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appointmentSlot.getId().intValue())))
            .andExpect(jsonPath("$.[*].startTime").value(hasItem(DEFAULT_START_TIME.toString())))
            .andExpect(jsonPath("$.[*].endTime").value(hasItem(DEFAULT_END_TIME.toString())))
            .andExpect(jsonPath("$.[*].isAvailable").value(hasItem(DEFAULT_IS_AVAILABLE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAppointmentSlotsWithEagerRelationshipsIsEnabled() throws Exception {
        when(appointmentSlotServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAppointmentSlotMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(appointmentSlotServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAppointmentSlotsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(appointmentSlotServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAppointmentSlotMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(appointmentSlotRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getAppointmentSlot() throws Exception {
        // Initialize the database
        insertedAppointmentSlot = appointmentSlotRepository.saveAndFlush(appointmentSlot);

        // Get the appointmentSlot
        restAppointmentSlotMockMvc
            .perform(get(ENTITY_API_URL_ID, appointmentSlot.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(appointmentSlot.getId().intValue()))
            .andExpect(jsonPath("$.startTime").value(DEFAULT_START_TIME.toString()))
            .andExpect(jsonPath("$.endTime").value(DEFAULT_END_TIME.toString()))
            .andExpect(jsonPath("$.isAvailable").value(DEFAULT_IS_AVAILABLE));
    }

    @Test
    @Transactional
    void getAppointmentSlotsByIdFiltering() throws Exception {
        // Initialize the database
        insertedAppointmentSlot = appointmentSlotRepository.saveAndFlush(appointmentSlot);

        Long id = appointmentSlot.getId();

        defaultAppointmentSlotFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultAppointmentSlotFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultAppointmentSlotFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllAppointmentSlotsByStartTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAppointmentSlot = appointmentSlotRepository.saveAndFlush(appointmentSlot);

        // Get all the appointmentSlotList where startTime equals to
        defaultAppointmentSlotFiltering("startTime.equals=" + DEFAULT_START_TIME, "startTime.equals=" + UPDATED_START_TIME);
    }

    @Test
    @Transactional
    void getAllAppointmentSlotsByStartTimeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAppointmentSlot = appointmentSlotRepository.saveAndFlush(appointmentSlot);

        // Get all the appointmentSlotList where startTime in
        defaultAppointmentSlotFiltering(
            "startTime.in=" + DEFAULT_START_TIME + "," + UPDATED_START_TIME,
            "startTime.in=" + UPDATED_START_TIME
        );
    }

    @Test
    @Transactional
    void getAllAppointmentSlotsByStartTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAppointmentSlot = appointmentSlotRepository.saveAndFlush(appointmentSlot);

        // Get all the appointmentSlotList where startTime is not null
        defaultAppointmentSlotFiltering("startTime.specified=true", "startTime.specified=false");
    }

    @Test
    @Transactional
    void getAllAppointmentSlotsByEndTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAppointmentSlot = appointmentSlotRepository.saveAndFlush(appointmentSlot);

        // Get all the appointmentSlotList where endTime equals to
        defaultAppointmentSlotFiltering("endTime.equals=" + DEFAULT_END_TIME, "endTime.equals=" + UPDATED_END_TIME);
    }

    @Test
    @Transactional
    void getAllAppointmentSlotsByEndTimeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAppointmentSlot = appointmentSlotRepository.saveAndFlush(appointmentSlot);

        // Get all the appointmentSlotList where endTime in
        defaultAppointmentSlotFiltering("endTime.in=" + DEFAULT_END_TIME + "," + UPDATED_END_TIME, "endTime.in=" + UPDATED_END_TIME);
    }

    @Test
    @Transactional
    void getAllAppointmentSlotsByEndTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAppointmentSlot = appointmentSlotRepository.saveAndFlush(appointmentSlot);

        // Get all the appointmentSlotList where endTime is not null
        defaultAppointmentSlotFiltering("endTime.specified=true", "endTime.specified=false");
    }

    @Test
    @Transactional
    void getAllAppointmentSlotsByIsAvailableIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAppointmentSlot = appointmentSlotRepository.saveAndFlush(appointmentSlot);

        // Get all the appointmentSlotList where isAvailable equals to
        defaultAppointmentSlotFiltering("isAvailable.equals=" + DEFAULT_IS_AVAILABLE, "isAvailable.equals=" + UPDATED_IS_AVAILABLE);
    }

    @Test
    @Transactional
    void getAllAppointmentSlotsByIsAvailableIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAppointmentSlot = appointmentSlotRepository.saveAndFlush(appointmentSlot);

        // Get all the appointmentSlotList where isAvailable in
        defaultAppointmentSlotFiltering(
            "isAvailable.in=" + DEFAULT_IS_AVAILABLE + "," + UPDATED_IS_AVAILABLE,
            "isAvailable.in=" + UPDATED_IS_AVAILABLE
        );
    }

    @Test
    @Transactional
    void getAllAppointmentSlotsByIsAvailableIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAppointmentSlot = appointmentSlotRepository.saveAndFlush(appointmentSlot);

        // Get all the appointmentSlotList where isAvailable is not null
        defaultAppointmentSlotFiltering("isAvailable.specified=true", "isAvailable.specified=false");
    }

    @Test
    @Transactional
    void getAllAppointmentSlotsByDoctorIsEqualToSomething() throws Exception {
        Doctor doctor;
        if (TestUtil.findAll(em, Doctor.class).isEmpty()) {
            appointmentSlotRepository.saveAndFlush(appointmentSlot);
            doctor = DoctorResourceIT.createEntity(em);
        } else {
            doctor = TestUtil.findAll(em, Doctor.class).get(0);
        }
        em.persist(doctor);
        em.flush();
        appointmentSlot.setDoctor(doctor);
        appointmentSlotRepository.saveAndFlush(appointmentSlot);
        Long doctorId = doctor.getId();
        // Get all the appointmentSlotList where doctor equals to doctorId
        defaultAppointmentSlotShouldBeFound("doctorId.equals=" + doctorId);

        // Get all the appointmentSlotList where doctor equals to (doctorId + 1)
        defaultAppointmentSlotShouldNotBeFound("doctorId.equals=" + (doctorId + 1));
    }

    private void defaultAppointmentSlotFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultAppointmentSlotShouldBeFound(shouldBeFound);
        defaultAppointmentSlotShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAppointmentSlotShouldBeFound(String filter) throws Exception {
        restAppointmentSlotMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appointmentSlot.getId().intValue())))
            .andExpect(jsonPath("$.[*].startTime").value(hasItem(DEFAULT_START_TIME.toString())))
            .andExpect(jsonPath("$.[*].endTime").value(hasItem(DEFAULT_END_TIME.toString())))
            .andExpect(jsonPath("$.[*].isAvailable").value(hasItem(DEFAULT_IS_AVAILABLE)));

        // Check, that the count call also returns 1
        restAppointmentSlotMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultAppointmentSlotShouldNotBeFound(String filter) throws Exception {
        restAppointmentSlotMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restAppointmentSlotMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingAppointmentSlot() throws Exception {
        // Get the appointmentSlot
        restAppointmentSlotMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAppointmentSlot() throws Exception {
        // Initialize the database
        insertedAppointmentSlot = appointmentSlotRepository.saveAndFlush(appointmentSlot);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the appointmentSlot
        AppointmentSlot updatedAppointmentSlot = appointmentSlotRepository.findById(appointmentSlot.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAppointmentSlot are not directly saved in db
        em.detach(updatedAppointmentSlot);
        updatedAppointmentSlot.startTime(UPDATED_START_TIME).endTime(UPDATED_END_TIME).isAvailable(UPDATED_IS_AVAILABLE);
        AppointmentSlotDTO appointmentSlotDTO = appointmentSlotMapper.toDto(updatedAppointmentSlot);

        restAppointmentSlotMockMvc
            .perform(
                put(ENTITY_API_URL_ID, appointmentSlotDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(appointmentSlotDTO))
            )
            .andExpect(status().isOk());

        // Validate the AppointmentSlot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAppointmentSlotToMatchAllProperties(updatedAppointmentSlot);
    }

    @Test
    @Transactional
    void putNonExistingAppointmentSlot() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        appointmentSlot.setId(longCount.incrementAndGet());

        // Create the AppointmentSlot
        AppointmentSlotDTO appointmentSlotDTO = appointmentSlotMapper.toDto(appointmentSlot);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAppointmentSlotMockMvc
            .perform(
                put(ENTITY_API_URL_ID, appointmentSlotDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(appointmentSlotDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AppointmentSlot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAppointmentSlot() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        appointmentSlot.setId(longCount.incrementAndGet());

        // Create the AppointmentSlot
        AppointmentSlotDTO appointmentSlotDTO = appointmentSlotMapper.toDto(appointmentSlot);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAppointmentSlotMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(appointmentSlotDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AppointmentSlot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAppointmentSlot() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        appointmentSlot.setId(longCount.incrementAndGet());

        // Create the AppointmentSlot
        AppointmentSlotDTO appointmentSlotDTO = appointmentSlotMapper.toDto(appointmentSlot);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAppointmentSlotMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(appointmentSlotDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AppointmentSlot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAppointmentSlotWithPatch() throws Exception {
        // Initialize the database
        insertedAppointmentSlot = appointmentSlotRepository.saveAndFlush(appointmentSlot);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the appointmentSlot using partial update
        AppointmentSlot partialUpdatedAppointmentSlot = new AppointmentSlot();
        partialUpdatedAppointmentSlot.setId(appointmentSlot.getId());

        restAppointmentSlotMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAppointmentSlot.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAppointmentSlot))
            )
            .andExpect(status().isOk());

        // Validate the AppointmentSlot in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAppointmentSlotUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedAppointmentSlot, appointmentSlot),
            getPersistedAppointmentSlot(appointmentSlot)
        );
    }

    @Test
    @Transactional
    void fullUpdateAppointmentSlotWithPatch() throws Exception {
        // Initialize the database
        insertedAppointmentSlot = appointmentSlotRepository.saveAndFlush(appointmentSlot);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the appointmentSlot using partial update
        AppointmentSlot partialUpdatedAppointmentSlot = new AppointmentSlot();
        partialUpdatedAppointmentSlot.setId(appointmentSlot.getId());

        partialUpdatedAppointmentSlot.startTime(UPDATED_START_TIME).endTime(UPDATED_END_TIME).isAvailable(UPDATED_IS_AVAILABLE);

        restAppointmentSlotMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAppointmentSlot.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAppointmentSlot))
            )
            .andExpect(status().isOk());

        // Validate the AppointmentSlot in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAppointmentSlotUpdatableFieldsEquals(
            partialUpdatedAppointmentSlot,
            getPersistedAppointmentSlot(partialUpdatedAppointmentSlot)
        );
    }

    @Test
    @Transactional
    void patchNonExistingAppointmentSlot() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        appointmentSlot.setId(longCount.incrementAndGet());

        // Create the AppointmentSlot
        AppointmentSlotDTO appointmentSlotDTO = appointmentSlotMapper.toDto(appointmentSlot);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAppointmentSlotMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, appointmentSlotDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(appointmentSlotDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AppointmentSlot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAppointmentSlot() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        appointmentSlot.setId(longCount.incrementAndGet());

        // Create the AppointmentSlot
        AppointmentSlotDTO appointmentSlotDTO = appointmentSlotMapper.toDto(appointmentSlot);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAppointmentSlotMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(appointmentSlotDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AppointmentSlot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAppointmentSlot() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        appointmentSlot.setId(longCount.incrementAndGet());

        // Create the AppointmentSlot
        AppointmentSlotDTO appointmentSlotDTO = appointmentSlotMapper.toDto(appointmentSlot);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAppointmentSlotMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(appointmentSlotDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AppointmentSlot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAppointmentSlot() throws Exception {
        // Initialize the database
        insertedAppointmentSlot = appointmentSlotRepository.saveAndFlush(appointmentSlot);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the appointmentSlot
        restAppointmentSlotMockMvc
            .perform(delete(ENTITY_API_URL_ID, appointmentSlot.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return appointmentSlotRepository.count();
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

    protected AppointmentSlot getPersistedAppointmentSlot(AppointmentSlot appointmentSlot) {
        return appointmentSlotRepository.findById(appointmentSlot.getId()).orElseThrow();
    }

    protected void assertPersistedAppointmentSlotToMatchAllProperties(AppointmentSlot expectedAppointmentSlot) {
        assertAppointmentSlotAllPropertiesEquals(expectedAppointmentSlot, getPersistedAppointmentSlot(expectedAppointmentSlot));
    }

    protected void assertPersistedAppointmentSlotToMatchUpdatableProperties(AppointmentSlot expectedAppointmentSlot) {
        assertAppointmentSlotAllUpdatablePropertiesEquals(expectedAppointmentSlot, getPersistedAppointmentSlot(expectedAppointmentSlot));
    }
}
