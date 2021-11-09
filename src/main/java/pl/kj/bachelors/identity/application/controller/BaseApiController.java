package pl.kj.bachelors.identity.application.controller;

import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.kj.bachelors.identity.application.dto.response.error.GenericErrorResponse;
import pl.kj.bachelors.identity.application.dto.response.error.ValidationErrorResponse;
import pl.kj.bachelors.identity.application.exception.*;
import pl.kj.bachelors.identity.domain.exception.ValidationViolation;
import pl.kj.bachelors.identity.domain.config.ApiConfig;
import pl.kj.bachelors.identity.domain.service.ModelValidator;

import java.nio.file.NoSuchFileException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

abstract class BaseApiController {
    protected final ModelMapper mapper;
    protected final String activeProfile;
    protected final ModelValidator validator;
    protected final ApiConfig apiConfig;

    BaseApiController(
            @Autowired ModelMapper mapper,
            @Value("spring.profiles.active") String activeProfile,
            @Autowired ModelValidator validator,
            ApiConfig apiConfig) {
        this.mapper = mapper;
        this.activeProfile = activeProfile;
        this.validator = validator;
        this.apiConfig = apiConfig;
    }

    protected  <T> T map(Object source, Class<T> destinationType) {
        return this.mapper.map(source, destinationType);
    }

    protected  <T, K> Collection<T> mapCollection(Collection<K> source, Class<T> destinationType) {
        return source
                .stream()
                .map(item -> this.map(item, destinationType))
                .collect(Collectors.toList());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = { NotFoundHttpException.class, NoSuchFileException.class, NotFoundException.class})
    protected ResponseEntity<Object> handleNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictHttpException.class)
    protected ResponseEntity<ValidationViolation> handleConflict(ConflictHttpException ex) {
        final var bodyBuilder = ResponseEntity.status(HttpStatus.CONFLICT);

        return ex.getError() != null ? bodyBuilder.body(ex.getError()) : bodyBuilder.build();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<ValidationErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex
    ) {
        final var bodyBuilder = ResponseEntity.status(HttpStatus.CONFLICT);
        final ValidationViolation violation = this.processDataIntegrityViolation(ex);
        return violation != null
                ? bodyBuilder.body(this.map(violation, ValidationErrorResponse.class))
                : bodyBuilder.build();
    }



    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NotAuthorizedHttpException.class)
    protected ResponseEntity<Object> handleNotAuthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestHttpException.class)
    protected ResponseEntity<Collection<ValidationErrorResponse>> handleBadRequest(BadRequestHttpException ex) {
        return ResponseEntity.badRequest().body(this.mapCollection(ex.getErrors(), ValidationErrorResponse.class));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationViolation.class)
    protected ResponseEntity<ValidationErrorResponse> handleRequestViolation(ValidationViolation ex) {
        return ResponseEntity.badRequest().body(this.map(ex, ValidationErrorResponse.class));
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ForbiddenHttpException.class)
    protected ResponseEntity<GenericErrorResponse> handleForbidden(ForbiddenHttpException ex) {
        var resp = this.map(ex, GenericErrorResponse.class);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
    }

    private boolean checkProfile(String profile) {
        return this.activeProfile.equals(profile);
    }

    protected boolean isDevelopment() {
        return this.checkProfile("dev");
    }

    protected boolean isTesting() {
        return this.checkProfile("test");
    }

    protected boolean isProduction() {
        return this.checkProfile("prod");
    }

    protected <T> void ensureThatModelIsValid(T model) throws BadRequestHttpException {
        var violations = this.validator.validateModel(model);
        if(violations.size() > 0) {
            throw new BadRequestHttpException(violations);
        }
    }

    private ValidationViolation processDataIntegrityViolation(DataIntegrityViolationException source) {
        String specificMessage = source.getMostSpecificCause().getMessage();

        String code = null;
        String path = null;
        if (this.isMessageContaining(specificMessage, "UN_EMAIL")) {
            code = "ID.012";
            path = "email";
        } else if (this.isMessageContaining(specificMessage, "UN_USERNAME")) {
            code = "ID.011";
            path = "username";
        }

        String message = this.apiConfig.getErrors().get(code);

        return path != null ? new ValidationViolation(message, code, path) : null;
    }

    private boolean isMessageContaining(String message, String substring) {
        Pattern pattern = Pattern.compile(substring, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(message);

        return matcher.find();
    }
}
