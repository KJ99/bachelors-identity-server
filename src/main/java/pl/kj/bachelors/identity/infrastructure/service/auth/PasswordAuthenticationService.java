package pl.kj.bachelors.identity.infrastructure.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import pl.kj.bachelors.identity.domain.config.JwtConfig;
import pl.kj.bachelors.identity.domain.exception.AccountNotVerifiedException;
import pl.kj.bachelors.identity.domain.exception.WrongCredentialsException;
import pl.kj.bachelors.identity.domain.model.AuthResult;
import pl.kj.bachelors.identity.domain.model.AuthResultDetail;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.model.payload.PasswordAuthPayload;
import pl.kj.bachelors.identity.domain.model.payload.TokenAuthPayload;
import pl.kj.bachelors.identity.domain.service.auth.PasswordAuthenticator;
import pl.kj.bachelors.identity.domain.service.jwt.JwtGenerator;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;
import pl.kj.bachelors.identity.infrastructure.repository.UserVerificationRepository;

@Service
public class PasswordAuthenticationService implements PasswordAuthenticator {
    private final UserRepository userRepository;
    private final UserVerificationRepository verificationRepository;
    private final JwtGenerator jwtGenerator;
    private final JwtConfig jwtConfig;

    @Autowired
    public PasswordAuthenticationService(
            UserRepository userRepository,
            UserVerificationRepository verificationRepository,
            JwtGenerator jwtGenerator,
            JwtConfig jwtConfig) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.jwtGenerator = jwtGenerator;
        this.jwtConfig = jwtConfig;
    }

    @Override
    public AuthResult<PasswordAuthPayload> authenticate(String username, String password)
            throws WrongCredentialsException, AccountNotVerifiedException {
        User user = this.userRepository.findByUserNameOrEmail(username).orElseThrow(WrongCredentialsException::new);
        if(!BCrypt.checkpw(password, user.getPassword())) {
            throw new WrongCredentialsException();
        }
        if(!user.isVerified()) {
            throw createAccountNotVerifiedException(user);
        }

        return this.createSuccessfulResult(user);
    }

    private AuthResult<PasswordAuthPayload> createSuccessfulResult(User user) {
        TokenAuthPayload tokenPayload = new TokenAuthPayload();
        tokenPayload.setTokenType(this.jwtConfig.getType());
        tokenPayload.setAccessToken(this.jwtGenerator.generateAccessToken(user));
        tokenPayload.setRefreshToken(this.jwtGenerator.generateRefreshToken(user));

        PasswordAuthPayload payload = new PasswordAuthPayload();
        payload.setUser(user);
        payload.setToken(tokenPayload);

        AuthResult<PasswordAuthPayload> result = new AuthResult<>();
        result.setDetail(AuthResultDetail.SUCCESS);
        result.setPayload(payload);

        return result;
    }

    private AccountNotVerifiedException createAccountNotVerifiedException(User user) {
        var ex = new AccountNotVerifiedException();
        ex.setLatestVerification(this.verificationRepository.findLatestByUserId(user.getUid()).orElse(null));

        return ex;
    }
}
