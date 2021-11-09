package pl.kj.bachelors.identity.domain.service.registration;

import pl.kj.bachelors.identity.application.exception.ConflictHttpException;
import pl.kj.bachelors.identity.domain.model.UserVerification;

import java.util.concurrent.ExecutionException;

public interface AccountRegistrationService {
    UserVerification registerAccount(String email, String username, String firstName, String lastName, String password) throws ConflictHttpException, ExecutionException, InterruptedException;
}
