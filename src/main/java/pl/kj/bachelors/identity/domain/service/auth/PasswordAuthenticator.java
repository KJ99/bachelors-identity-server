package pl.kj.bachelors.identity.domain.service.auth;

import pl.kj.bachelors.identity.domain.exception.AccountNotVerifiedException;
import pl.kj.bachelors.identity.domain.exception.WrongCredentialsException;
import pl.kj.bachelors.identity.domain.model.AuthResult;
import pl.kj.bachelors.identity.domain.model.payload.PasswordAuthPayload;

public interface PasswordAuthenticator {
    AuthResult<PasswordAuthPayload> authenticate(String username, String password) throws WrongCredentialsException, AccountNotVerifiedException;
}
