package com.genome.munoz.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.Instant;
import javax.persistence.*;

/**
 * /**\nThe Employee entity.
 */
@Schema(description = "/**\nThe Employee entity.")
@Entity
@Table(name = "messages")
public class Messages implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "message")
    private String message;

    @Column(name = "hire_date")
    private Instant hireDate;

    /**
     * Another side of the same relationship
     */
    @Schema(description = "Another side of the same relationship")
    @ManyToOne
    @JsonIgnoreProperties(value = { "messages" }, allowSetters = true)
    private Greeting greeting;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Messages id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return this.message;
    }

    public Messages message(String message) {
        this.setMessage(message);
        return this;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getHireDate() {
        return this.hireDate;
    }

    public Messages hireDate(Instant hireDate) {
        this.setHireDate(hireDate);
        return this;
    }

    public void setHireDate(Instant hireDate) {
        this.hireDate = hireDate;
    }

    public Greeting getGreeting() {
        return this.greeting;
    }

    public void setGreeting(Greeting greeting) {
        this.greeting = greeting;
    }

    public Messages greeting(Greeting greeting) {
        this.setGreeting(greeting);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Messages)) {
            return false;
        }
        return id != null && id.equals(((Messages) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Messages{" +
            "id=" + getId() +
            ", message='" + getMessage() + "'" +
            ", hireDate='" + getHireDate() + "'" +
            "}";
    }
}
