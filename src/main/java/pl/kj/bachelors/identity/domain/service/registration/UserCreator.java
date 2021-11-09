package pl.kj.bachelors.identity.domain.service.registration;

import pl.kj.bachelors.identity.domain.model.entity.User;

public interface UserCreator {
    User createUser(String email, String username, String firstName, String lastName, String password);
}
