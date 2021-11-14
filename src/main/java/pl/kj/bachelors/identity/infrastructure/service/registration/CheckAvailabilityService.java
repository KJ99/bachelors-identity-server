package pl.kj.bachelors.identity.infrastructure.service.registration;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.kj.bachelors.identity.domain.service.registration.AvailabilityChecker;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;

@Service
public class CheckAvailabilityService implements AvailabilityChecker {
    private final UserRepository repository;

    @Autowired
    public CheckAvailabilityService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean isAvailable(String fieldName, String value) {
        boolean result = false;
        if(fieldName.equals("email")) {
            result = this.repository.countByEmail(value) == 0;
        } else if(fieldName.equals("username")) {
            result = this.repository.countByUserName(value) == 0;
        }

        return result;
    }
}
