package itee.licenta.monitorizare.web.rest;

import static itee.licenta.monitorizare.domain.ChatMessageAsserts.*;
import static itee.licenta.monitorizare.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import itee.licenta.monitorizare.IntegrationTest;
import itee.licenta.monitorizare.domain.ChatMessage;
import itee.licenta.monitorizare.domain.Patient;
import itee.licenta.monitorizare.domain.enumeration.ChatRole;
import itee.licenta.monitorizare.repository.ChatMessageRepository;
import itee.licenta.monitorizare.service.dto.ChatMessageDTO;
import itee.licenta.monitorizare.service.mapper.ChatMessageMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
 * Integration tests for the {@link ChatMessageResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ChatMessageResourceIT {

    private static final ChatRole DEFAULT_ROLE = ChatRole.USER;
    private static final ChatRole UPDATED_ROLE = ChatRole.ASSISTANT;

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final LocalDate DEFAULT_CONTEXT_START_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_CONTEXT_START_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_CONTEXT_END_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_CONTEXT_END_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String ENTITY_API_URL = "/api/chat-messages";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restChatMessageMockMvc;

    private ChatMessage chatMessage;

    private ChatMessage insertedChatMessage;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ChatMessage createEntity(EntityManager em) {
        ChatMessage chatMessage = new ChatMessage()
            .role(DEFAULT_ROLE)
            .content(DEFAULT_CONTENT)
            .createdAt(DEFAULT_CREATED_AT)
            .contextStartDate(DEFAULT_CONTEXT_START_DATE)
            .contextEndDate(DEFAULT_CONTEXT_END_DATE);
        // Add required entity
        Patient patient;
        if (TestUtil.findAll(em, Patient.class).isEmpty()) {
            patient = PatientResourceIT.createEntity(em);
            em.persist(patient);
            em.flush();
        } else {
            patient = TestUtil.findAll(em, Patient.class).get(0);
        }
        chatMessage.setPatient(patient);
        return chatMessage;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ChatMessage createUpdatedEntity(EntityManager em) {
        ChatMessage updatedChatMessage = new ChatMessage()
            .role(UPDATED_ROLE)
            .content(UPDATED_CONTENT)
            .createdAt(UPDATED_CREATED_AT)
            .contextStartDate(UPDATED_CONTEXT_START_DATE)
            .contextEndDate(UPDATED_CONTEXT_END_DATE);
        // Add required entity
        Patient patient;
        if (TestUtil.findAll(em, Patient.class).isEmpty()) {
            patient = PatientResourceIT.createUpdatedEntity(em);
            em.persist(patient);
            em.flush();
        } else {
            patient = TestUtil.findAll(em, Patient.class).get(0);
        }
        updatedChatMessage.setPatient(patient);
        return updatedChatMessage;
    }

    @BeforeEach
    void initTest() {
        chatMessage = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedChatMessage != null) {
            chatMessageRepository.delete(insertedChatMessage);
            insertedChatMessage = null;
        }
    }

    @Test
    @Transactional
    void createChatMessage() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ChatMessage
        ChatMessageDTO chatMessageDTO = chatMessageMapper.toDto(chatMessage);
        var returnedChatMessageDTO = om.readValue(
            restChatMessageMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(chatMessageDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ChatMessageDTO.class
        );

        // Validate the ChatMessage in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedChatMessage = chatMessageMapper.toEntity(returnedChatMessageDTO);
        assertChatMessageUpdatableFieldsEquals(returnedChatMessage, getPersistedChatMessage(returnedChatMessage));

        insertedChatMessage = returnedChatMessage;
    }

    @Test
    @Transactional
    void createChatMessageWithExistingId() throws Exception {
        // Create the ChatMessage with an existing ID
        chatMessage.setId(1L);
        ChatMessageDTO chatMessageDTO = chatMessageMapper.toDto(chatMessage);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restChatMessageMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(chatMessageDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ChatMessage in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkRoleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        chatMessage.setRole(null);

        // Create the ChatMessage, which fails.
        ChatMessageDTO chatMessageDTO = chatMessageMapper.toDto(chatMessage);

        restChatMessageMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(chatMessageDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        chatMessage.setCreatedAt(null);

        // Create the ChatMessage, which fails.
        ChatMessageDTO chatMessageDTO = chatMessageMapper.toDto(chatMessage);

        restChatMessageMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(chatMessageDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllChatMessages() throws Exception {
        // Initialize the database
        insertedChatMessage = chatMessageRepository.saveAndFlush(chatMessage);

        // Get all the chatMessageList
        restChatMessageMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(chatMessage.getId().intValue())))
            .andExpect(jsonPath("$.[*].role").value(hasItem(DEFAULT_ROLE.toString())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].contextStartDate").value(hasItem(DEFAULT_CONTEXT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].contextEndDate").value(hasItem(DEFAULT_CONTEXT_END_DATE.toString())));
    }

    @Test
    @Transactional
    void getChatMessage() throws Exception {
        // Initialize the database
        insertedChatMessage = chatMessageRepository.saveAndFlush(chatMessage);

        // Get the chatMessage
        restChatMessageMockMvc
            .perform(get(ENTITY_API_URL_ID, chatMessage.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(chatMessage.getId().intValue()))
            .andExpect(jsonPath("$.role").value(DEFAULT_ROLE.toString()))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.contextStartDate").value(DEFAULT_CONTEXT_START_DATE.toString()))
            .andExpect(jsonPath("$.contextEndDate").value(DEFAULT_CONTEXT_END_DATE.toString()));
    }

    @Test
    @Transactional
    void getNonExistingChatMessage() throws Exception {
        // Get the chatMessage
        restChatMessageMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingChatMessage() throws Exception {
        // Initialize the database
        insertedChatMessage = chatMessageRepository.saveAndFlush(chatMessage);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the chatMessage
        ChatMessage updatedChatMessage = chatMessageRepository.findById(chatMessage.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedChatMessage are not directly saved in db
        em.detach(updatedChatMessage);
        updatedChatMessage
            .role(UPDATED_ROLE)
            .content(UPDATED_CONTENT)
            .createdAt(UPDATED_CREATED_AT)
            .contextStartDate(UPDATED_CONTEXT_START_DATE)
            .contextEndDate(UPDATED_CONTEXT_END_DATE);
        ChatMessageDTO chatMessageDTO = chatMessageMapper.toDto(updatedChatMessage);

        restChatMessageMockMvc
            .perform(
                put(ENTITY_API_URL_ID, chatMessageDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(chatMessageDTO))
            )
            .andExpect(status().isOk());

        // Validate the ChatMessage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedChatMessageToMatchAllProperties(updatedChatMessage);
    }

    @Test
    @Transactional
    void putNonExistingChatMessage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        chatMessage.setId(longCount.incrementAndGet());

        // Create the ChatMessage
        ChatMessageDTO chatMessageDTO = chatMessageMapper.toDto(chatMessage);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restChatMessageMockMvc
            .perform(
                put(ENTITY_API_URL_ID, chatMessageDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(chatMessageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ChatMessage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchChatMessage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        chatMessage.setId(longCount.incrementAndGet());

        // Create the ChatMessage
        ChatMessageDTO chatMessageDTO = chatMessageMapper.toDto(chatMessage);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChatMessageMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(chatMessageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ChatMessage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamChatMessage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        chatMessage.setId(longCount.incrementAndGet());

        // Create the ChatMessage
        ChatMessageDTO chatMessageDTO = chatMessageMapper.toDto(chatMessage);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChatMessageMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(chatMessageDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ChatMessage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateChatMessageWithPatch() throws Exception {
        // Initialize the database
        insertedChatMessage = chatMessageRepository.saveAndFlush(chatMessage);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the chatMessage using partial update
        ChatMessage partialUpdatedChatMessage = new ChatMessage();
        partialUpdatedChatMessage.setId(chatMessage.getId());

        partialUpdatedChatMessage.role(UPDATED_ROLE).contextStartDate(UPDATED_CONTEXT_START_DATE);

        restChatMessageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedChatMessage.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedChatMessage))
            )
            .andExpect(status().isOk());

        // Validate the ChatMessage in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertChatMessageUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedChatMessage, chatMessage),
            getPersistedChatMessage(chatMessage)
        );
    }

    @Test
    @Transactional
    void fullUpdateChatMessageWithPatch() throws Exception {
        // Initialize the database
        insertedChatMessage = chatMessageRepository.saveAndFlush(chatMessage);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the chatMessage using partial update
        ChatMessage partialUpdatedChatMessage = new ChatMessage();
        partialUpdatedChatMessage.setId(chatMessage.getId());

        partialUpdatedChatMessage
            .role(UPDATED_ROLE)
            .content(UPDATED_CONTENT)
            .createdAt(UPDATED_CREATED_AT)
            .contextStartDate(UPDATED_CONTEXT_START_DATE)
            .contextEndDate(UPDATED_CONTEXT_END_DATE);

        restChatMessageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedChatMessage.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedChatMessage))
            )
            .andExpect(status().isOk());

        // Validate the ChatMessage in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertChatMessageUpdatableFieldsEquals(partialUpdatedChatMessage, getPersistedChatMessage(partialUpdatedChatMessage));
    }

    @Test
    @Transactional
    void patchNonExistingChatMessage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        chatMessage.setId(longCount.incrementAndGet());

        // Create the ChatMessage
        ChatMessageDTO chatMessageDTO = chatMessageMapper.toDto(chatMessage);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restChatMessageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, chatMessageDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(chatMessageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ChatMessage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchChatMessage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        chatMessage.setId(longCount.incrementAndGet());

        // Create the ChatMessage
        ChatMessageDTO chatMessageDTO = chatMessageMapper.toDto(chatMessage);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChatMessageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(chatMessageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ChatMessage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamChatMessage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        chatMessage.setId(longCount.incrementAndGet());

        // Create the ChatMessage
        ChatMessageDTO chatMessageDTO = chatMessageMapper.toDto(chatMessage);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChatMessageMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(chatMessageDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ChatMessage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteChatMessage() throws Exception {
        // Initialize the database
        insertedChatMessage = chatMessageRepository.saveAndFlush(chatMessage);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the chatMessage
        restChatMessageMockMvc
            .perform(delete(ENTITY_API_URL_ID, chatMessage.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return chatMessageRepository.count();
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

    protected ChatMessage getPersistedChatMessage(ChatMessage chatMessage) {
        return chatMessageRepository.findById(chatMessage.getId()).orElseThrow();
    }

    protected void assertPersistedChatMessageToMatchAllProperties(ChatMessage expectedChatMessage) {
        assertChatMessageAllPropertiesEquals(expectedChatMessage, getPersistedChatMessage(expectedChatMessage));
    }

    protected void assertPersistedChatMessageToMatchUpdatableProperties(ChatMessage expectedChatMessage) {
        assertChatMessageAllUpdatablePropertiesEquals(expectedChatMessage, getPersistedChatMessage(expectedChatMessage));
    }
}
