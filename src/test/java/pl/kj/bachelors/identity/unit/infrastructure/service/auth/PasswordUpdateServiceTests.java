package pl.kj.bachelors.identity.unit.infrastructure.service.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import pl.kj.bachelors.identity.BaseTest;
import pl.kj.bachelors.identity.domain.exception.ValidationViolation;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;
import pl.kj.bachelors.identity.infrastructure.service.auth.PasswordUpdateServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public class PasswordUpdateServiceTests extends BaseTest {
    @Autowired
    private PasswordUpdateServiceImpl service;
    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    public void testUpdatePassword_Correct() {
        final User user = this.userRepository.findById("uid-active-1").orElseThrow();

        Throwable thrown = catchThrowable(() ->
                this.service.updatePassword(user, "foobar", "new-password")
        );
        assertThat(thrown).isNull();
    }

    @Test
    public void testUpdatePassword_WrongCurrentPassword() {
        var user = this.userRepository.findById("uid-active-1").orElseThrow();

        assertThatThrownBy(() ->
                this.service.updatePassword(user, "wrong-pass", "new-password")
        ).isInstanceOf(ValidationViolation.class);

    }
}
