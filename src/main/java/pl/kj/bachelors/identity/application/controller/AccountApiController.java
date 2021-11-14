package pl.kj.bachelors.identity.application.controller;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.kj.bachelors.identity.application.dto.request.AccountVerificationRequest;
import pl.kj.bachelors.identity.application.dto.request.AvailabilityRequest;
import pl.kj.bachelors.identity.application.dto.request.RegistrationRequest;
import pl.kj.bachelors.identity.application.dto.request.VerificationResendRequest;
import pl.kj.bachelors.identity.application.dto.response.AvailabilityResponse;
import pl.kj.bachelors.identity.application.dto.response.PasswordResetInitResponse;
import pl.kj.bachelors.identity.application.dto.response.UserVerificationResponse;
import pl.kj.bachelors.identity.application.dto.response.error.ValidationErrorResponse;
import pl.kj.bachelors.identity.application.exception.BadRequestHttpException;
import pl.kj.bachelors.identity.application.exception.ConflictHttpException;
import pl.kj.bachelors.identity.domain.exception.ValidationViolation;
import pl.kj.bachelors.identity.domain.model.entity.UserVerification;
import pl.kj.bachelors.identity.domain.service.mail.MailSender;
import pl.kj.bachelors.identity.domain.service.registration.AccountRegistrationService;
import pl.kj.bachelors.identity.domain.service.registration.AccountVerifier;
import pl.kj.bachelors.identity.domain.service.registration.AvailabilityChecker;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/v1/account")
@Tag(name = "Account")
public class AccountApiController extends BaseApiController {
    private final AccountRegistrationService service;
    private final MailSender mailer;
    private final AccountVerifier verificationService;
    private final AvailabilityChecker availabilityChecker;
    AccountApiController(
            @Autowired AccountRegistrationService service,
            @Autowired MailSender mailer,
            @Autowired AccountVerifier verificationService,
            @Autowired AvailabilityChecker availabilityChecker) {
        this.service = service;
        this.mailer = mailer;
        this.verificationService = verificationService;
        this.availabilityChecker = availabilityChecker;
    }

    @PostMapping
    @Transactional
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserVerificationResponse.class)
                    ),
                    description = "User verification token"),
            @ApiResponse(responseCode = "400", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = ValidationErrorResponse.class)))
            )
    })
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

        this.logger.info(
                String.format("Successfully created an account with UID %s from address %s",
                        verification.getUser().getUid(),
                        this.currentRequest.getRemoteAddr()
                )
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.map(verification, UserVerificationResponse.class));
    }

    @PostMapping("/verification/resend")
    @Transactional
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserVerificationResponse.class)
                    ),
                    description = "User verification token"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<UserVerificationResponse> resendVerification(@RequestBody VerificationResendRequest request)
            throws NotFoundException, ExecutionException, InterruptedException {
        UserVerification verification = this.verificationService.createVerification(request.getEmail());
        this.verificationService.saveVerification(verification);
        this.mailer.sendVerificationEmail(verification);

        return ResponseEntity.ok(this.map(verification, UserVerificationResponse.class));
    }

    @PutMapping("/verify")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    content = @Content(schema = @Schema(hidden = true)),
                    description = "User verified successfully"),
            @ApiResponse(responseCode = "400", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = ValidationErrorResponse.class)))
            )
    })
    public ResponseEntity<?> verify(@RequestBody AccountVerificationRequest request)
            throws ValidationViolation, NotFoundException {
        this.verificationService.verifyUser(request.getToken(), request.getPin());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/availability")
    @ApiResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AvailabilityResponse.class)
            ),
            description = "Availability result"
    )
    public ResponseEntity<AvailabilityResponse> getAvailability(AvailabilityRequest request) {
        boolean result = this.availabilityChecker.isAvailable(request.getField(), request.getValue());
        AvailabilityResponse response = new AvailabilityResponse();
        response.setAvailable(result);
        return ResponseEntity.ok(response);
    }
}
