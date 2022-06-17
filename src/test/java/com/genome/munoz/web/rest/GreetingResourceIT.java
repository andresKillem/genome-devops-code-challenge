package com.genome.munoz.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.genome.munoz.IntegrationTest;
import com.genome.munoz.domain.Greeting;
import com.genome.munoz.repository.GreetingRepository;
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
 * Integration tests for the {@link GreetingResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class GreetingResourceIT {

    private static final String DEFAULT_GREETING = "AAAAAAAAAA";
    private static final String UPDATED_GREETING = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/greeting";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private GreetingRepository greetingRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restGreetingMockMvc;

    private Greeting greeting;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Greeting createEntity(EntityManager em) {
        Greeting greeting = new Greeting().greeting(DEFAULT_GREETING);
        return greeting;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Greeting createUpdatedEntity(EntityManager em) {
        Greeting greeting = new Greeting().greeting(UPDATED_GREETING);
        return greeting;
    }

    @BeforeEach
    public void initTest() {
        greeting = createEntity(em);
    }

    @Test
    @Transactional
    void createGreeting() throws Exception {
        int databaseSizeBeforeCreate = greetingRepository.findAll().size();
        // Create the Greeting
        restGreetingMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(greeting)))
            .andExpect(status().isCreated());

        // Validate the Greeting in the database
        List<Greeting> greetingList = greetingRepository.findAll();
        assertThat(greetingList).hasSize(databaseSizeBeforeCreate + 1);
        Greeting testGreeting = greetingList.get(greetingList.size() - 1);
        assertThat(testGreeting.getGreeting()).isEqualTo(DEFAULT_GREETING);
    }

    @Test
    @Transactional
    void createGreetingWithExistingId() throws Exception {
        // Create the Greeting with an existing ID
        greeting.setId(1L);

        int databaseSizeBeforeCreate = greetingRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restGreetingMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(greeting)))
            .andExpect(status().isBadRequest());

        // Validate the Greeting in the database
        List<Greeting> greetingList = greetingRepository.findAll();
        assertThat(greetingList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllGreetings() throws Exception {
        // Initialize the database
        greetingRepository.saveAndFlush(greeting);

        // Get all the greetingList
        restGreetingMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(greeting.getId().intValue())))
            .andExpect(jsonPath("$.[*].greeting").value(hasItem(DEFAULT_GREETING)));
    }

    @Test
    @Transactional
    void getGreeting() throws Exception {
        // Initialize the database
        greetingRepository.saveAndFlush(greeting);

        // Get the greeting
        restGreetingMockMvc
            .perform(get(ENTITY_API_URL_ID, greeting.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(greeting.getId().intValue()))
            .andExpect(jsonPath("$.greeting").value(DEFAULT_GREETING));
    }

    @Test
    @Transactional
    void getNonExistingGreeting() throws Exception {
        // Get the greeting
        restGreetingMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewGreeting() throws Exception {
        // Initialize the database
        greetingRepository.saveAndFlush(greeting);

        int databaseSizeBeforeUpdate = greetingRepository.findAll().size();

        // Update the greeting
        Greeting updatedGreeting = greetingRepository.findById(greeting.getId()).get();
        // Disconnect from session so that the updates on updatedGreeting are not directly saved in db
        em.detach(updatedGreeting);
        updatedGreeting.greeting(UPDATED_GREETING);

        restGreetingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedGreeting.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedGreeting))
            )
            .andExpect(status().isOk());

        // Validate the Greeting in the database
        List<Greeting> greetingList = greetingRepository.findAll();
        assertThat(greetingList).hasSize(databaseSizeBeforeUpdate);
        Greeting testGreeting = greetingList.get(greetingList.size() - 1);
        assertThat(testGreeting.getGreeting()).isEqualTo(UPDATED_GREETING);
    }

    @Test
    @Transactional
    void putNonExistingGreeting() throws Exception {
        int databaseSizeBeforeUpdate = greetingRepository.findAll().size();
        greeting.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGreetingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, greeting.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(greeting))
            )
            .andExpect(status().isBadRequest());

        // Validate the Greeting in the database
        List<Greeting> greetingList = greetingRepository.findAll();
        assertThat(greetingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchGreeting() throws Exception {
        int databaseSizeBeforeUpdate = greetingRepository.findAll().size();
        greeting.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGreetingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(greeting))
            )
            .andExpect(status().isBadRequest());

        // Validate the Greeting in the database
        List<Greeting> greetingList = greetingRepository.findAll();
        assertThat(greetingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamGreeting() throws Exception {
        int databaseSizeBeforeUpdate = greetingRepository.findAll().size();
        greeting.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGreetingMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(greeting)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Greeting in the database
        List<Greeting> greetingList = greetingRepository.findAll();
        assertThat(greetingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateGreetingWithPatch() throws Exception {
        // Initialize the database
        greetingRepository.saveAndFlush(greeting);

        int databaseSizeBeforeUpdate = greetingRepository.findAll().size();

        // Update the greeting using partial update
        Greeting partialUpdatedGreeting = new Greeting();
        partialUpdatedGreeting.setId(greeting.getId());

        restGreetingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGreeting.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedGreeting))
            )
            .andExpect(status().isOk());

        // Validate the Greeting in the database
        List<Greeting> greetingList = greetingRepository.findAll();
        assertThat(greetingList).hasSize(databaseSizeBeforeUpdate);
        Greeting testGreeting = greetingList.get(greetingList.size() - 1);
        assertThat(testGreeting.getGreeting()).isEqualTo(DEFAULT_GREETING);
    }

    @Test
    @Transactional
    void fullUpdateGreetingWithPatch() throws Exception {
        // Initialize the database
        greetingRepository.saveAndFlush(greeting);

        int databaseSizeBeforeUpdate = greetingRepository.findAll().size();

        // Update the greeting using partial update
        Greeting partialUpdatedGreeting = new Greeting();
        partialUpdatedGreeting.setId(greeting.getId());

        partialUpdatedGreeting.greeting(UPDATED_GREETING);

        restGreetingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGreeting.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedGreeting))
            )
            .andExpect(status().isOk());

        // Validate the Greeting in the database
        List<Greeting> greetingList = greetingRepository.findAll();
        assertThat(greetingList).hasSize(databaseSizeBeforeUpdate);
        Greeting testGreeting = greetingList.get(greetingList.size() - 1);
        assertThat(testGreeting.getGreeting()).isEqualTo(UPDATED_GREETING);
    }

    @Test
    @Transactional
    void patchNonExistingGreeting() throws Exception {
        int databaseSizeBeforeUpdate = greetingRepository.findAll().size();
        greeting.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGreetingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, greeting.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(greeting))
            )
            .andExpect(status().isBadRequest());

        // Validate the Greeting in the database
        List<Greeting> greetingList = greetingRepository.findAll();
        assertThat(greetingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchGreeting() throws Exception {
        int databaseSizeBeforeUpdate = greetingRepository.findAll().size();
        greeting.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGreetingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(greeting))
            )
            .andExpect(status().isBadRequest());

        // Validate the Greeting in the database
        List<Greeting> greetingList = greetingRepository.findAll();
        assertThat(greetingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamGreeting() throws Exception {
        int databaseSizeBeforeUpdate = greetingRepository.findAll().size();
        greeting.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGreetingMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(greeting)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Greeting in the database
        List<Greeting> greetingList = greetingRepository.findAll();
        assertThat(greetingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteGreeting() throws Exception {
        // Initialize the database
        greetingRepository.saveAndFlush(greeting);

        int databaseSizeBeforeDelete = greetingRepository.findAll().size();

        // Delete the greeting
        restGreetingMockMvc
            .perform(delete(ENTITY_API_URL_ID, greeting.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Greeting> greetingList = greetingRepository.findAll();
        assertThat(greetingList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
