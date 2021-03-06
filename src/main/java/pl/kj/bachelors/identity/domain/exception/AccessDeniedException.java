package pl.kj.bachelors.identity.domain.exception;

public class AccessDeniedException extends Throwable{
    protected final String message;

    public AccessDeniedException() {
        this("Access Denied");
    }

    public AccessDeniedException(String message) {
        this.message = message;
    }
}
