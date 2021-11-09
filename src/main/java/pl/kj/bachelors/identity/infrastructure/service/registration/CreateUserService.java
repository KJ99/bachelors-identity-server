package pl.kj.bachelors.identity.infrastructure.service.registration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import pl.kj.bachelors.identity.domain.config.PasswordConfig;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.service.registration.UserCreator;

import java.util.UUID;

@Service
public class CreateUserService implements UserCreator {
    private final PasswordConfig config;

    public CreateUserService(@Autowired PasswordConfig config) {
        this.config = config;
    }

    @Override
    public User createUser(
            final String email,
            final String username,
            final String firstName,
            final String lastName,
            final String password
    ) {
        String salt = BCrypt.gensalt(config.getSaltRounds());

        var user = new User();
        user.setUid(UUID.randomUUID().toString());
        user.setEmail(email);
        user.setUserName(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setSalt(salt);
        user.setPassword(BCrypt.hashpw(password, salt));

        return user;
    }
}
