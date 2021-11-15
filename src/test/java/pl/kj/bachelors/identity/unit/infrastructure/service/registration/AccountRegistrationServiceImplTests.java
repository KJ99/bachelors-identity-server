package pl.kj.bachelors.identity.unit.infrastructure.service.registration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import pl.kj.bachelors.identity.BaseTest;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;
import pl.kj.bachelors.identity.infrastructure.service.registration.AccountRegistrationServiceImpl;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AccountRegistrationServiceImplTests extends BaseTest {
    @Autowired
    private AccountRegistrationServiceImpl service;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testRegisterAccount_CorrectData() throws ExecutionException, InterruptedException {
        final String email = "some-other-email@foobar.com";
        final String username = "foobaroo";
        final String firstName = "first";
        final String lastName = "last";
        final String password = "hello";

        this.service.registerAccount(email, username, firstName, lastName, password);

        Optional<User> user = this.userRepository.findByUserNameOrEmail(username);

        assertThat(user.isPresent()).isTrue();
    }

    @Test
    public void testRegisterAccount_ThrowsConflict_UsernameTaken() {
        final String username = "active-1";
        final String email = "some-other-email@foobar.com";
        final String firstName = "first";
        final String lastName = "last";
        final String password = "hello";

        assertThatThrownBy(() ->
                        this.service.registerAccount(email, username, firstName, lastName, password)
                ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testRegisterAccount_ThrowsConflict_EmailTaken() {
        final String email = "activeuser1@fakemail";
        final String username = "foobaroo";
        final String firstName = "first";
        final String lastName = "last";
        final String password = "hello";

        assertThatThrownBy(() ->
                this.service.registerAccount(email, username, firstName, lastName, password)
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
