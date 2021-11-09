package pl.kj.bachelors.identity.domain.service.jwt;

import pl.kj.bachelors.identity.domain.model.User;

public interface JwtGenerator {
    String generateJwt(User user);
}
