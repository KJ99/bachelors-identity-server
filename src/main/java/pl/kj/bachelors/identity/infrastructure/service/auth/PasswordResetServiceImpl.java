package pl.kj.bachelors.identity.infrastructure.service.auth;

import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kj.bachelors.identity.domain.config.PasswordConfig;
import pl.kj.bachelors.identity.domain.config.PasswordResetConfig;
import pl.kj.bachelors.identity.domain.exception.AccessDeniedException;
import pl.kj.bachelors.identity.domain.exception.ValidationViolation;
import pl.kj.bachelors.identity.domain.model.entity.PasswordReset;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.service.registration.PasswordResetService;
import pl.kj.bachelors.identity.infrastructure.repository.PasswordResetRepository;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;
import pl.kj.bachelors.identity.infrastructure.service.TokenGenerationService;

import java.util.Calendar;
import java.util.concurrent.ExecutionException;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {
    private final PasswordResetConfig config;
    private final TokenGenerationService tokenGenerator;
    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordConfig passwordConfig;

    @Autowired
    public PasswordResetServiceImpl(
            PasswordResetConfig config,
            TokenGenerationService tokenGenerator,
            UserRepository userRepository,
            PasswordResetRepository passwordResetRepository,
            PasswordConfig passwordConfig) {
        this.config = config;
        this.tokenGenerator = tokenGenerator;
        this.userRepository = userRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.passwordConfig = passwordConfig;
    }

    @Override
    @Transactional
    public PasswordReset createPasswordReset(String email) throws NotFoundException, ExecutionException, InterruptedException {
        User user = this.userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
        Calendar expiresAt = Calendar.getInstance();
        expiresAt.add(Calendar.MINUTE, config.getExpiresInMinutes());
        PasswordReset reset = new PasswordReset();
        reset.setToken(
                this.tokenGenerator.generateToken(
                        this.config.getTokenPrefix(),
                        this.config.getTokenSuffix(),
                        this.config.getContentLength()
                )
        );
        reset.setPin(this.tokenGenerator.generateNumericToken(this.config.getPinLength()));
        reset.setExpiresAt(expiresAt);
        reset.setUser(user);

        this.passwordResetRepository.save(reset);

        return reset;
    }

    @Override
    @Transactional
    public void resetPassword(String token, String pin, String newPassword) throws NotFoundException, AccessDeniedException, ValidationViolation {
        var passwordReset = this.passwordResetRepository
                .findByToken(token)
                .orElseThrow(() -> new NotFoundException("Token not found"));
        if(passwordReset.getExpiresAt().getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
            throw new AccessDeniedException("Token expired");
        }
        if(!pin.equals(passwordReset.getPin())) {
            throw new ValidationViolation("", "ID.042", "pin");
        }
        var user = passwordReset.getUser();

        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt(this.passwordConfig.getSaltRounds())));

        this.userRepository.save(user);
        this.passwordResetRepository.delete(passwordReset);
    }
}
