package pl.kj.bachelors.identity.integration.application.controller;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultJwtBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import pl.kj.bachelors.identity.application.Application;
import pl.kj.bachelors.identity.domain.config.JwtConfig;
import pl.kj.bachelors.identity.domain.config.JwtCookieConfig;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import java.util.Calendar;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ContextConfiguration(classes = {Application.class})
@AutoConfigureMockMvc
public class AuthenticationApiControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private JwtCookieConfig cookieConfig;

    @Test
    public void testRefresh_Correct() throws Exception {
        var user = this.loadUser("uid-1");
        Calendar accessExpiresAt = Calendar.getInstance();
        Calendar refreshExpiresAt = Calendar.getInstance();
        accessExpiresAt.add(Calendar.HOUR, -1);
        refreshExpiresAt.add(Calendar.HOUR, 1);

        String access = this.getJwt(accessExpiresAt, user.getUid());
        String refresh = this.getJwt(refreshExpiresAt, user.getUid());

        MvcResult mvcResult = this.mockMvc.perform(
                        put("/v1/auth/refresh")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        String.format("%s %s", this.jwtConfig.getType(), access)
                                )
                                .cookie(new Cookie(this.cookieConfig.getName(), refresh))
                )
                .andExpect(status().isAccepted())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Cookie cookie = response.getCookie(this.cookieConfig.getName());
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isNotEmpty().isNotEqualTo(refresh);

        assertThat(response.getContentAsString()).contains("token");
    }

    @Test
    public void testRefresh_Forbidden() throws Exception {
        var user = this.loadUser("uid-1");
        var user2 = this.loadUser("uid-2");
        Calendar accessExpiresAt = Calendar.getInstance();
        Calendar refreshExpiresAt = Calendar.getInstance();
        accessExpiresAt.add(Calendar.HOUR, -1);
        refreshExpiresAt.add(Calendar.HOUR, 1);

        String access = this.getJwt(accessExpiresAt, user.getUid());
        String refresh = this.getJwt(refreshExpiresAt, user2.getUid());

        this.mockMvc.perform(
                        put("/v1/auth/refresh")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        String.format("%s %s", this.jwtConfig.getType(), access)
                                )
                                .cookie(new Cookie(this.cookieConfig.getName(), refresh))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void testRefresh_NotAuthorized_NoAccess() throws Exception {
        var user = this.loadUser("uid-1");
        Calendar refreshExpiresAt = Calendar.getInstance();
        refreshExpiresAt.add(Calendar.HOUR, 1);

        String refresh = this.getJwt(refreshExpiresAt, user.getUid());

        this.mockMvc.perform(
                        put("/v1/auth/refresh")
                                .cookie(new Cookie(this.cookieConfig.getName(), refresh))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testRefresh_NotAuthorized_NoRefresh() throws Exception {
        var user = this.loadUser("uid-1");
        Calendar accessExpiresAt = Calendar.getInstance();
        accessExpiresAt.add(Calendar.HOUR, -1);

        String access = this.getJwt(accessExpiresAt, user.getUid());

        this.mockMvc.perform(
                put("/v1/auth/refresh")
                                .header(HttpHeaders.AUTHORIZATION, access)
                )
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    private User loadUser(String uid) {
        User user = new User();
        user.setUid(uid);
        user.setUserName(UUID.randomUUID().toString());
        user.setEmail(UUID.randomUUID().toString());
        user.setFirstName("Ab");
        user.setLastName("Ba");
        user.setPassword("pass");
        user.setSalt(UUID.randomUUID().toString());
        user.setActive(true);
        this.userRepository.saveAndFlush(user);

        return user;
    }

    private String getJwt(Calendar expiresAt, String uid) {
        SecretKeySpec spec = new SecretKeySpec(this.jwtConfig.getSecret().getBytes(), this.jwtConfig.getAlgorithm());

        DefaultJwtBuilder builder = new DefaultJwtBuilder();
        builder.setSubject(uid);
        builder.setExpiration(expiresAt.getTime());
        builder.signWith(SignatureAlgorithm.forName(this.jwtConfig.getAlgorithm()), spec);

        return builder.compact();
    }
}
