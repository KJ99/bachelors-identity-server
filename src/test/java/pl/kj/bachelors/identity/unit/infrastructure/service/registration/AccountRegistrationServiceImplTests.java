package pl.kj.bachelors.identity.unit.infrastructure.service.registration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.TransactionSystemException;
import pl.kj.bachelors.identity.application.Application;
import pl.kj.bachelors.identity.application.exception.ConflictHttpException;
import pl.kj.bachelors.identity.domain.model.User;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;
import pl.kj.bachelors.identity.infrastructure.service.registration.AccountRegistrationServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest
@ContextConfiguration(classes = { Application.class })
public class AccountRegistrationServiceImplTests {
    @Autowired
    private AccountRegistrationServiceImpl service;

    @Autowired
    private UserRepository userRepository;

    private final String sampleUserEmail = "some-email@foobar.foo";
    private final String sampleUserUsername = "some-user-name";

    @BeforeEach
    private void setUp() {
        User user = new User();
        user.setUid("some-id");
        user.setEmail(this.sampleUserEmail);
        user.setUserName(this.sampleUserUsername);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("SomEP@ssword");
        user.setSalt("some salt");
        this.userRepository.save(user);
    }

    @Test
    public void testRegisterAccount_CorrectData() throws ConflictHttpException {
        final String email = "some-other-email@foobar.com";
        final String username = "foobaroo";
        final String firstName = "first";
        final String lastName = "last";
        final String password = "hello";

        this.service.registerAccount(email, username, firstName, lastName, password);

        Optional<User> user = this.userRepository.findByUserNameOrPassword(username);

        assertThat(user.isPresent()).isTrue();
    }

    @Test
    public void testRegisterAccount_ThrowsConflict_UsernameTaken() {
        final String username = this.sampleUserUsername;
        final String email = "some-other-email@foobar.com";
        final String firstName = "first";
        final String lastName = "last";
        final String password = "hello";

        Throwable thrown = catchThrowable(() -> {
            this.service.registerAccount(email, username, firstName, lastName, password);
        });

        assertThat(thrown).isInstanceOf(ConflictHttpException.class);

        ConflictHttpException ex = (ConflictHttpException) thrown;

        assertThat(ex.getError()).isNotNull();
        assertThat(ex.getError().getCode()).isEqualTo("ID.011");
        assertThat(ex.getError().getPath()).isEqualTo("username");
    }

    @Test
    public void testRegisterAccount_ThrowsConflict_EmailTaken() {
        final String email = this.sampleUserEmail;
        final String username = "foobaroo";
        final String firstName = "first";
        final String lastName = "last";
        final String password = "hello";

        Throwable thrown = catchThrowable(() -> {
            this.service.registerAccount(email, username, firstName, lastName, password);
        });
        assertThat(thrown).isInstanceOf(ConflictHttpException.class);

        ConflictHttpException ex = (ConflictHttpException) thrown;

        assertThat(ex.getError()).isNotNull();
        assertThat(ex.getError().getCode()).isEqualTo("ID.012");
        assertThat(ex.getError().getPath()).isEqualTo("email");
    }
}
