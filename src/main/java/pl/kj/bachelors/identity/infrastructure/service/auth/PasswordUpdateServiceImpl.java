package pl.kj.bachelors.identity.infrastructure.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kj.bachelors.identity.domain.config.ApiConfig;
import pl.kj.bachelors.identity.domain.config.PasswordConfig;
import pl.kj.bachelors.identity.domain.exception.ValidationViolation;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.service.auth.PasswordUpdateService;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;

@Service
public class PasswordUpdateServiceImpl implements PasswordUpdateService {
    private final ApiConfig apiConfig;
    private final PasswordConfig passwordConfig;
    private final UserRepository userRepository;

    @Autowired
    public PasswordUpdateServiceImpl(ApiConfig apiConfig, PasswordConfig passwordConfig, UserRepository userRepository) {
        this.apiConfig = apiConfig;
        this.passwordConfig = passwordConfig;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(rollbackFor = ValidationViolation.class)
    public void updatePassword(User user, String currentPassword, String newPassword) throws ValidationViolation {
        if(!BCrypt.checkpw(currentPassword, user.getPassword())) {
            String errorCode = "ID.201";
            throw new ValidationViolation(apiConfig.getErrors().get(errorCode), "ID.201", "current_password");
        }
        final String salt =  BCrypt.gensalt(this.passwordConfig.getSaltRounds());
        final String hash = BCrypt.hashpw(newPassword, salt);
        user.setPassword(hash);
        user.setSalt(salt);

        this.userRepository.save(user);
    }
}
