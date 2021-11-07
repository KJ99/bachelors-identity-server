package pl.kj.bachelors.identity.domain.service.registration;

import pl.kj.bachelors.identity.application.exception.ConflictHttpException;

public interface AccountRegistrationService {
    void registerAccount(String email, String username, String firstName, String lastName, String password) throws ConflictHttpException;
}
