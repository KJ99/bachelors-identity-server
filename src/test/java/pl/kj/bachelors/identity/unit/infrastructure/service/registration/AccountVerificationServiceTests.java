package pl.kj.bachelors.identity.unit.infrastructure.service.registration;

import javassist.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.kj.bachelors.identity.BaseTest;
import pl.kj.bachelors.identity.domain.exception.ValidationViolation;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.model.entity.UserVerification;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;
import pl.kj.bachelors.identity.infrastructure.repository.UserVerificationRepository;
import pl.kj.bachelors.identity.infrastructure.service.registration.AccountVerificationService;

import java.util.Calendar;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;

class AccountVerificationServiceTests extends BaseTest {
    @Autowired
    private AccountVerificationService service;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserVerificationRepository verificationRepository;

    @Test
    public void testCreateVerification_ByUser() throws ExecutionException, InterruptedException {
        User user = this.userRepository.findById("uid-active-1").orElseThrow();

        UserVerification verification = this.service.createVerification(user);
        this.checkVerification(verification, user.getUid());
    }

    @Test
    public void testCreateVerification_ByEmail() throws NotFoundException, ExecutionException, InterruptedException {
        UserVerification verification = this.service.createVerification("notverified1@fakemail");
        this.checkVerification(verification, "uid-not-verified-1");
    }

    @Test
    public void testCreateVerification_ByEmail_NotFound() {
        assertThatThrownBy(() -> this.service.createVerification("fake-mail"))
                .isInstanceOf(NotFoundException.class);
    }

    private void checkVerification(UserVerification verification, String uid) {
        assertThat(verification.getUser().getUid()).isEqualTo(uid);
        assertThat(verification.getPin())
                .isNotNull()
                .isNotEmpty()
                .matches("[0-9]{6}");
        assertThat(verification.getToken())
                .isNotNull()
                .isNotEmpty();
        assertThat(verification.getExpiresAt()).isNotNull();
        assertThat(verification.getExpiresAt().getTimeInMillis())
                .isGreaterThan(Calendar.getInstance().getTimeInMillis());
    }

    @Test
    public void testVerifyUser_CorrectResult() throws NotFoundException, ValidationViolation {
        String uid = this.verificationRepository.findByToken("active-token-1").orElseThrow().getUser().getUid();

        this.service.verifyUser("active-token-1","012345");

        User user = this.userRepository.findById(uid).orElseThrow();
        Optional<UserVerification> verification = this.verificationRepository.findByToken("active-token-1");

        assertThat(user.isVerified()).isTrue();
        assertThat(user.isActive()).isTrue();
        assertThat(verification.isPresent()).isFalse();
    }

    @Test
    public void testVerifyUser_IncorrectToken() {
        assertThatThrownBy(() ->
                service.verifyUser("fake-token", "012345")
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testSaveVerification() {
        final String token = "some-token";
        User user = this.userRepository.findById("uid-active-1").orElseThrow();
        UserVerification verification = this.createUserVerification(user);

        this.service.saveVerification(verification);

        assertThat(this.verificationRepository.findByToken(token).isPresent()).isTrue();
    }

    @Test
    public void testVerifyUser_PinNotMatch() {
        Throwable thrown = catchThrowable(() ->
                service.verifyUser("active-token-1", "fake-pin")
        );

        assertThat(thrown).isInstanceOf(ValidationViolation.class);

        ValidationViolation violation = (ValidationViolation) thrown;

        assertThat(violation.getCode()).isEqualTo("ID.021");
        assertThat(violation.getPath()).isEqualTo("pin");
    }

    @Test
    public void testVerifyUser_PinExpired() {
        Throwable thrown = catchThrowable(() ->
                service.verifyUser("expired-token-1", "012345")
        );

        assertThat(thrown).isInstanceOf(ValidationViolation.class);

        ValidationViolation violation = (ValidationViolation) thrown;

        assertThat(violation.getCode()).isEqualTo("ID.022");
        assertThat(violation.getPath()).isEqualTo("pin");

    }

    @Test
    public void testVerifyUser_AccountAlreadyVerified() {
        Throwable thrown = catchThrowable(() ->
                service.verifyUser("used-token-1", "012345")
        );

        assertThat(thrown).isInstanceOf(ValidationViolation.class);

        ValidationViolation violation = (ValidationViolation) thrown;

        assertThat(violation.getCode()).isEqualTo("ID.023");

    }

    private UserVerification createUserVerification(User user) {
        var verification = new UserVerification();
        verification.setUser(user);
        verification.setPin("012345");
        verification.setToken("some-token");
        verification.setExpiresAt(Calendar.getInstance());

        return verification;
    }

}