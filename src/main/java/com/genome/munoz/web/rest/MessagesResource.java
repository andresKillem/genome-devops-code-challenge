package com.genome.munoz.web.rest;

import com.genome.munoz.domain.Messages;
import com.genome.munoz.repository.MessagesRepository;
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
 * REST controller for managing {@link com.genome.munoz.domain.Messages}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class MessagesResource {

    private final Logger log = LoggerFactory.getLogger(MessagesResource.class);

    private static final String ENTITY_NAME = "genomeMessages";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MessagesRepository messagesRepository;

    public MessagesResource(MessagesRepository messagesRepository) {
        this.messagesRepository = messagesRepository;
    }

    /**
     * {@code POST  /messages} : Create a new messages.
     *
     * @param messages the messages to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new messages, or with status {@code 400 (Bad Request)} if the messages has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/messages")
    public ResponseEntity<Messages> createMessages(@RequestBody Messages messages) throws URISyntaxException {
        log.debug("REST request to save Messages : {}", messages);
        if (messages.getId() != null) {
            throw new BadRequestAlertException("A new messages cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Messages result = messagesRepository.save(messages);
        return ResponseEntity
            .created(new URI("/api/messages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /messages/:id} : Updates an existing messages.
     *
     * @param id the id of the messages to save.
     * @param messages the messages to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated messages,
     * or with status {@code 400 (Bad Request)} if the messages is not valid,
     * or with status {@code 500 (Internal Server Error)} if the messages couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/messages/{id}")
    public ResponseEntity<Messages> updateMessages(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Messages messages
    ) throws URISyntaxException {
        log.debug("REST request to update Messages : {}, {}", id, messages);
        if (messages.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, messages.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!messagesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Messages result = messagesRepository.save(messages);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, messages.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /messages/:id} : Partial updates given fields of an existing messages, field will ignore if it is null
     *
     * @param id the id of the messages to save.
     * @param messages the messages to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated messages,
     * or with status {@code 400 (Bad Request)} if the messages is not valid,
     * or with status {@code 404 (Not Found)} if the messages is not found,
     * or with status {@code 500 (Internal Server Error)} if the messages couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/messages/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Messages> partialUpdateMessages(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Messages messages
    ) throws URISyntaxException {
        log.debug("REST request to partial update Messages partially : {}, {}", id, messages);
        if (messages.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, messages.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!messagesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Messages> result = messagesRepository
            .findById(messages.getId())
            .map(existingMessages -> {
                if (messages.getMessage() != null) {
                    existingMessages.setMessage(messages.getMessage());
                }
                if (messages.getHireDate() != null) {
                    existingMessages.setHireDate(messages.getHireDate());
                }

                return existingMessages;
            })
            .map(messagesRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, messages.getId().toString())
        );
    }

    /**
     * {@code GET  /messages} : get all the messages.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of messages in body.
     */
    @GetMapping("/messages")
    public List<Messages> getAllMessages() {
        log.debug("REST request to get all Messages");
        return messagesRepository.findAll();
    }

    /**
     * {@code GET  /messages/:id} : get the "id" messages.
     *
     * @param id the id of the messages to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the messages, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/messages/{id}")
    public ResponseEntity<Messages> getMessages(@PathVariable Long id) {
        log.debug("REST request to get Messages : {}", id);
        Optional<Messages> messages = messagesRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(messages);
    }

    /**
     * {@code DELETE  /messages/:id} : delete the "id" messages.
     *
     * @param id the id of the messages to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> deleteMessages(@PathVariable Long id) {
        log.debug("REST request to delete Messages : {}", id);
        messagesRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
