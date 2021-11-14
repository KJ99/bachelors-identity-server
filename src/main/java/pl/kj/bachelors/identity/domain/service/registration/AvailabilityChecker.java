package pl.kj.bachelors.identity.domain.service.registration;

public interface AvailabilityChecker {
    boolean isAvailable(String fieldName, String value);
}
