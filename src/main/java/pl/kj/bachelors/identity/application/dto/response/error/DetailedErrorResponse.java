package pl.kj.bachelors.identity.application.dto.response.error;

public class DetailedErrorResponse {
    private String type;
    private String message;
    private DetailedErrorResponse cause;

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(String type) {
        this.type = type;
    }

    public DetailedErrorResponse getCause() {
        return cause;
    }

    public void setCause(DetailedErrorResponse cause) {
        this.cause = cause;
    }
}
