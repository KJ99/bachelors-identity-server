package pl.kj.bachelors.identity.domain.service.auth;

import pl.kj.bachelors.identity.domain.exception.ValidationViolation;
import pl.kj.bachelors.identity.domain.exception.WrongCredentialsException;
import pl.kj.bachelors.identity.domain.model.entity.User;

public interface PasswordUpdateService {
    void updatePassword(User user, String currentPassword, String newPassword) throws ValidationViolation;
}
