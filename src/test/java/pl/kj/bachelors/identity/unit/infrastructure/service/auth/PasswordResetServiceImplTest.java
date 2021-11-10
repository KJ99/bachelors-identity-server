package pl.kj.bachelors.identity.unit.infrastructure.service.auth;

import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pl.kj.bachelors.identity.application.Application;
import pl.kj.bachelors.identity.domain.exception.AccessDeniedException;
import pl.kj.bachelors.identity.domain.exception.ValidationViolation;
import pl.kj.bachelors.identity.domain.model.entity.PasswordReset;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.infrastructure.repository.PasswordResetRepository;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;
import pl.kj.bachelors.identity.infrastructure.service.auth.PasswordResetServiceImpl;

import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = { Application.class })
public class PasswordResetServiceImplTest {
    @Autowired
    private PasswordResetServiceImpl service;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordResetRepository passwordResetRepository;

    private final String correctEmail = "correct-email";
    private final String correctToken = "correct-token";
    private final String otherCorrectToken = "correct-token";
    private final String correctPin = "123456";
    private final String expiredToken = "expired-token";

    @BeforeEach
    public void setUp() {
        this.loadData();
    }

    @Test
    public void testCreatePasswordReset_Correct() throws NotFoundException, ExecutionException, InterruptedException {
        PasswordReset result = this.service.createPasswordReset(this.correctEmail);

        assertThat(result).isNotNull();
        assertThat(result.getPin()).isNotEmpty();
        assertThat(result.getToken()).isNotEmpty();
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getEmail()).isEqualTo(this.correctEmail);
    }

    @Test
    public void testCreatePasswordReset_UserNotFound() {
        assertThatThrownBy(() -> this.service.createPasswordReset("fake-mail"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testResetPassword_Correct() {
        Throwable thrown = catchThrowable(() ->
                this.service.resetPassword(this.correctToken, this.correctPin, "newPass")
        );
        assertThat(thrown).isNull();
    }

    @Test
    public void testResetPassword_InvalidPin() {
        assertThatThrownBy(() ->
                this.service.resetPassword(this.otherCorrectToken, "fake-pin", "newPass")
        ).isInstanceOf(ValidationViolation.class);
    }

    @Test
    public void testResetPassword_InvalidToken() {
        assertThatThrownBy(() ->
                this.service.resetPassword("fake-token", this.correctPin, "newPass")
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testResetPassword_TokenExpired() {
        assertThatThrownBy(() ->
                this.service.resetPassword(this.expiredToken, this.correctPin, "newPass")
        ).isInstanceOf(AccessDeniedException.class);
    }

    private void loadData() {
        var user = new User();
        user.setUid("uid-1");
        user.setEmail(this.correctEmail);
        user.setUserName("username");
        user.setFirstName("Ab");
        user.setLastName("Ba");
        user.setSalt("salt");
        user.setPassword("pass");

        this.userRepository.saveAndFlush(user);

        var activeReset = new PasswordReset();
        activeReset.setToken(this.correctToken);
        activeReset.setPin(this.correctPin);
        activeReset.setUser(user);

        var otherActiveReset = new PasswordReset();
        otherActiveReset.setToken(this.otherCorrectToken);
        otherActiveReset.setPin("123456");
        otherActiveReset.setUser(user);

        var expiredReset = new PasswordReset();
        expiredReset.setToken(this.expiredToken);
        expiredReset.setPin("00000");
        expiredReset.setUser(user);

        Calendar future = Calendar.getInstance();
        future.add(Calendar.HOUR, 100);
        Calendar past = Calendar.getInstance();
        past.add(Calendar.HOUR, -1);

        activeReset.setExpiresAt(future);
        otherActiveReset.setExpiresAt(future);
        expiredReset.setExpiresAt(past);

        this.passwordResetRepository.saveAndFlush(activeReset);
        this.passwordResetRepository.saveAndFlush(otherActiveReset);
        this.passwordResetRepository.saveAndFlush(expiredReset);
    }
}
