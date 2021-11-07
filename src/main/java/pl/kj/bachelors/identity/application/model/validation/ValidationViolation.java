package pl.kj.bachelors.identity.application.model.validation;

public class ValidationViolation {
    private final String path;
    private final String message;
    private final String code;

    public ValidationViolation(String message, String path, String code) {
        this.message = message;
        this.path = path;
        this.code = code;
    }

    public String getPath() {
        return path;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }
}
