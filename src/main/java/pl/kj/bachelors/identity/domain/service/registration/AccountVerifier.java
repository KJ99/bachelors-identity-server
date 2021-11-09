package pl.kj.bachelors.identity.domain.service.registration;

import javassist.NotFoundException;
import pl.kj.bachelors.identity.domain.exception.ValidationViolation;
import pl.kj.bachelors.identity.domain.model.User;
import pl.kj.bachelors.identity.domain.model.UserVerification;

import java.util.concurrent.ExecutionException;

public interface AccountVerifier {
    UserVerification createVerification(User user) throws ExecutionException, InterruptedException;
    UserVerification createVerification(String email) throws NotFoundException, ExecutionException, InterruptedException;
    void saveVerification(UserVerification verification);
    void verifyUser(String token, String pin) throws NotFoundException, ValidationViolation;
}
