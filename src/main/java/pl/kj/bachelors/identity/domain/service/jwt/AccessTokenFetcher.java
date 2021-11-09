package pl.kj.bachelors.identity.domain.service.jwt;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public interface AccessTokenFetcher {
    Optional<String> getAccessTokenFromRequest(HttpServletRequest request);
}
