package pl.kj.bachelors.identity.unit.infrastructure.service.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.ContextConfiguration;
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

@SpringBootTest
@ContextConfiguration(classes = { Application.class })
public class PasswordAuthenticationServiceTests {
    @Autowired
    private PasswordAuthenticationService service;
    @Autowired
    private PasswordConfig passwordConfig;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserVerificationRepository verificationRepository;
    private final String correctEmail = "correct-email@testaroo.test";
    private final String correctUsername = "usernamo";
    private final String correctPassword = "P@ssw0rdo";

    @Test
    public void testAuthenticate_Correct_ByEmail() throws AccountNotVerifiedException, WrongCredentialsException {
        this.loadUser(this.correctEmail, this.correctUsername, this.correctPassword, true);

        AuthResult<PasswordAuthPayload> result = this.service.authenticate(this.correctEmail, this.correctPassword);

        this.checkCorrectResult(result);
    }

    @Test
    public void testAuthenticate_Correct_ByUsername() throws AccountNotVerifiedException, WrongCredentialsException {
        this.loadUser(this.correctEmail, this.correctUsername, this.correctPassword, true);

        AuthResult<PasswordAuthPayload> result = this.service.authenticate(this.correctUsername, this.correctPassword);

        this.checkCorrectResult(result);
    }

    @Test
    private void checkCorrectResult(AuthResult<PasswordAuthPayload> result) {
        assertThat(result.getDetail()).isEqualTo(AuthResultDetail.SUCCESS);
        assertThat(result.getPayload()).isNotNull();
        assertThat(result.getPayload().getUser()).isNotNull();
        assertThat(result.getPayload().getUser().getEmail()).isEqualTo(this.correctEmail);
        assertThat(result.getPayload().getToken()).isNotNull();
        assertThat(result.getPayload().getToken().getAccessToken()).isNotEmpty();
        assertThat(result.getPayload().getToken().getRefreshToken()).isNotEmpty();
    }

    @Test
    public void testAuthenticate_WrongPassword() {
        this.loadUser(this.correctEmail, this.correctUsername, this.correctPassword, true);

        assertThatThrownBy(() -> this.service.authenticate(this.correctUsername, "fake-pass"))
                .isInstanceOf(WrongCredentialsException.class);

    }

    @Test
    public void testAuthenticate_AccountNotVerified() {
        var user = this.loadUser(this.correctEmail, this.correctUsername, this.correctPassword, false);
        var verification = this.loadVerification(user);

        Throwable thrown = catchThrowable(() -> this.service.authenticate(this.correctEmail, this.correctPassword));

        assertThat(thrown).isInstanceOf(AccountNotVerifiedException.class);
        AccountNotVerifiedException ex = (AccountNotVerifiedException) thrown;
        assertThat(ex.getCode()).isEqualTo("ID.031");
        assertThat(ex.getLatestVerification()).isNotNull();
        assertThat(ex.getLatestVerification().getToken()).isEqualTo(verification.getToken());
    }

    private User loadUser(String email, String username, String password, boolean verified) {
        String salt = BCrypt.gensalt(this.passwordConfig.getSaltRounds());
        String hash = BCrypt.hashpw(password, salt);
        User user = new User();
        user.setUid("uid-10");
        user.setEmail(email);
        user.setUserName(username);
        user.setFirstName("Abc");
        user.setLastName("Abc");
        user.setPassword(hash);
        user.setSalt(salt);
        user.setVerified(verified);
        user.setActive(true);

        this.userRepository.saveAndFlush(user);

        return user;
    }

    private UserVerification loadVerification(User user) {
        UserVerification verification = new UserVerification();
        verification.setExpiresAt(Calendar.getInstance());
        verification.setPin("000000");
        verification.setToken("veri-token");
        verification.setUser(user);

        this.verificationRepository.saveAndFlush(verification);

        return verification;
    }
}
