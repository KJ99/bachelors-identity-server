package pl.kj.bachelors.identity.domain.service.jwt;

import pl.kj.bachelors.identity.domain.model.entity.User;

public interface JwtGenerator {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
}
