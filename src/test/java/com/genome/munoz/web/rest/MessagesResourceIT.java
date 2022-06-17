package com.genome.munoz.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.genome.munoz.IntegrationTest;
import com.genome.munoz.domain.Messages;
import com.genome.munoz.repository.MessagesRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link MessagesResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class MessagesResourceIT {

    private static final String DEFAULT_MESSAGE = "AAAAAAAAAA";
    private static final String UPDATED_MESSAGE = "BBBBBBBBBB";

    private static final Instant DEFAULT_HIRE_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_HIRE_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/messages";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private MessagesRepository messagesRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMessagesMockMvc;

    private Messages messages;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Messages createEntity(EntityManager em) {
        Messages messages = new Messages().message(DEFAULT_MESSAGE).hireDate(DEFAULT_HIRE_DATE);
        return messages;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Messages createUpdatedEntity(EntityManager em) {
        Messages messages = new Messages().message(UPDATED_MESSAGE).hireDate(UPDATED_HIRE_DATE);
        return messages;
    }

    @BeforeEach
    public void initTest() {
        messages = createEntity(em);
    }

    @Test
    @Transactional
    void createMessages() throws Exception {
        int databaseSizeBeforeCreate = messagesRepository.findAll().size();
        // Create the Messages
        restMessagesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(messages)))
            .andExpect(status().isCreated());

        // Validate the Messages in the database
        List<Messages> messagesList = messagesRepository.findAll();
        assertThat(messagesList).hasSize(databaseSizeBeforeCreate + 1);
        Messages testMessages = messagesList.get(messagesList.size() - 1);
        assertThat(testMessages.getMessage()).isEqualTo(DEFAULT_MESSAGE);
        assertThat(testMessages.getHireDate()).isEqualTo(DEFAULT_HIRE_DATE);
    }

    @Test
    @Transactional
    void createMessagesWithExistingId() throws Exception {
        // Create the Messages with an existing ID
        messages.setId(1L);

        int databaseSizeBeforeCreate = messagesRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMessagesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(messages)))
            .andExpect(status().isBadRequest());

        // Validate the Messages in the database
        List<Messages> messagesList = messagesRepository.findAll();
        assertThat(messagesList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllMessages() throws Exception {
        // Initialize the database
        messagesRepository.saveAndFlush(messages);

        // Get all the messagesList
        restMessagesMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(messages.getId().intValue())))
            .andExpect(jsonPath("$.[*].message").value(hasItem(DEFAULT_MESSAGE)))
            .andExpect(jsonPath("$.[*].hireDate").value(hasItem(DEFAULT_HIRE_DATE.toString())));
    }

    @Test
    @Transactional
    void getMessages() throws Exception {
        // Initialize the database
        messagesRepository.saveAndFlush(messages);

        // Get the messages
        restMessagesMockMvc
            .perform(get(ENTITY_API_URL_ID, messages.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(messages.getId().intValue()))
            .andExpect(jsonPath("$.message").value(DEFAULT_MESSAGE))
            .andExpect(jsonPath("$.hireDate").value(DEFAULT_HIRE_DATE.toString()));
    }

    @Test
    @Transactional
    void getNonExistingMessages() throws Exception {
        // Get the messages
        restMessagesMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewMessages() throws Exception {
        // Initialize the database
        messagesRepository.saveAndFlush(messages);

        int databaseSizeBeforeUpdate = messagesRepository.findAll().size();

        // Update the messages
        Messages updatedMessages = messagesRepository.findById(messages.getId()).get();
        // Disconnect from session so that the updates on updatedMessages are not directly saved in db
        em.detach(updatedMessages);
        updatedMessages.message(UPDATED_MESSAGE).hireDate(UPDATED_HIRE_DATE);

        restMessagesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedMessages.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedMessages))
            )
            .andExpect(status().isOk());

        // Validate the Messages in the database
        List<Messages> messagesList = messagesRepository.findAll();
        assertThat(messagesList).hasSize(databaseSizeBeforeUpdate);
        Messages testMessages = messagesList.get(messagesList.size() - 1);
        assertThat(testMessages.getMessage()).isEqualTo(UPDATED_MESSAGE);
        assertThat(testMessages.getHireDate()).isEqualTo(UPDATED_HIRE_DATE);
    }

    @Test
    @Transactional
    void putNonExistingMessages() throws Exception {
        int databaseSizeBeforeUpdate = messagesRepository.findAll().size();
        messages.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMessagesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, messages.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(messages))
            )
            .andExpect(status().isBadRequest());

        // Validate the Messages in the database
        List<Messages> messagesList = messagesRepository.findAll();
        assertThat(messagesList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMessages() throws Exception {
        int databaseSizeBeforeUpdate = messagesRepository.findAll().size();
        messages.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessagesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(messages))
            )
            .andExpect(status().isBadRequest());

        // Validate the Messages in the database
        List<Messages> messagesList = messagesRepository.findAll();
        assertThat(messagesList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMessages() throws Exception {
        int databaseSizeBeforeUpdate = messagesRepository.findAll().size();
        messages.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessagesMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(messages)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Messages in the database
        List<Messages> messagesList = messagesRepository.findAll();
        assertThat(messagesList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMessagesWithPatch() throws Exception {
        // Initialize the database
        messagesRepository.saveAndFlush(messages);

        int databaseSizeBeforeUpdate = messagesRepository.findAll().size();

        // Update the messages using partial update
        Messages partialUpdatedMessages = new Messages();
        partialUpdatedMessages.setId(messages.getId());

        restMessagesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMessages.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedMessages))
            )
            .andExpect(status().isOk());

        // Validate the Messages in the database
        List<Messages> messagesList = messagesRepository.findAll();
        assertThat(messagesList).hasSize(databaseSizeBeforeUpdate);
        Messages testMessages = messagesList.get(messagesList.size() - 1);
        assertThat(testMessages.getMessage()).isEqualTo(DEFAULT_MESSAGE);
        assertThat(testMessages.getHireDate()).isEqualTo(DEFAULT_HIRE_DATE);
    }

    @Test
    @Transactional
    void fullUpdateMessagesWithPatch() throws Exception {
        // Initialize the database
        messagesRepository.saveAndFlush(messages);

        int databaseSizeBeforeUpdate = messagesRepository.findAll().size();

        // Update the messages using partial update
        Messages partialUpdatedMessages = new Messages();
        partialUpdatedMessages.setId(messages.getId());

        partialUpdatedMessages.message(UPDATED_MESSAGE).hireDate(UPDATED_HIRE_DATE);

        restMessagesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMessages.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedMessages))
            )
            .andExpect(status().isOk());

        // Validate the Messages in the database
        List<Messages> messagesList = messagesRepository.findAll();
        assertThat(messagesList).hasSize(databaseSizeBeforeUpdate);
        Messages testMessages = messagesList.get(messagesList.size() - 1);
        assertThat(testMessages.getMessage()).isEqualTo(UPDATED_MESSAGE);
        assertThat(testMessages.getHireDate()).isEqualTo(UPDATED_HIRE_DATE);
    }

    @Test
    @Transactional
    void patchNonExistingMessages() throws Exception {
        int databaseSizeBeforeUpdate = messagesRepository.findAll().size();
        messages.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMessagesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, messages.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(messages))
            )
            .andExpect(status().isBadRequest());

        // Validate the Messages in the database
        List<Messages> messagesList = messagesRepository.findAll();
        assertThat(messagesList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMessages() throws Exception {
        int databaseSizeBeforeUpdate = messagesRepository.findAll().size();
        messages.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessagesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(messages))
            )
            .andExpect(status().isBadRequest());

        // Validate the Messages in the database
        List<Messages> messagesList = messagesRepository.findAll();
        assertThat(messagesList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMessages() throws Exception {
        int databaseSizeBeforeUpdate = messagesRepository.findAll().size();
        messages.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessagesMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(messages)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Messages in the database
        List<Messages> messagesList = messagesRepository.findAll();
        assertThat(messagesList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMessages() throws Exception {
        // Initialize the database
        messagesRepository.saveAndFlush(messages);

        int databaseSizeBeforeDelete = messagesRepository.findAll().size();

        // Delete the messages
        restMessagesMockMvc
            .perform(delete(ENTITY_API_URL_ID, messages.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Messages> messagesList = messagesRepository.findAll();
        assertThat(messagesList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
