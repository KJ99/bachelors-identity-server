package pl.kj.bachelors.identity.unit.infrastructure.service.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import pl.kj.bachelors.identity.BaseTest;
import pl.kj.bachelors.identity.domain.config.JwtCookieConfig;
import pl.kj.bachelors.identity.infrastructure.service.jwt.RefreshTokenManagementService;

import javax.servlet.http.Cookie;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class RefreshTokenManagementServiceTests extends BaseTest {
    @Autowired
    private RefreshTokenManagementService service;
    @Autowired
    private JwtCookieConfig config;

    @Test
    public void testGetFromRequest() {
        final String tokenValue = "auth-token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(this.config.getName(), tokenValue));

        Optional<String> jwt = this.service.getFromRequest(request);

        assertThat(jwt.isPresent()).isTrue();
        assertThat(jwt.get()).isEqualTo(tokenValue);
    }

    @Test
    public void testPutInResponse() {
        final String tokenValue = "auth-token";
        MockHttpServletResponse response = new MockHttpServletResponse();

        this.service.putInResponse(tokenValue, response);

        Cookie cookie = response.getCookie(this.config.getName());
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isEqualTo(tokenValue);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isTrue();
    }
}
