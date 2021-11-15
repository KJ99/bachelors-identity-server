package pl.kj.bachelors.identity.unit.infrastructure.service.auth;

import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pl.kj.bachelors.identity.BaseTest;
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

public class PasswordResetServiceImplTest extends BaseTest {
    @Autowired
    private PasswordResetServiceImpl service;

    @Test
    public void testCreatePasswordReset_Correct() throws NotFoundException, ExecutionException, InterruptedException {
        PasswordReset result = this.service.createPasswordReset("activeuser1@fakemail");

        assertThat(result).isNotNull();
        assertThat(result.getPin()).isNotEmpty();
        assertThat(result.getToken()).isNotEmpty();
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getEmail()).isEqualTo("activeuser1@fakemail");
    }

    @Test
    public void testCreatePasswordReset_UserNotFound() {
        assertThatThrownBy(() -> this.service.createPasswordReset("fake-mail"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testResetPassword_Correct() {
        Throwable thrown = catchThrowable(() ->
                this.service.resetPassword("active-token-1", "012345", "newPass")
        );
        assertThat(thrown).isNull();
    }

    @Test
    public void testResetPassword_InvalidPin() {
        assertThatThrownBy(() ->
                this.service.resetPassword("active-token-1", "fake-pin", "newPass")
        ).isInstanceOf(ValidationViolation.class);
    }

    @Test
    public void testResetPassword_InvalidToken() {
        assertThatThrownBy(() ->
                this.service.resetPassword("fake-token", "012345", "newPass")
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testResetPassword_TokenExpired() {
        assertThatThrownBy(() ->
                this.service.resetPassword("expired-token-1", "012345", "newPass")
        ).isInstanceOf(AccessDeniedException.class);
    }
}
