package pl.kj.bachelors.identity.domain.service.jwt;

import pl.kj.bachelors.identity.domain.model.AuthResult;
import pl.kj.bachelors.identity.domain.model.payload.TokenAuthPayload;

public interface TokenRefresher {
    AuthResult<TokenAuthPayload> refreshToken(String accessToken, String refreshToken);
}
