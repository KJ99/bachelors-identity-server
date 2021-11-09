package pl.kj.bachelors.identity.application.dto.response.error;

import io.swagger.annotations.ApiModel;

@ApiModel
public class ValidationErrorResponse {
    private String path;
    private String message;
    private String code;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
