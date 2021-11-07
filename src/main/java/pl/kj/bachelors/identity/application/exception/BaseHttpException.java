package pl.kj.bachelors.identity.application.exception;

abstract class BaseHttpException extends Throwable {
    protected final int httpCode = 500;
    protected final String errorCode;
    protected final String message;

    public BaseHttpException(String message, String errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }

    public BaseHttpException(String message) {
        this(message, null);
    }

    public BaseHttpException() {
        this("Unexpected error has occurred", null);
    }

    public int getHttpCode() {
        return httpCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
