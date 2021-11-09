package pl.kj.bachelors.identity.application.exception;

import pl.kj.bachelors.identity.domain.exception.ValidationViolation;

public class ConflictHttpException extends BaseHttpException {
    protected final int httpCode = 409;
    protected ValidationViolation error;

    public ValidationViolation getError() {
        return this.error;
    }

    public void setError(ValidationViolation error) {
        this.error = error;
    }
}
