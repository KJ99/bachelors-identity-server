package pl.kj.bachelors.identity.infrastructure.service.jwt;

import com.google.common.net.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.kj.bachelors.identity.domain.config.JwtConfig;
import pl.kj.bachelors.identity.domain.config.JwtCookieConfig;
import pl.kj.bachelors.identity.domain.service.jwt.AccessTokenFetcher;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
public class FetchAccessTokenService implements AccessTokenFetcher {
    private final JwtConfig config;

    @Autowired
    public FetchAccessTokenService(JwtConfig config) {
        this.config = config;
    }

    @Override
    public Optional<String> getAccessTokenFromRequest(HttpServletRequest request) {
        String token = null;
        String headerValue = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(headerValue != null) {
            String[] chunks = headerValue.split(" ");
            token = chunks.length > 0 && chunks[0].equals(this.config.getType()) ? chunks[1] : null;
        }
        return Optional.ofNullable(token);
    }
}
