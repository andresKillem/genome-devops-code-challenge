package com.genome.munoz.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.genome.munoz.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class GreetingTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Greeting.class);
        Greeting greeting1 = new Greeting();
        greeting1.setId(1L);
        Greeting greeting2 = new Greeting();
        greeting2.setId(greeting1.getId());
        assertThat(greeting1).isEqualTo(greeting2);
        greeting2.setId(2L);
        assertThat(greeting1).isNotEqualTo(greeting2);
        greeting1.setId(null);
        assertThat(greeting1).isNotEqualTo(greeting2);
    }
}
