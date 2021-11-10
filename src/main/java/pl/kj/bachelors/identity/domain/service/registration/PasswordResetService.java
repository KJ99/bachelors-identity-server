package pl.kj.bachelors.identity.domain.service.registration;

import javassist.NotFoundException;
import pl.kj.bachelors.identity.domain.exception.AccessDeniedException;
import pl.kj.bachelors.identity.domain.exception.ValidationViolation;
import pl.kj.bachelors.identity.domain.model.entity.PasswordReset;

import java.util.concurrent.ExecutionException;

public interface PasswordResetService {
    PasswordReset createPasswordReset(String email) throws NotFoundException, ExecutionException, InterruptedException;
    void resetPassword(String token, String pin, String newPassword) throws NotFoundException, AccessDeniedException, ValidationViolation;
}
