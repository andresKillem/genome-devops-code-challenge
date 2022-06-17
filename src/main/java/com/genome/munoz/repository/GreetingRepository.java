package com.genome.munoz.repository;

import com.genome.munoz.domain.Greeting;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Greeting entity.
 */
@SuppressWarnings("unused")
@Repository
public interface GreetingRepository extends JpaRepository<Greeting, Long> {}
