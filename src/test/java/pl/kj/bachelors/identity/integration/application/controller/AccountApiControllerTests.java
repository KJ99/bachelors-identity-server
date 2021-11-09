package pl.kj.bachelors.identity.integration.application.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.kj.bachelors.identity.application.Application;
import pl.kj.bachelors.identity.domain.config.PasswordConfig;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.model.entity.UserVerification;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;
import pl.kj.bachelors.identity.infrastructure.repository.UserVerificationRepository;

import java.util.Calendar;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ContextConfiguration(classes = { Application.class })
@ComponentScan(basePackages = "pl.kj.bachelors.identity")
@AutoConfigureMockMvc
public class AccountApiControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserVerificationRepository verificationRepository;


    @Test
    public void testPost_Created() throws Exception {
        String requestBody = "{" +
                "\"email\": \"testmail@test.eu\"," +
                "\"username\": \"someusername\"," +
                "\"first_name\": \"John\"," +
                "\"last_name\": \"Doe\"," +
                "\"password\": \"P@ssw0rd\"," +
                "\"confirm_password\": \"P@ssw0rd\"" +
                "}";
        MvcResult result = mockMvc.perform(
                post("/v1/account")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isCreated()).andReturn();
        assertThat(result.getResponse().getContentAsString()).contains("verification");
    }

    @Test
    public void testPost_BadRequest() throws Exception {
        String requestBody = "{" +
                "\"email\": \"testmailtest.eu\"," +
                "\"username\": \"a\"," +
                "\"first_name\": \"John\"," +
                "\"last_name\": \"Doe\"," +
                "\"password\": \"ab\"," +
                "\"confirm_password\": \"P@ssw0rd\"" +
                "}";
        var result = mockMvc.perform(
                post("/v1/account")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isBadRequest()).andReturn();

        assertThat(result.getResponse().getContentAsString())
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    public void testPost_Conflict() throws Exception {
        var user = this.loadSampleUser();
        String requestBody = String.format(
                "{" +
                "\"email\": \"%s\"," +
                "\"username\": \"someusername\"," +
                "\"first_name\": \"John\"," +
                "\"last_name\": \"Doe\"," +
                "\"password\": \"P@ssw0rd\"," +
                "\"confirm_password\": \"P@ssw0rd\"" +
                "}",
                user.getEmail()
        );
        MvcResult result = mockMvc.perform(
                post("/v1/account")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isConflict()).andReturn();
        assertThat(result.getResponse().getContentAsString()).contains("email");
    }

    @Test
    public void testVerificationResend() throws Exception {
        var user = this.loadSampleUser();
        String requestBody = String.format("{\"email\": \"%s\"}", user.getEmail());
        var result = mockMvc.perform(
                post("/v1/account/verification/resend")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("verification");
    }

    @Test
    public void testVerificationResend_NotFound() throws Exception {
        String requestBody = "{\"email\": \"fake-email\"}";
        mockMvc.perform(
                post("/v1/account/verification/resend")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isNotFound());
    }

    @Test
    public void testVerify() throws Exception {
        var user = this.loadSampleUser();
        var verification = this.loadSampleVerification(user);
        String requestBody = String.format(
                "{\"token\": \"%s\", \"pin\": \"%s\"}",
                verification.getToken(),
                verification.getPin()
        );

        mockMvc.perform(
                put("/v1/account/verify")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isNoContent());
    }

    @Test
    public void testVerify_NotFound() throws Exception {
        var user = this.loadSampleUser();
        var verification = this.loadSampleVerification(user);
        String requestBody = String.format(
                "{\"token\": \"fake-token\", \"pin\": \"%s\"}",
                verification.getToken()
        );

        mockMvc.perform(
                put("/v1/account/verify")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isNotFound());
    }

    @Test
    public void testVerify_BadRequest() throws Exception {
        var user = this.loadSampleUser();
        var verification = this.loadSampleVerification(user);
        String requestBody = String.format(
                "{\"token\": \"%s\", \"pin\": \"fake-pin\"}",
                verification.getToken()
        );

        var result = mockMvc.perform(
                put("/v1/account/verify")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isBadRequest()).andReturn();

        assertThat(result.getResponse().getContentAsString()).isNotEmpty();
    }


    private User loadSampleUser() {
        User user = new User();
        user.setUid("uid-10");
        user.setEmail(UUID.randomUUID().toString().concat("@testaroomail.mail"));
        user.setUserName(UUID.randomUUID().toString());
        user.setFirstName("Abc");
        user.setLastName("Abc");
        user.setPassword("pass");
        user.setSalt(UUID.randomUUID().toString());

        this.userRepository.saveAndFlush(user);

        return user;
    }

    private UserVerification loadSampleVerification(User user) {
        var verification = new UserVerification();
        verification.setUser(user);
        verification.setPin("123456");
        verification.setToken("some-token");
        verification.setExpiresAt(Calendar.getInstance());
        verification.getExpiresAt().add(Calendar.HOUR, 10);

        this.verificationRepository.saveAndFlush(verification);

        return verification;
    }
}
