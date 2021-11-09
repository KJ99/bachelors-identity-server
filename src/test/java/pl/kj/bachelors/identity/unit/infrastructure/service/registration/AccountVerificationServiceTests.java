package pl.kj.bachelors.identity.unit.infrastructure.service.registration;

import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pl.kj.bachelors.identity.application.Application;
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

@SpringBootTest
@ContextConfiguration(classes = { Application.class })
class AccountVerificationServiceTests {
    private final String correctPin = "123456";
    private final String activeToken = "activeToken";
    private final String usedToken = "used-token";
    private final String expiredToken = "expiredToken";

    @Autowired
    private AccountVerificationService service;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserVerificationRepository verificationRepository;

    @BeforeEach
    public void setUp() {
        var notVerifiedUser = createUser("uid-1", "1@testuser.testuser");
        var verifiedUser = createUser("uid-2", "2@testuser.testuser");
        verifiedUser.setVerified(true);
        verifiedUser.setActive(true);
        var activeVerification = this.createUserVerification(notVerifiedUser, this.activeToken);
        var expiredVerification = this.createUserVerification(notVerifiedUser, this.expiredToken);
        var usedVerification = this.createUserVerification(verifiedUser, this.usedToken);
        activeVerification.getExpiresAt().add(Calendar.HOUR, 10);
        expiredVerification.getExpiresAt().add(Calendar.HOUR, -10);

        this.userRepository.saveAndFlush(notVerifiedUser);
        this.userRepository.saveAndFlush(verifiedUser);
        this.verificationRepository.saveAndFlush(activeVerification);
        this.verificationRepository.saveAndFlush(expiredVerification);
        this.verificationRepository.saveAndFlush(usedVerification);
    }

    @Test
    public void testCreateVerification_ByUser() throws ExecutionException, InterruptedException {
        User user = createUser("uid-3", "email@email.pl");

        UserVerification verification = this.service.createVerification(user);
        this.checkVerification(verification, user.getUid());
    }

    @Test
    public void testCreateVerification_ByEmail() throws NotFoundException, ExecutionException, InterruptedException {
        UserVerification verification = this.service.createVerification("1@testuser.testuser");
        this.checkVerification(verification, "uid-1");
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
        String uid = this.verificationRepository.findByToken(this.activeToken).get().getUser().getUid();

        this.service.verifyUser(this.activeToken, this.correctPin);

        User user = this.userRepository.findById(uid).get();
        Optional<UserVerification> verification = this.verificationRepository.findByToken(this.activeToken);

        assertThat(user.isVerified()).isTrue();
        assertThat(user.isActive()).isTrue();
        assertThat(verification.isPresent()).isFalse();
    }

    @Test
    public void testVerifyUser_IncorrectToken() {
        assertThatThrownBy(() ->
                service.verifyUser("fake-token", this.correctPin)
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testSaveVerification() {
        final String token = "some-token";
        User user = this.userRepository.findById("uid-1").get();
        UserVerification verification = this.createUserVerification(user, token);

        this.service.saveVerification(verification);

        assertThat(this.verificationRepository.findByToken(token).isPresent()).isTrue();
    }

    @Test
    public void testVerifyUser_PinNotMatch() {
        Throwable thrown = catchThrowable(() ->
                service.verifyUser(this.activeToken, "fake-pin")
        );

        assertThat(thrown).isInstanceOf(ValidationViolation.class);

        ValidationViolation violation = (ValidationViolation) thrown;

        assertThat(violation.getCode()).isEqualTo("ID.021");
        assertThat(violation.getPath()).isEqualTo("pin");
    }

    @Test
    public void testVerifyUser_PinExpired() {
        Throwable thrown = catchThrowable(() ->
                service.verifyUser(this.expiredToken, this.correctPin)
        );

        assertThat(thrown).isInstanceOf(ValidationViolation.class);

        ValidationViolation violation = (ValidationViolation) thrown;

        assertThat(violation.getCode()).isEqualTo("ID.022");
        assertThat(violation.getPath()).isEqualTo("pin");

    }

    @Test
    public void testVerifyUser_AccountAlreadyVerified() {
        Throwable thrown = catchThrowable(() ->
                service.verifyUser(this.usedToken, this.correctPin)
        );

        assertThat(thrown).isInstanceOf(ValidationViolation.class);

        ValidationViolation violation = (ValidationViolation) thrown;

        assertThat(violation.getCode()).isEqualTo("ID.023");

    }

    private User createUser(String uid, String email) {
        var user = new User();
        user.setUid(uid);
        user.setEmail(email);
        user.setUserName(email);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("pass");
        user.setSalt(email);

        return user;
    }

    private UserVerification createUserVerification(User user, String token) {
        var verification = new UserVerification();
        verification.setUser(user);
        verification.setPin(this.correctPin);
        verification.setToken(token);
        verification.setExpiresAt(Calendar.getInstance());

        return verification;
    }
}