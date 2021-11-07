package pl.kj.bachelors.identity.application.dto.response.health;

public class SingleCheckResponse {
    private String serviceName;
    private String status;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
