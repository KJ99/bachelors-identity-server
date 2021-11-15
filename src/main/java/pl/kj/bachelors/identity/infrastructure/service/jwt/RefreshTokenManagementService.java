package pl.kj.bachelors.identity.infrastructure.service.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.kj.bachelors.identity.domain.config.JwtCookieConfig;
import pl.kj.bachelors.identity.domain.service.jwt.RefreshTokenManager;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;

@Service
public class RefreshTokenManagementService implements RefreshTokenManager {
    private final JwtCookieConfig config;

    @Autowired
    public RefreshTokenManagementService(JwtCookieConfig config) {
        this.config = config;
    }

    @Override
    public Optional<String> getFromRequest(HttpServletRequest request) {
        String token = null;
        if(request.getCookies() != null) {
            Optional<Cookie> cookie = Arrays.stream(request.getCookies())
                    .filter(item -> item.getName().equals(this.config.getName()))
                    .findFirst();
            token = cookie.map(Cookie::getValue).orElse(null);
        }

        return Optional.ofNullable(token);
    }

    @Override
    public void putInResponse(String token, HttpServletResponse response) {
        Cookie cookie = new Cookie(this.config.getName(), token);
        cookie.setMaxAge(this.config.getValidTimeInMinutes() * 60);
        cookie.setDomain(this.config.getDomain());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);

        response.addCookie(cookie);
    }
}
