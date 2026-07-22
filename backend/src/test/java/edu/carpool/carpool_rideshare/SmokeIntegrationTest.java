package edu.carpool.carpool_rideshare;

import edu.carpool.carpool_rideshare.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class SmokeIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @Test
    void contextLoadsAndCanQueryDatabase() {
        long count = userRepository.count();
        assertThat(count).isEqualTo(0);
    }
}
