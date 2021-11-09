package pl.kj.bachelors.identity.integration.application.controller;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultJwtBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.kj.bachelors.identity.application.Application;
import pl.kj.bachelors.identity.domain.config.JwtConfig;
import pl.kj.bachelors.identity.domain.config.JwtCookieConfig;
import pl.kj.bachelors.identity.domain.config.PasswordConfig;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.model.entity.UserVerification;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;
import pl.kj.bachelors.identity.infrastructure.repository.UserVerificationRepository;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Autowired
    private PasswordConfig passwordConfig;

    @Autowired
    private UserVerificationRepository verificationRepository;

    @Test
    public void testRefresh_Accepted() throws Exception {
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

    @Test
    public void testLogin_Ok() throws Exception {
        String correctUsername = "foobar@foobar.pl";
        String correctPassword = "P@ssw0rdo";
        this.loadUser(correctUsername, correctPassword, true);

        String requestBody = String.format(
                "{\"username\": \"%s\", \"password\": \"%s\"}",
                correctUsername,
                correctPassword
        );

        MvcResult result = this.mockMvc.perform(
                post("/v1/auth/login")
                        .contentType("application/json")
                        .content(requestBody.getBytes())
        ).andExpect(status().isOk()).andReturn();

        MockHttpServletResponse response = result.getResponse();
        Cookie cookie = response.getCookie(this.cookieConfig.getName());
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isNotEmpty();

        assertThat(response.getContentAsString())
                .contains("token")
                .contains("name");

    }

    @Test
    public void testLogin_BadRequest() throws Exception {
        String correctUsername = "foobar@foobar.pl";
        String correctPassword = "P@ssw0rdo";
        this.loadUser(correctUsername, correctPassword, true);

        String requestBody = String.format(
                "{\"username\": \"%s\", \"password\": \"%s\"}",
                correctUsername,
                "fake-password"
        );

        this.mockMvc.perform(
                post("/v1/auth/login")
                        .contentType("application/json")
                        .content(requestBody.getBytes())
        ).andExpect(status().isBadRequest()).andReturn();

    }

    @Test
    public void testLogin_Forbidden() throws Exception {
        String correctUsername = "foobar@foobar.pl";
        String correctPassword = "P@ssw0rdo";
        User user = this.loadUser(correctUsername, correctPassword, false);
        this.loadVerification(user);

        String requestBody = String.format(
                "{\"username\": \"%s\", \"password\": \"%s\"}",
                correctUsername,
                correctPassword
        );

        MvcResult result = this.mockMvc.perform(
                post("/v1/auth/login")
                        .contentType("application/json")
                        .content(requestBody.getBytes())
        ).andExpect(status().isForbidden()).andReturn();

        assertThat(result.getResponse().getContentAsString())
                .contains("ID.031")
                .contains("verification_token");

    }

    private User loadUser(String email, String password, boolean verified) {
        String salt = BCrypt.gensalt(this.passwordConfig.getSaltRounds());
        String hash = BCrypt.hashpw(password, salt);
        User user = new User();
        user.setUid("uid-10");
        user.setEmail(email);
        user.setUserName(UUID.randomUUID().toString());
        user.setFirstName("Abc");
        user.setLastName("Abc");
        user.setPassword(hash);
        user.setSalt(salt);
        user.setActive(true);
        user.setVerified(verified);

        this.userRepository.saveAndFlush(user);

        return user;
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

    private UserVerification loadVerification(User user) {
        UserVerification verification = new UserVerification();
        verification.setExpiresAt(Calendar.getInstance());
        verification.setPin("000000");
        verification.setToken("veri-token");
        verification.setUser(user);

        this.verificationRepository.saveAndFlush(verification);

        return verification;
    }
}
