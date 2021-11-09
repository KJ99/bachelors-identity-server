package pl.kj.bachelors.identity.domain.service.jwt;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

public interface RefreshTokenManager {
    Optional<String> getFromRequest(HttpServletRequest request);
    void putInResponse(String token, HttpServletResponse response);
}
