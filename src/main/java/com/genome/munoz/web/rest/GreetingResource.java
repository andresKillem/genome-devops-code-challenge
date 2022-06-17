package com.genome.munoz.web.rest;

import com.genome.munoz.domain.Greeting;
import com.genome.munoz.repository.GreetingRepository;
import com.genome.munoz.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.genome.munoz.domain.Greeting}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class GreetingResource {

    private final Logger log = LoggerFactory.getLogger(GreetingResource.class);

    private static final String ENTITY_NAME = "genomeGreeting";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final GreetingRepository greetingRepository;


    public GreetingResource(GreetingRepository greetingRepository) {
        this.greetingRepository = greetingRepository;
    }

    /**
     * {@code POST  /greeting} : Create a new greeting.
     *
     * @param greeting the greeting to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new greeting, or with status {@code 400 (Bad Request)} if the greeting has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/greeting")
    public ResponseEntity<Greeting> createGreeting(@RequestBody Greeting greeting) throws URISyntaxException {
        log.debug("REST request to save Greeting : {}", greeting);
        if (greeting.getId() != null) {
            throw new BadRequestAlertException("A new greeting cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Greeting result = greetingRepository.save(greeting);
        return ResponseEntity
            .created(new URI("/api/greeting/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /greeting/:id} : Updates an existing greeting.
     *
     * @param id the id of the greeting to save.
     * @param greeting the greeting to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated greeting,
     * or with status {@code 400 (Bad Request)} if the greeting is not valid,
     * or with status {@code 500 (Internal Server Error)} if the greeting couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/greeting/{id}")
    public ResponseEntity<Greeting> updateGreeting(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Greeting greeting
    ) throws URISyntaxException {
        log.debug("REST request to update Greeting : {}, {}", id, greeting);
        if (greeting.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, greeting.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!greetingRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Greeting result = greetingRepository.save(greeting);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, greeting.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /greeting/:id} : Partial updates given fields of an existing greeting, field will ignore if it is null
     *
     * @param id the id of the greeting to save.
     * @param greeting the greeting to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated greeting,
     * or with status {@code 400 (Bad Request)} if the greeting is not valid,
     * or with status {@code 404 (Not Found)} if the greeting is not found,
     * or with status {@code 500 (Internal Server Error)} if the greeting couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/greeting/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Greeting> partialUpdateGreeting(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Greeting greeting
    ) throws URISyntaxException {
        log.debug("REST request to partial update Greeting partially : {}, {}", id, greeting);
        if (greeting.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, greeting.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!greetingRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Greeting> result = greetingRepository
            .findById(greeting.getId())
            .map(existingGreeting -> {
                if (greeting.getGreeting() != null) {
                    existingGreeting.setGreeting(greeting.getGreeting());
                }

                return existingGreeting;
            })
            .map(greetingRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, greeting.getId().toString())
        );
    }

    /**
     * {@code GET  /greeting} : get all the greetings.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of greetings in body.
     */
    @GetMapping("/greetings")
    public List<Greeting> getAllGreetings() {
        log.debug("REST request to get all Greetings");
        return greetingRepository.findAll();
    }

    /**
     * {@code GET  /greeting/:id} : get the "id" greeting.
     *
     * @param id the id of the greeting to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the greeting, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/greeting/{id}")
    public ResponseEntity<Greeting> getGreeting(@PathVariable Long id) {
        log.debug("REST request to get Greeting : {}", id);
        Optional<Greeting> greeting = greetingRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(greeting);
    }

    /**
     * {@code DELETE  /greeting/:id} : delete the "id" greeting.
     *
     * @param id the id of the greeting to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/greeting/{id}")
    public ResponseEntity<Void> deleteGreeting(@PathVariable Long id) {
        log.debug("REST request to delete Greeting : {}", id);
        greetingRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
