package pl.kj.bachelors.identity.infrastructure.service.registration;

import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kj.bachelors.identity.domain.config.ApiConfig;
import pl.kj.bachelors.identity.domain.config.VerificationConfig;
import pl.kj.bachelors.identity.domain.exception.ValidationViolation;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.model.entity.UserVerification;
import pl.kj.bachelors.identity.domain.service.registration.AccountVerifier;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;
import pl.kj.bachelors.identity.infrastructure.repository.UserVerificationRepository;
import pl.kj.bachelors.identity.infrastructure.service.TokenGenerationService;

import java.util.Calendar;
import java.util.concurrent.ExecutionException;

@Service
public class AccountVerificationService implements AccountVerifier {
    private final UserRepository userRepository;
    private final UserVerificationRepository verificationRepository;
    private final TokenGenerationService tokenGenerator;
    private final VerificationConfig verificationConfig;
    private final ApiConfig apiConfig;

    @Autowired
    public AccountVerificationService(
            UserRepository userRepository,
            UserVerificationRepository verificationRepository,
            TokenGenerationService tokenGenerator,
            VerificationConfig verificationConfig,
            ApiConfig apiConfig) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.tokenGenerator = tokenGenerator;
        this.verificationConfig = verificationConfig;
        this.apiConfig = apiConfig;
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
        verification.setPin(this.tokenGenerator.generateNumericToken(6));
        verification.setExpiresAt(getExpiresAt());

        return verification;
    }

    @Override
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

    @Override
    @Transactional(rollbackFor = { NotFoundException.class, ValidationViolation.class })
    public void verifyUser(final String token, final String pin) throws NotFoundException, ValidationViolation {
        UserVerification verification = this.verificationRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if(verification.getUser().isVerified()) {
            throw new ValidationViolation(this.apiConfig.getErrors().get("ID.009"), "ID.009", null);
        }
        if(verification.getExpiresAt().getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
            throw new ValidationViolation(this.apiConfig.getErrors().get("ID.008"), "ID.008", "pin");
        }
        if(!verification.getPin().equals(pin)) {
            throw new ValidationViolation(this.apiConfig.getErrors().get("ID.007"), "ID.007", "pin");
        }

        User user = verification.getUser();
        user.setVerified(true);
        user.setActive(true);

        this.userRepository.save(user);
        this.verificationRepository.delete(verification);
    }
}
