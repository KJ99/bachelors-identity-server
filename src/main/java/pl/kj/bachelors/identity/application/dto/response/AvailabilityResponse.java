package pl.kj.bachelors.identity.application.dto.response;

import io.swagger.annotations.ApiModel;

@ApiModel
public class AvailabilityResponse {
    private boolean available;

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
