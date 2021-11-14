package pl.kj.bachelors.identity.unit.infrastructure.service.registration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import pl.kj.bachelors.identity.application.Application;
import pl.kj.bachelors.identity.infrastructure.service.registration.CheckAvailabilityService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = {Application.class})
@Sql(value = "/db.test/seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/db.test/clear.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CheckAvailabilityServiceTests {
    @Autowired
    private CheckAvailabilityService service;

    @Test
    public void test_IsAvailable_ByEmail_ReturnsTrue() {
        boolean result = this.service.isAvailable("email", "fresh@mail");
        assertThat(result).isTrue();
    }

    @Test
    public void test_IsAvailable_ByUsername_ReturnsTrue() {
        boolean result = this.service.isAvailable("username", "fresh-user");
        assertThat(result).isTrue();
    }

    @Test
    public void test_IsAvailable_ByEmail_ReturnsFalse() {
        boolean result = this.service.isAvailable("email", "activeuser1@fakemail");
        assertThat(result).isFalse();
    }

    @Test
    public void test_IsAvailable_ByUsername_ReturnsFalse() {
        boolean result = this.service.isAvailable("username", "active-1");
        assertThat(result).isFalse();
    }

}