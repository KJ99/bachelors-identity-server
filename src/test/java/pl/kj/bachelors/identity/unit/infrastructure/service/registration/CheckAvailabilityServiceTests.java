package pl.kj.bachelors.identity.unit.infrastructure.service.registration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.kj.bachelors.identity.BaseTest;
import pl.kj.bachelors.identity.infrastructure.service.registration.CheckAvailabilityService;

import static org.assertj.core.api.Assertions.assertThat;

public class CheckAvailabilityServiceTests extends BaseTest {
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
