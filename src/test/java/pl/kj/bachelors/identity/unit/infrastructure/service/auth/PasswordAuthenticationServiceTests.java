package pl.kj.bachelors.identity.unit.infrastructure.service.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.ContextConfiguration;
import pl.kj.bachelors.identity.BaseTest;
import pl.kj.bachelors.identity.application.Application;
import pl.kj.bachelors.identity.domain.config.PasswordConfig;
import pl.kj.bachelors.identity.domain.exception.AccountNotVerifiedException;
import pl.kj.bachelors.identity.domain.exception.WrongCredentialsException;
import pl.kj.bachelors.identity.domain.model.AuthResult;
import pl.kj.bachelors.identity.domain.model.AuthResultDetail;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.model.entity.UserVerification;
import pl.kj.bachelors.identity.domain.model.payload.PasswordAuthPayload;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;
import pl.kj.bachelors.identity.infrastructure.repository.UserVerificationRepository;
import pl.kj.bachelors.identity.infrastructure.service.auth.PasswordAuthenticationService;

import java.util.Calendar;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.*;

public class PasswordAuthenticationServiceTests extends BaseTest {
    @Autowired
    private PasswordAuthenticationService service;
    @Autowired
    private UserVerificationRepository verificationRepository;

    @Test
    public void testAuthenticate_Correct_ByEmail() throws AccountNotVerifiedException, WrongCredentialsException {
        AuthResult<PasswordAuthPayload> result = this.service.authenticate("activeuser1@fakemail", "foobar");

        this.checkCorrectResult(result);
    }

    @Test
    public void testAuthenticate_Correct_ByUsername() throws AccountNotVerifiedException, WrongCredentialsException {
        AuthResult<PasswordAuthPayload> result = this.service.authenticate("active-1", "foobar");

        this.checkCorrectResult(result);
    }

    @Test
    private void checkCorrectResult(AuthResult<PasswordAuthPayload> result) {
        assertThat(result.getDetail()).isEqualTo(AuthResultDetail.SUCCESS);
        assertThat(result.getPayload()).isNotNull();
        assertThat(result.getPayload().getUser()).isNotNull();
        assertThat(result.getPayload().getUser().getEmail()).isEqualTo("activeuser1@fakemail");
        assertThat(result.getPayload().getToken()).isNotNull();
        assertThat(result.getPayload().getToken().getAccessToken()).isNotEmpty();
        assertThat(result.getPayload().getToken().getRefreshToken()).isNotEmpty();
    }

    @Test
    public void testAuthenticate_WrongPassword() {
        assertThatThrownBy(() -> this.service.authenticate("active-1", "fake-pass"))
                .isInstanceOf(WrongCredentialsException.class);

    }

    @Test
    public void testAuthenticate_AccountNotVerified() {
        var verification = this.verificationRepository.findByToken("active-token-1").orElseThrow();

        Throwable thrown = catchThrowable(() -> this.service.authenticate("notverified1@fakemail", "foobar"));

        assertThat(thrown).isInstanceOf(AccountNotVerifiedException.class);
        AccountNotVerifiedException ex = (AccountNotVerifiedException) thrown;
        assertThat(ex.getCode()).isEqualTo("ID.031");
        assertThat(ex.getLatestVerification()).isNotNull();
        assertThat(ex.getLatestVerification().getToken()).isEqualTo(verification.getToken());
    }
}
