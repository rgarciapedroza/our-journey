package com.ourjourney.backend.repository;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;

import com.ourjourney.backend.entity.Trip;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TripRepositoryTest {
    
    @Autowired
    private TripRepository tripRepository;

    @Test
    void shouldSaveAndFindTrip() {
        Trip trip = Trip.builder()
                    .name("The Canary Islands 2027")
                    .description("New Years's trip")
                    .destination("Gran Canaria and Tenerife")
                    .startDate(LocalDate.of(2026, 12, 30))
                    .endDate(LocalDate.of(2027, 1, 2))
                    .coverImage("Maspalomas.jpg")
                    .build();

        Trip savedTrip = tripRepository.save(trip);

        assertThat(savedTrip.getId()).isNotNull();

        Trip foundTrip = tripRepository.findById(savedTrip.getId())
                .orElseThrow();

        assertThat(foundTrip.getName()).isEqualTo("The Canary Islands 2027");
        assertThat(foundTrip.getDescription()).isEqualTo("New Years's trip");
        assertThat(foundTrip.getDestination()).isEqualTo("Gran Canaria and Tenerife");
        assertThat(foundTrip.getStartDate())
                .isEqualTo(LocalDate.of(2026, 12, 30));
        assertThat(foundTrip.getEndDate())
                .isEqualTo(LocalDate.of(2027, 1, 2));

    }
}
