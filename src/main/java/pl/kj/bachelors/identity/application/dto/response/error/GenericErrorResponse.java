package pl.kj.bachelors.identity.application.dto.response.error;

public class GenericErrorResponse {
    private String detailCode;
    private String detailMessage;

    public GenericErrorResponse() {
        this(null, null);
    }

    public GenericErrorResponse(String detailMessage, String detailCode) {
        this.detailMessage = detailMessage;
        this.detailCode = detailCode;
    }

    public String getDetailCode() {
        return detailCode;
    }

    public void setDetailCode(String detailCode) {
        this.detailCode = detailCode;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public void setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
    }
}
