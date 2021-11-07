package pl.kj.bachelors.identity.application.exception;

import pl.kj.bachelors.identity.application.model.validation.ValidationViolation;

import java.util.Collection;
import java.util.LinkedList;

public class BadRequestHttpException extends BaseHttpException {
    protected int httpCode = 400;
    protected Collection<ValidationViolation> errors = new LinkedList<>();

    public Collection<ValidationViolation> getErrors() {
        return this.errors;
    }

    public void setErrors(Collection<ValidationViolation> errors) {
        this.errors = errors;
    }
}
