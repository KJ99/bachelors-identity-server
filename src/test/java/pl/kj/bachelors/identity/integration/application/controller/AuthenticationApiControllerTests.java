package pl.kj.bachelors.identity.integration.application.controller;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultJwtBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.kj.bachelors.identity.application.Application;
import pl.kj.bachelors.identity.application.dto.request.LoginRequest;
import pl.kj.bachelors.identity.application.dto.request.PasswordResetRequest;
import pl.kj.bachelors.identity.domain.config.JwtConfig;
import pl.kj.bachelors.identity.domain.config.JwtCookieConfig;
import pl.kj.bachelors.identity.domain.config.PasswordConfig;
import pl.kj.bachelors.identity.domain.model.entity.PasswordReset;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.model.entity.UserVerification;
import pl.kj.bachelors.identity.infrastructure.repository.PasswordResetRepository;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;
import pl.kj.bachelors.identity.infrastructure.repository.UserVerificationRepository;
import pl.kj.bachelors.identity.integration.BaseIntegrationTest;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import java.util.Calendar;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthenticationApiControllerTests extends BaseIntegrationTest {
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

    @Autowired
    private PasswordResetRepository passwordResetRepository;

    @Test
    public void testRefresh_Accepted() throws Exception {
        String access = this.generateExpiredAccessToken("uid-active-1");
        String refresh = this.generateValidAccessToken("uid-active-1");

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
        String access = this.generateValidAccessToken("uid-active-1");
        String refresh = this.generateValidAccessToken("uid-active-2");

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
        String refresh = this.generateValidAccessToken("uid-active-1");

        this.mockMvc.perform(
                        put("/v1/auth/refresh")
                                .cookie(new Cookie(this.cookieConfig.getName(), refresh))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testRefresh_NotAuthorized_NoRefresh() throws Exception {
        String access = this.generateValidAccessToken("uid-active-1");

        this.mockMvc.perform(
                put("/v1/auth/refresh")
                                .header(HttpHeaders.AUTHORIZATION, access)
                )
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    public void testLogin_Ok() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("active-1");
        request.setPassword("foobar");
        String requestBody = this.serialize(request);

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
        LoginRequest request = new LoginRequest();
        request.setUsername("active-1");
        request.setPassword("fake-pass");
        String requestBody = this.serialize(request);

        this.mockMvc.perform(
                post("/v1/auth/login")
                        .contentType("application/json")
                        .content(requestBody.getBytes())
        ).andExpect(status().isBadRequest()).andReturn();

    }

    @Test
    public void testLogin_Forbidden() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("not-verified-1");
        request.setPassword("foobar");
        String requestBody = this.serialize(request);

        MvcResult result = this.mockMvc.perform(
                post("/v1/auth/login")
                        .contentType("application/json")
                        .content(requestBody.getBytes())
        ).andExpect(status().isForbidden()).andReturn();

        assertThat(result.getResponse().getContentAsString())
                .contains("ID.031")
                .contains("verification_token");

    }

    @Test
    public void testInitPasswordReset_Accepted() throws Exception {
        var result = this.mockMvc.perform(
                post("/v1/auth/password-reset/init")
                        .contentType("application/json")
                        .content("{\"email\": \"activeuser2@fakemail\"}")
        ).andExpect(status().isAccepted()).andReturn();
        assertThat(result.getResponse().getContentAsString()).contains("token");
    }

    @Test
    public void testInitPasswordReset_NotFound() throws Exception {
        this.mockMvc.perform(
                post("/v1/auth/password-reset/init")
                        .contentType("application/json")
                        .content("{\"email\": \"fake-email\"}")
        ).andExpect(status().isNotFound());

    }

    @Test
    public void testResetPassword_NoContent() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setToken("active-token-1");
        request.setPin("012345");
        request.setPassword("P@ssw0rdoo00");
        request.setConfirmPassword("P@ssw0rdoo00");
        String requestBody = this.serialize(request);

        this.mockMvc.perform(
                post("/v1/auth/password-reset")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isNoContent());

    }

    @Test
    public void testResetPassword_NotFound() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setToken("fake-token");
        request.setPin("012345");
        request.setPassword("P@ssw0rdoo00");
        request.setConfirmPassword("P@ssw0rdoo00");
        String requestBody = this.serialize(request);
        this.mockMvc.perform(
                post("/v1/auth/password-reset")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isNotFound());

    }

    @Test
    public void testResetPassword_Forbidden() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setToken("expired-token-1");
        request.setPin("012345");
        request.setPassword("P@ssw0rdoo00");
        request.setConfirmPassword("P@ssw0rdoo00");
        String requestBody = this.serialize(request);
        this.mockMvc.perform(
                post("/v1/auth/password-reset")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isForbidden());
    }

    @Test
    public void testResetPassword_BadRequest() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setToken("active-token-1");
        request.setPin("fake-pin");
        request.setPassword("P@ssw0rdoo00");
        request.setConfirmPassword("P@ssw0rdoo00");
        String requestBody = this.serialize(request);
        this.mockMvc.perform(
                post("/v1/auth/password-reset")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isBadRequest());
    }
}
