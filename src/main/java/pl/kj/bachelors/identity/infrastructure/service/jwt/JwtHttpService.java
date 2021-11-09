package pl.kj.bachelors.identity.infrastructure.service.jwt;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.kj.bachelors.identity.domain.config.JwtCookieConfig;
import pl.kj.bachelors.identity.domain.service.jwt.JwtHttpManager;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;

@Service
public class JwtHttpService implements JwtHttpManager {
    private JwtCookieConfig config;

    public JwtHttpService(@Autowired JwtCookieConfig config) {
        this.config = config;
    }

    @Override
    public String getFromRequest(HttpServletRequest request) {
        Optional<Cookie> cookie = Arrays.stream(request.getCookies())
                        .filter(item -> item.getName().equals(this.config.getName()))
                        .findFirst();

        return cookie.map(Cookie::getValue).orElse(null);
    }

    @Override
    public void putInResponse(String token, HttpServletResponse response) {
        Cookie cookie = new Cookie(this.config.getName(), token);
        cookie.setDomain(this.config.getDomain());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(this.config.getValidTimeInMinutes() * 60);

        response.addCookie(cookie);
    }
}
