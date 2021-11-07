package pl.kj.bachelors.identity.application.exception;

public class NotAuthorizedHttpException extends BaseHttpException {
    protected final int httpCode = 401;
}
