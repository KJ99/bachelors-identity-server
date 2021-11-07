package pl.kj.bachelors.identity.application.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.kj.bachelors.identity.application.dto.response.error.GenericErrorResponse;
import pl.kj.bachelors.identity.application.exception.*;
import pl.kj.bachelors.identity.application.model.validation.ValidationViolation;

import java.nio.file.NoSuchFileException;
import java.util.Collection;
import java.util.stream.Collectors;

abstract class BaseApiController {
    protected final ModelMapper mapper;
    protected final String activeProfile;

    BaseApiController(@Autowired ModelMapper mapper, @Value("spring.profiles.active") String activeProfile) {
        this.mapper = mapper;
        this.activeProfile = activeProfile;
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
    @ExceptionHandler(value = { NotFoundHttpException.class, NoSuchFileException.class})
    protected ResponseEntity<Object> handleNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictHttpException.class)
    protected ResponseEntity<ValidationViolation> handleConflict(ConflictHttpException ex) {
        final var bodyBuilder = ResponseEntity.status(HttpStatus.CONFLICT);

        return ex.getError() != null ? bodyBuilder.body(ex.getError()) : bodyBuilder.build();
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NotAuthorizedHttpException.class)
    protected ResponseEntity<Object> handleNotAuthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestHttpException.class)
    protected ResponseEntity<Collection<ValidationViolation>> handleBadRequest(BadRequestHttpException ex) {
        return ResponseEntity.badRequest().body(ex.getErrors());
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
}
