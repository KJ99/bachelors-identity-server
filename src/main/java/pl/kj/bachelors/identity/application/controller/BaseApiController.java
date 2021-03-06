package pl.kj.bachelors.identity.application.controller;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.kj.bachelors.identity.application.dto.response.UserVerificationResponse;
import pl.kj.bachelors.identity.application.dto.response.error.GenericErrorResponse;
import pl.kj.bachelors.identity.application.dto.response.error.ValidationErrorResponse;
import pl.kj.bachelors.identity.domain.config.ApiConfig;
import pl.kj.bachelors.identity.domain.exception.*;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.security.action.Action;
import pl.kj.bachelors.identity.domain.security.voter.Voter;
import pl.kj.bachelors.identity.domain.service.ModelValidator;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

abstract class BaseApiController {
    @Autowired
    protected ModelMapper mapper;
    @Autowired
    protected ModelValidator validator;
    @Autowired
    protected ApiConfig apiConfig;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected HttpServletRequest currentRequest;
    @Autowired(required = false)
    @SuppressWarnings("rawtypes")
    protected Set<Voter> voters;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

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
    @ExceptionHandler(value = { ResourceNotFoundException.class, NoSuchFileException.class, NotFoundException.class})
    protected ResponseEntity<Object> handleNotFound() {
        return ResponseEntity.notFound().build();
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
    @ExceptionHandler(CredentialsNotFoundException.class)
    protected ResponseEntity<Object> handleNotAuthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AggregatedApiError.class)
    protected ResponseEntity<Collection<ValidationErrorResponse>> handleAggregatedApiError(AggregatedApiError ex) {
        return ResponseEntity.badRequest().body(this.mapCollection(ex.getErrors(), ValidationErrorResponse.class));
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationViolation.class)
    protected ResponseEntity<ValidationErrorResponse> handleRequestViolation(ValidationViolation ex) {
        return ResponseEntity.badRequest().body(this.map(ex, ValidationErrorResponse.class));
    }

    @ExceptionHandler(value = {WrongCredentialsException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<?> handleWrongCredentials(WrongCredentialsException ex) {
        return ResponseEntity.badRequest().build();
    }


    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<?> handleAccessDenied() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = { MalformedJwtException.class, JwtInvalidException.class })
    protected ResponseEntity<?> handleJwtReject() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = { ExpiredJwtException.class })
    protected ResponseEntity<GenericErrorResponse<?>> handleJwtExpired() {
        var response = new GenericErrorResponse<>();
        response.setDetailCode("ID.101");
        response.setDetailMessage(this.apiConfig.getErrors().get("ID.101"));

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = {AccountNotVerifiedException.class})
    protected ResponseEntity<?> handleAccountNotVerified(AccountNotVerifiedException ex) {
        String detailMessage = this.apiConfig.getErrors().get(ex.getCode());
        var res = new GenericErrorResponse<UserVerificationResponse>();
        res.setDetailCode(ex.getCode());
        res.setDetailMessage(detailMessage);
        if(ex.getLatestVerification() != null) {
            res.setAdditionalData(this.map(ex.getLatestVerification(), UserVerificationResponse.class));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
    }

    protected <T> void ensureThatModelIsValid(T model) throws AggregatedApiError {
        var violations = this.validator.validateModel(model);
        if(violations.size() > 0) {
            var ex = new AggregatedApiError();
            ex.setErrors(violations);
            throw ex;
        }
    }

    private ValidationViolation processDataIntegrityViolation(DataIntegrityViolationException source) {
        String specificMessage = source.getMostSpecificCause().getMessage();

        String code = null;
        String path = null;
        if (this.isMessageContaining(specificMessage, "UN_EMAIL")) {
            code = "ID.006";
            path = "email";
        } else if (this.isMessageContaining(specificMessage, "UN_USERNAME")) {
            code = "ID.005";
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

    protected Optional<User> getUser() {
        String uid = (String) this.currentRequest.getAttribute("uid");

        return uid != null ? this.userRepository.findById(uid) : Optional.empty();
    }


    @SuppressWarnings("unchecked")
    protected <T> void ensureThatUserHasAccessToAction(T subject, Action action)
            throws AccessDeniedException {
        User user = this.getUser().orElseThrow(AccessDeniedException::new);
        var voters = this.voters.stream()
                .filter(
                        voter ->
                                voter.getSupportedSubjectType().equals(subject.getClass()) &&
                                        Arrays.asList(voter.getSupportedActions())
                                                .contains(action))
                .collect(Collectors.toSet());

        var result = voters.stream().allMatch(v -> v.vote(subject, action, user));

        if(!result) {
            throw new AccessDeniedException();
        }
    }
}
