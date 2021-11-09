package pl.kj.bachelors.identity.application.controller;

import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.kj.bachelors.identity.application.dto.request.AccountVerificationRequest;
import pl.kj.bachelors.identity.application.dto.request.RegistrationRequest;
import pl.kj.bachelors.identity.application.dto.request.VerificationResendRequest;
import pl.kj.bachelors.identity.application.dto.response.UserVerificationResponse;
import pl.kj.bachelors.identity.application.exception.BadRequestHttpException;
import pl.kj.bachelors.identity.application.exception.ConflictHttpException;
import pl.kj.bachelors.identity.domain.exception.ValidationViolation;
import pl.kj.bachelors.identity.domain.config.ApiConfig;
import pl.kj.bachelors.identity.domain.model.UserVerification;
import pl.kj.bachelors.identity.domain.service.ModelValidator;
import pl.kj.bachelors.identity.domain.service.mail.MailSender;
import pl.kj.bachelors.identity.domain.service.registration.AccountRegistrationService;
import pl.kj.bachelors.identity.domain.service.registration.AccountVerifier;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/v1/account")
public class AccountApiController extends BaseApiController {
    private final AccountRegistrationService service;
    private final MailSender mailer;
    private final AccountVerifier verificationService;
    AccountApiController(
            @Autowired ModelMapper mapper,
            @Value("spring.profiles.active") String activeProfile,
            @Autowired ModelValidator validator,
            @Autowired ApiConfig apiConfig,
            @Autowired AccountRegistrationService service,
            @Autowired MailSender mailer,
            @Autowired AccountVerifier verificationService) {
        super(mapper, activeProfile, validator, apiConfig);
        this.service = service;
        this.mailer = mailer;
        this.verificationService = verificationService;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<UserVerificationResponse> register(@RequestBody RegistrationRequest request)
            throws BadRequestHttpException, ConflictHttpException, ExecutionException, InterruptedException {
        this.ensureThatModelIsValid(request);
        UserVerification verification = this.service.registerAccount(
                request.getEmail(),
                request.getUsername(),
                request.getFirstName(),
                request.getLastName(),
                request.getPassword()
        );

        mailer.sendVerificationEmail(verification);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.map(verification, UserVerificationResponse.class));
    }

    @PostMapping("/verification/resend")
    public ResponseEntity<UserVerificationResponse> resendVerification(@RequestBody VerificationResendRequest request)
            throws NotFoundException, ExecutionException, InterruptedException {
        UserVerification verification = this.verificationService.createVerification(request.getEmail());
        this.verificationService.saveVerification(verification);
        this.mailer.sendVerificationEmail(verification);

        return ResponseEntity.ok(this.map(verification, UserVerificationResponse.class));
    }

    @PutMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody AccountVerificationRequest request)
            throws ValidationViolation, NotFoundException {
        this.verificationService.verifyUser(request.getToken(), request.getPin());
        return ResponseEntity.noContent().build();
    }
}
