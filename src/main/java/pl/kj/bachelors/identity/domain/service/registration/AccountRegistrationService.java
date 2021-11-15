package pl.kj.bachelors.identity.domain.service.registration;

import org.springframework.dao.DataIntegrityViolationException;
import pl.kj.bachelors.identity.domain.model.entity.UserVerification;

import java.util.concurrent.ExecutionException;

public interface AccountRegistrationService {
    UserVerification registerAccount(String email, String username, String firstName, String lastName, String password)
            throws DataIntegrityViolationException, ExecutionException, InterruptedException;
}
