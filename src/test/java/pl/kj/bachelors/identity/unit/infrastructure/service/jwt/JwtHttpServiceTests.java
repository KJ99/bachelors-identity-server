package pl.kj.bachelors.identity.unit.infrastructure.service.jwt;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import pl.kj.bachelors.identity.application.Application;
import pl.kj.bachelors.identity.domain.config.JwtCookieConfig;
import pl.kj.bachelors.identity.infrastructure.service.jwt.JwtHttpService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = {Application.class})
public class JwtHttpServiceTests {
    @Autowired
    private JwtHttpService service;
    @Autowired
    private JwtCookieConfig cookieConfig;

    @Test
    public void testGetFromRequest() {
        final String tokenValue = "auth-token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cookie cookie = new Cookie(this.cookieConfig.getName(), tokenValue);
        request.setCookies(cookie);

        String jwt = this.service.getFromRequest(request);

        assertThat(jwt).isEqualTo(tokenValue);
    }

    @Test
    public void testPutInResponse() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String tokenValue = "auth-token";

        this.service.putInResponse(tokenValue, response);

        Cookie cookie = response.getCookie(this.cookieConfig.getName());

        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isEqualTo(tokenValue);
        assertThat(cookie.getMaxAge()).isEqualTo(this.cookieConfig.getValidTimeInMinutes() * 60);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isTrue();
    }
}
