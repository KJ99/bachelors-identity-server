package pl.kj.bachelors.identity.unit.infrastructure.service.jwt;

import com.google.common.net.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import pl.kj.bachelors.identity.application.Application;
import pl.kj.bachelors.identity.domain.config.JwtConfig;
import pl.kj.bachelors.identity.domain.config.JwtCookieConfig;
import pl.kj.bachelors.identity.infrastructure.service.jwt.FetchAccessTokenService;

import javax.servlet.http.Cookie;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = {Application.class})
public class FetchAccessTokenServiceTests {
    @Autowired
    private FetchAccessTokenService service;

    @Autowired
    private JwtConfig jwtConfig;

    @Test
    public void testGetFromRequest() {
        final String tokenValue = "auth-token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, String.format("%s %s", jwtConfig.getType(), tokenValue));

        Optional<String> jwt = this.service.getAccessTokenFromRequest(request);

        assertThat(jwt.isPresent()).isTrue();
        assertThat(jwt.get()).isEqualTo(tokenValue);
    }
}
