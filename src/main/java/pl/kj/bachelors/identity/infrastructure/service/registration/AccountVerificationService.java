package pl.kj.bachelors.identity.infrastructure.service.registration;

import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kj.bachelors.identity.domain.exception.ValidationViolation;
import pl.kj.bachelors.identity.domain.config.VerificationConfig;
import pl.kj.bachelors.identity.domain.model.User;
import pl.kj.bachelors.identity.domain.model.UserVerification;
import pl.kj.bachelors.identity.domain.service.registration.AccountVerifier;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;
import pl.kj.bachelors.identity.infrastructure.repository.UserVerificationRepository;
import pl.kj.bachelors.identity.infrastructure.service.TokenGenerationService;

import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.ExecutionException;

@Service
public class AccountVerificationService implements AccountVerifier {
    private final UserRepository userRepository;
    private final UserVerificationRepository verificationRepository;
    private final TokenGenerationService tokenGenerator;
    private final VerificationConfig verificationConfig;

    public AccountVerificationService(
            @Autowired UserRepository userRepository,
            @Autowired UserVerificationRepository verificationRepository,
            @Autowired TokenGenerationService tokenGenerator,
            @Autowired VerificationConfig verificationConfig) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.tokenGenerator = tokenGenerator;
        this.verificationConfig = verificationConfig;
    }

    @Override
    public UserVerification createVerification(final String email) throws NotFoundException, ExecutionException, InterruptedException {
        User user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return this.createVerification(user);
    }

    @Override
    public UserVerification createVerification(final User user) throws ExecutionException, InterruptedException {
        UserVerification verification = new UserVerification();
        verification.setUser(user);
        verification.setToken(getToken());
        verification.setPin(generatePin());
        verification.setExpiresAt(getExpiresAt());

        return verification;
    }

    @Override
    @Transactional
    public void saveVerification(UserVerification verification) {
        this.verificationRepository.save(verification);
    }

    private String getToken() throws ExecutionException, InterruptedException {
        return this.tokenGenerator.generateToken(
                verificationConfig.getTokenPrefix(),
                verificationConfig.getTokenSuffix(),
                verificationConfig.getContentLength()
        );
    }

    private Calendar getExpiresAt() {
        Calendar expiresAt = Calendar.getInstance();
        expiresAt.add(Calendar.MINUTE, verificationConfig.getExpiresInMinutes());

        return expiresAt;
    }

    private String generatePin() {
        Random random = new Random();
        String[] pinElements = new String[6];
        for(int i = 0; i < pinElements.length; i++) {
            pinElements[i] = String.valueOf(random.nextInt(9));
        }

        return String.join("", pinElements);
    }

    @Override
    @Transactional(rollbackFor = { NotFoundException.class, ValidationViolation.class })
    public void verifyUser(final String token, final String pin) throws NotFoundException, ValidationViolation {
        UserVerification verification = this.verificationRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if(verification.getUser().isVerified()) {
            throw new ValidationViolation("User is already verified", "ID.023", null);
        }
        if(verification.getExpiresAt().getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
            throw new ValidationViolation("PIN has been expired", "ID.022", "pin");
        }
        if(!verification.getPin().equals(pin)) {
            throw new ValidationViolation("Invalid verification PIN", "ID.021", "pin");
        }

        User user = verification.getUser();
        user.setVerified(true);
        user.setActive(true);

        this.userRepository.save(user);
        this.verificationRepository.delete(verification);
    }
}