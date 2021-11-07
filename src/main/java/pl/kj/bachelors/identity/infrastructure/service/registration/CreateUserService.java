package pl.kj.bachelors.identity.infrastructure.service.registration;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import pl.kj.bachelors.identity.domain.model.User;
import pl.kj.bachelors.identity.domain.service.registration.UserCreator;

import java.util.UUID;

@Service
public class CreateUserService implements UserCreator {

    @Override
    public User createUser(
            final String email,
            final String username,
            final String firstName,
            final String lastName,
            final String password
    ) {
        String salt = BCrypt.gensalt(16);

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
