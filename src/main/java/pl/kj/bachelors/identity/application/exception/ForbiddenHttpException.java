package pl.kj.bachelors.identity.application.exception;

public class ForbiddenHttpException extends BaseHttpException {
    protected int httpCode = 403;
    private String detailCode = null;

    public void setDetailCode(String detailCode) {
        this.detailCode = detailCode;
    }

    public String getDetailCode() {
        return this.detailCode;
    }
}
