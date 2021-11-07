package pl.kj.bachelors.identity.application.exception;

public class NotFoundHttpException extends BaseHttpException {
    protected final int httpCode = 404;
}
