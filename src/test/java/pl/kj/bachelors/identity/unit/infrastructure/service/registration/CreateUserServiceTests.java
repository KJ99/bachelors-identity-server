package pl.kj.bachelors.identity.unit.infrastructure.service.registration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.kj.bachelors.identity.BaseTest;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.infrastructure.service.registration.CreateUserService;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateUserServiceTests extends BaseTest {
    @Autowired
    private CreateUserService service;

    @Test
    public void testCreateUser() {
        final String email = "some-email@foobar.com";
        final String username = "username";
        final String firstName = "first";
        final String lastName = "last";
        final String password = "hello";

        User result = this.service.createUser(email, username, firstName, lastName, password);

        assertThat(result.getUid())
                .isNotNull()
                .isNotEmpty();
        assertThat(result.getPassword())
                .isNotNull()
                .isNotEmpty()
                .isNotEqualTo(password);

        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getFirstName()).isEqualTo(firstName);
        assertThat(result.getLastName()).isEqualTo(lastName);
        assertThat(result.getUserName()).isEqualTo(username);
    }
}
