package itee.licenta.monitorizare.web.rest;

import static itee.licenta.monitorizare.domain.NotificationAsserts.*;
import static itee.licenta.monitorizare.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import itee.licenta.monitorizare.IntegrationTest;
import itee.licenta.monitorizare.domain.Notification;
import itee.licenta.monitorizare.domain.Patient;
import itee.licenta.monitorizare.domain.User;
import itee.licenta.monitorizare.domain.enumeration.NotificationType;
import itee.licenta.monitorizare.repository.NotificationRepository;
import itee.licenta.monitorizare.repository.UserRepository;
import itee.licenta.monitorizare.service.NotificationService;
import itee.licenta.monitorizare.service.dto.NotificationDTO;
import itee.licenta.monitorizare.service.mapper.NotificationMapper;
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
 * Integration tests for the {@link NotificationResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class NotificationResourceIT {

    private static final NotificationType DEFAULT_TYPE = NotificationType.THRESHOLD_ALERT;
    private static final NotificationType UPDATED_TYPE = NotificationType.ANOMALY_DETECTED;

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_MESSAGE = "AAAAAAAAAA";
    private static final String UPDATED_MESSAGE = "BBBBBBBBBB";

    private static final Boolean DEFAULT_IS_READ = false;
    private static final Boolean UPDATED_IS_READ = true;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/notifications";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepositoryMock;

    @Autowired
    private NotificationMapper notificationMapper;

    @Mock
    private NotificationService notificationServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restNotificationMockMvc;

    private Notification notification;

    private Notification insertedNotification;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Notification createEntity(EntityManager em) {
        Notification notification = new Notification()
            .type(DEFAULT_TYPE)
            .title(DEFAULT_TITLE)
            .message(DEFAULT_MESSAGE)
            .isRead(DEFAULT_IS_READ)
            .createdAt(DEFAULT_CREATED_AT);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        notification.setRecipientUser(user);
        return notification;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Notification createUpdatedEntity(EntityManager em) {
        Notification updatedNotification = new Notification()
            .type(UPDATED_TYPE)
            .title(UPDATED_TITLE)
            .message(UPDATED_MESSAGE)
            .isRead(UPDATED_IS_READ)
            .createdAt(UPDATED_CREATED_AT);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedNotification.setRecipientUser(user);
        return updatedNotification;
    }

    @BeforeEach
    void initTest() {
        notification = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedNotification != null) {
            notificationRepository.delete(insertedNotification);
            insertedNotification = null;
        }
    }

    @Test
    @Transactional
    void createNotification() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Notification
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);
        var returnedNotificationDTO = om.readValue(
            restNotificationMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            NotificationDTO.class
        );

        // Validate the Notification in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedNotification = notificationMapper.toEntity(returnedNotificationDTO);
        assertNotificationUpdatableFieldsEquals(returnedNotification, getPersistedNotification(returnedNotification));

        insertedNotification = returnedNotification;
    }

    @Test
    @Transactional
    void createNotificationWithExistingId() throws Exception {
        // Create the Notification with an existing ID
        notification.setId(1L);
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restNotificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Notification in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        notification.setType(null);

        // Create the Notification, which fails.
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        restNotificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTitleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        notification.setTitle(null);

        // Create the Notification, which fails.
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        restNotificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkIsReadIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        notification.setIsRead(null);

        // Create the Notification, which fails.
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        restNotificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        notification.setCreatedAt(null);

        // Create the Notification, which fails.
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        restNotificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllNotifications() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList
        restNotificationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(notification.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].message").value(hasItem(DEFAULT_MESSAGE)))
            .andExpect(jsonPath("$.[*].isRead").value(hasItem(DEFAULT_IS_READ)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllNotificationsWithEagerRelationshipsIsEnabled() throws Exception {
        when(notificationServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restNotificationMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(notificationServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllNotificationsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(notificationServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restNotificationMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(notificationRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getNotification() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get the notification
        restNotificationMockMvc
            .perform(get(ENTITY_API_URL_ID, notification.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(notification.getId().intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.message").value(DEFAULT_MESSAGE))
            .andExpect(jsonPath("$.isRead").value(DEFAULT_IS_READ))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNotificationsByIdFiltering() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        Long id = notification.getId();

        defaultNotificationFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultNotificationFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultNotificationFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllNotificationsByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where type equals to
        defaultNotificationFiltering("type.equals=" + DEFAULT_TYPE, "type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllNotificationsByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where type in
        defaultNotificationFiltering("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE, "type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllNotificationsByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where type is not null
        defaultNotificationFiltering("type.specified=true", "type.specified=false");
    }

    @Test
    @Transactional
    void getAllNotificationsByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where title equals to
        defaultNotificationFiltering("title.equals=" + DEFAULT_TITLE, "title.equals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllNotificationsByTitleIsInShouldWork() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where title in
        defaultNotificationFiltering("title.in=" + DEFAULT_TITLE + "," + UPDATED_TITLE, "title.in=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllNotificationsByTitleIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where title is not null
        defaultNotificationFiltering("title.specified=true", "title.specified=false");
    }

    @Test
    @Transactional
    void getAllNotificationsByTitleContainsSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where title contains
        defaultNotificationFiltering("title.contains=" + DEFAULT_TITLE, "title.contains=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllNotificationsByTitleNotContainsSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where title does not contain
        defaultNotificationFiltering("title.doesNotContain=" + UPDATED_TITLE, "title.doesNotContain=" + DEFAULT_TITLE);
    }

    @Test
    @Transactional
    void getAllNotificationsByIsReadIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where isRead equals to
        defaultNotificationFiltering("isRead.equals=" + DEFAULT_IS_READ, "isRead.equals=" + UPDATED_IS_READ);
    }

    @Test
    @Transactional
    void getAllNotificationsByIsReadIsInShouldWork() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where isRead in
        defaultNotificationFiltering("isRead.in=" + DEFAULT_IS_READ + "," + UPDATED_IS_READ, "isRead.in=" + UPDATED_IS_READ);
    }

    @Test
    @Transactional
    void getAllNotificationsByIsReadIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where isRead is not null
        defaultNotificationFiltering("isRead.specified=true", "isRead.specified=false");
    }

    @Test
    @Transactional
    void getAllNotificationsByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where createdAt equals to
        defaultNotificationFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllNotificationsByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where createdAt in
        defaultNotificationFiltering("createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT, "createdAt.in=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllNotificationsByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where createdAt is not null
        defaultNotificationFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    @Test
    @Transactional
    void getAllNotificationsByRecipientUserIsEqualToSomething() throws Exception {
        User recipientUser;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            notificationRepository.saveAndFlush(notification);
            recipientUser = UserResourceIT.createEntity();
        } else {
            recipientUser = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(recipientUser);
        em.flush();
        notification.setRecipientUser(recipientUser);
        notificationRepository.saveAndFlush(notification);
        Long recipientUserId = recipientUser.getId();
        // Get all the notificationList where recipientUser equals to recipientUserId
        defaultNotificationShouldBeFound("recipientUserId.equals=" + recipientUserId);

        // Get all the notificationList where recipientUser equals to (recipientUserId + 1)
        defaultNotificationShouldNotBeFound("recipientUserId.equals=" + (recipientUserId + 1));
    }

    @Test
    @Transactional
    void getAllNotificationsBySenderUserIsEqualToSomething() throws Exception {
        User senderUser;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            notificationRepository.saveAndFlush(notification);
            senderUser = UserResourceIT.createEntity();
        } else {
            senderUser = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(senderUser);
        em.flush();
        notification.setSenderUser(senderUser);
        notificationRepository.saveAndFlush(notification);
        Long senderUserId = senderUser.getId();
        // Get all the notificationList where senderUser equals to senderUserId
        defaultNotificationShouldBeFound("senderUserId.equals=" + senderUserId);

        // Get all the notificationList where senderUser equals to (senderUserId + 1)
        defaultNotificationShouldNotBeFound("senderUserId.equals=" + (senderUserId + 1));
    }

    @Test
    @Transactional
    void getAllNotificationsByRelatedPatientIsEqualToSomething() throws Exception {
        Patient relatedPatient;
        if (TestUtil.findAll(em, Patient.class).isEmpty()) {
            notificationRepository.saveAndFlush(notification);
            relatedPatient = PatientResourceIT.createEntity(em);
        } else {
            relatedPatient = TestUtil.findAll(em, Patient.class).get(0);
        }
        em.persist(relatedPatient);
        em.flush();
        notification.setRelatedPatient(relatedPatient);
        notificationRepository.saveAndFlush(notification);
        Long relatedPatientId = relatedPatient.getId();
        // Get all the notificationList where relatedPatient equals to relatedPatientId
        defaultNotificationShouldBeFound("relatedPatientId.equals=" + relatedPatientId);

        // Get all the notificationList where relatedPatient equals to (relatedPatientId + 1)
        defaultNotificationShouldNotBeFound("relatedPatientId.equals=" + (relatedPatientId + 1));
    }

    private void defaultNotificationFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultNotificationShouldBeFound(shouldBeFound);
        defaultNotificationShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultNotificationShouldBeFound(String filter) throws Exception {
        restNotificationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(notification.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].message").value(hasItem(DEFAULT_MESSAGE)))
            .andExpect(jsonPath("$.[*].isRead").value(hasItem(DEFAULT_IS_READ)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));

        // Check, that the count call also returns 1
        restNotificationMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultNotificationShouldNotBeFound(String filter) throws Exception {
        restNotificationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restNotificationMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingNotification() throws Exception {
        // Get the notification
        restNotificationMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingNotification() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the notification
        Notification updatedNotification = notificationRepository.findById(notification.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedNotification are not directly saved in db
        em.detach(updatedNotification);
        updatedNotification
            .type(UPDATED_TYPE)
            .title(UPDATED_TITLE)
            .message(UPDATED_MESSAGE)
            .isRead(UPDATED_IS_READ)
            .createdAt(UPDATED_CREATED_AT);
        NotificationDTO notificationDTO = notificationMapper.toDto(updatedNotification);

        restNotificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, notificationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(notificationDTO))
            )
            .andExpect(status().isOk());

        // Validate the Notification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedNotificationToMatchAllProperties(updatedNotification);
    }

    @Test
    @Transactional
    void putNonExistingNotification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        notification.setId(longCount.incrementAndGet());

        // Create the Notification
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNotificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, notificationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(notificationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Notification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchNotification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        notification.setId(longCount.incrementAndGet());

        // Create the Notification
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNotificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(notificationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Notification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamNotification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        notification.setId(longCount.incrementAndGet());

        // Create the Notification
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNotificationMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Notification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateNotificationWithPatch() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the notification using partial update
        Notification partialUpdatedNotification = new Notification();
        partialUpdatedNotification.setId(notification.getId());

        partialUpdatedNotification.type(UPDATED_TYPE).isRead(UPDATED_IS_READ);

        restNotificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedNotification.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedNotification))
            )
            .andExpect(status().isOk());

        // Validate the Notification in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertNotificationUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedNotification, notification),
            getPersistedNotification(notification)
        );
    }

    @Test
    @Transactional
    void fullUpdateNotificationWithPatch() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the notification using partial update
        Notification partialUpdatedNotification = new Notification();
        partialUpdatedNotification.setId(notification.getId());

        partialUpdatedNotification
            .type(UPDATED_TYPE)
            .title(UPDATED_TITLE)
            .message(UPDATED_MESSAGE)
            .isRead(UPDATED_IS_READ)
            .createdAt(UPDATED_CREATED_AT);

        restNotificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedNotification.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedNotification))
            )
            .andExpect(status().isOk());

        // Validate the Notification in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertNotificationUpdatableFieldsEquals(partialUpdatedNotification, getPersistedNotification(partialUpdatedNotification));
    }

    @Test
    @Transactional
    void patchNonExistingNotification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        notification.setId(longCount.incrementAndGet());

        // Create the Notification
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNotificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, notificationDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(notificationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Notification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchNotification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        notification.setId(longCount.incrementAndGet());

        // Create the Notification
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNotificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(notificationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Notification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamNotification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        notification.setId(longCount.incrementAndGet());

        // Create the Notification
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNotificationMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(notificationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Notification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteNotification() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the notification
        restNotificationMockMvc
            .perform(delete(ENTITY_API_URL_ID, notification.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return notificationRepository.count();
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

    protected Notification getPersistedNotification(Notification notification) {
        return notificationRepository.findById(notification.getId()).orElseThrow();
    }

    protected void assertPersistedNotificationToMatchAllProperties(Notification expectedNotification) {
        assertNotificationAllPropertiesEquals(expectedNotification, getPersistedNotification(expectedNotification));
    }

    protected void assertPersistedNotificationToMatchUpdatableProperties(Notification expectedNotification) {
        assertNotificationAllUpdatablePropertiesEquals(expectedNotification, getPersistedNotification(expectedNotification));
    }
}
