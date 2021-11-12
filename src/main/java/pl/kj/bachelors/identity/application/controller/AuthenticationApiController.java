package pl.kj.bachelors.identity.application.controller;

import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kj.bachelors.identity.application.dto.request.LoginRequest;
import pl.kj.bachelors.identity.application.dto.request.PasswordResetInitRequest;
import pl.kj.bachelors.identity.application.dto.request.PasswordResetRequest;
import pl.kj.bachelors.identity.application.dto.response.LoginResponse;
import pl.kj.bachelors.identity.application.dto.response.PasswordResetInitResponse;
import pl.kj.bachelors.identity.application.dto.response.TokenResponse;
import pl.kj.bachelors.identity.application.exception.BadRequestHttpException;
import pl.kj.bachelors.identity.application.exception.ForbiddenHttpException;
import pl.kj.bachelors.identity.application.exception.NotAuthorizedHttpException;
import pl.kj.bachelors.identity.domain.config.ApiConfig;
import pl.kj.bachelors.identity.domain.exception.AccessDeniedException;
import pl.kj.bachelors.identity.domain.exception.AccountNotVerifiedException;
import pl.kj.bachelors.identity.domain.exception.ValidationViolation;
import pl.kj.bachelors.identity.domain.exception.WrongCredentialsException;
import pl.kj.bachelors.identity.domain.model.AuthResult;
import pl.kj.bachelors.identity.domain.model.AuthResultDetail;
import pl.kj.bachelors.identity.domain.model.entity.PasswordReset;
import pl.kj.bachelors.identity.domain.model.payload.PasswordAuthPayload;
import pl.kj.bachelors.identity.domain.model.payload.TokenAuthPayload;
import pl.kj.bachelors.identity.domain.service.ModelValidator;
import pl.kj.bachelors.identity.domain.service.auth.PasswordAuthenticator;
import pl.kj.bachelors.identity.domain.service.jwt.AccessTokenFetcher;
import pl.kj.bachelors.identity.domain.service.jwt.RefreshTokenManager;
import pl.kj.bachelors.identity.domain.service.jwt.TokenRefresher;
import pl.kj.bachelors.identity.domain.service.mail.MailSender;
import pl.kj.bachelors.identity.domain.service.registration.PasswordResetService;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/v1/auth")
public class AuthenticationApiController extends BaseApiController {
    private final TokenRefresher tokenRefresher;
    private final RefreshTokenManager refreshTokenManager;
    private final AccessTokenFetcher accessTokenFetcher;
    private final PasswordAuthenticator passwordAuthenticator;
    private final PasswordResetService passwordResetService;
    private final MailSender mailer;

    AuthenticationApiController(
            @Autowired TokenRefresher tokenRefresher,
            @Autowired RefreshTokenManager refreshTokenManager,
            @Autowired AccessTokenFetcher accessTokenFetcher,
            @Autowired PasswordAuthenticator passwordAuthenticator,
            @Autowired PasswordResetService passwordResetService,
            @Autowired MailSender mailer
    ) {
        this.tokenRefresher = tokenRefresher;
        this.refreshTokenManager = refreshTokenManager;
        this.accessTokenFetcher = accessTokenFetcher;
        this.passwordAuthenticator = passwordAuthenticator;
        this.passwordResetService = passwordResetService;
        this.mailer = mailer;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletResponse response)
            throws AccountNotVerifiedException, WrongCredentialsException {
        AuthResult<PasswordAuthPayload> result = this.passwordAuthenticator.authenticate(
                request.getUsername(),
                request.getPassword()
        );
        String refreshToken = result.getPayload().getToken().getRefreshToken();
        this.refreshTokenManager.putInResponse(refreshToken, response);

        return ResponseEntity.ok(this.map(result.getPayload(), LoginResponse.class));
    }

    @PutMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(HttpServletRequest request, HttpServletResponse response)
            throws ForbiddenHttpException, NotAuthorizedHttpException {
        String access = this.accessTokenFetcher.getAccessTokenFromRequest(request)
                .orElseThrow(NotAuthorizedHttpException::new);
        String refresh = this.refreshTokenManager.getFromRequest(request)
                .orElseThrow(NotAuthorizedHttpException::new);

        AuthResult<TokenAuthPayload> result = this.tokenRefresher.refreshToken(access, refresh);
        if(result.getDetail().equals(AuthResultDetail.INVALID_TOKEN)) {
            throw new ForbiddenHttpException();
        }

        this.refreshTokenManager.putInResponse(result.getPayload().getRefreshToken(), response);

        return ResponseEntity.accepted().body(this.map(result.getPayload(), TokenResponse.class));
    }

    @PostMapping("/password-reset/init")
    public ResponseEntity<PasswordResetInitResponse> initPasswordReset(@RequestBody PasswordResetInitRequest request)
            throws BadRequestHttpException, NotFoundException, ExecutionException, InterruptedException {
        this.ensureThatModelIsValid(request);
        PasswordReset passwordReset = this.passwordResetService.createPasswordReset(request.getEmail());
        this.mailer.sendPasswordResetEmail(passwordReset);

        return ResponseEntity.accepted().body(this.map(passwordReset, PasswordResetInitResponse.class));
    }

    @PostMapping("/password-reset")
    public ResponseEntity<?> initPasswordReset(@RequestBody PasswordResetRequest request)
            throws BadRequestHttpException, AccessDeniedException, ValidationViolation, NotFoundException {
        this.ensureThatModelIsValid(request);
        this.passwordResetService.resetPassword(request.getToken(), request.getPin(), request.getPassword());

        return ResponseEntity.noContent().build();
    }
}
