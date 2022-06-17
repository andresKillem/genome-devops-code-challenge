package com.genome.munoz.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

/**
 * A Greeting.
 */
@Entity
@Table(name = "greeting")
public class Greeting implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "greeting")
    private String greeting;

    /**
     * A relationship
     */
    @Schema(description = "A relationship")
    @OneToMany(mappedBy = "greeting")
    @JsonIgnoreProperties(value = { "greeting" }, allowSetters = true)
    private Set<Messages> messages = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Greeting id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGreeting() {
        return this.greeting;
    }

    public Greeting greeting(String greeting) {
        this.setGreeting(greeting);
        return this;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public Set<Messages> getMessages() {
        return this.messages;
    }

    public void setMessages(Set<Messages> messages) {
        if (this.messages != null) {
            this.messages.forEach(i -> i.setGreeting(null));
        }
        if (messages != null) {
            messages.forEach(i -> i.setGreeting(this));
        }
        this.messages = messages;
    }

    public Greeting messages(Set<Messages> messages) {
        this.setMessages(messages);
        return this;
    }

    public Greeting addMessages(Messages messages) {
        this.messages.add(messages);
        messages.setGreeting(this);
        return this;
    }

    public Greeting removeMessages(Messages messages) {
        this.messages.remove(messages);
        messages.setGreeting(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Greeting)) {
            return false;
        }
        return id != null && id.equals(((Greeting) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Greeting{" +
            "id=" + getId() +
            ", greeting='" + getGreeting() + "'" +
            "}";
    }
}
