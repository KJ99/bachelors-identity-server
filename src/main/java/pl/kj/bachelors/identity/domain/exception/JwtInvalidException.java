package pl.kj.bachelors.identity.domain.exception;

public class JwtInvalidException extends Throwable {
    public JwtInvalidException() {
        super("JWT is not valid");
    }
    public JwtInvalidException(String message) {
        super(message);
    }
}
