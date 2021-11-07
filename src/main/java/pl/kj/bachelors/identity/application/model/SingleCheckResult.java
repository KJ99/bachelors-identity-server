package pl.kj.bachelors.identity.application.model;

public class SingleCheckResult {
    private String serviceName;
    private boolean active;

    public SingleCheckResult(String serviceName, boolean active) {
        this.serviceName = serviceName;
        this.active = active;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
