package pl.kj.bachelors.identity.domain.exception;

import java.util.Collection;

public class AggregatedApiError extends Throwable {
    private Collection<ValidationViolation> errors;

    public Collection<ValidationViolation> getErrors() {
        return errors;
    }

    public void setErrors(Collection<ValidationViolation> errors) {
        this.errors = errors;
    }
}
