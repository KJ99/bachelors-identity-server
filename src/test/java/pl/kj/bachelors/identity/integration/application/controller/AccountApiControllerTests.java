package pl.kj.bachelors.identity.integration.application.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.kj.bachelors.identity.application.dto.request.AccountVerificationRequest;
import pl.kj.bachelors.identity.application.dto.request.RegistrationRequest;
import pl.kj.bachelors.identity.application.dto.request.VerificationResendRequest;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;
import pl.kj.bachelors.identity.infrastructure.repository.UserVerificationRepository;
import pl.kj.bachelors.identity.integration.BaseIntegrationTest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AccountApiControllerTests extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserVerificationRepository verificationRepository;


    @Test
    public void testPost_Created() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("testmail@test.eu");
        request.setUsername("someusername");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPassword("P@ssw0rd");
        request.setConfirmPassword("P@ssw0rd");
        String requestBody = this.serialize(request);

        MvcResult result = mockMvc.perform(
                post("/v1/account")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isCreated()).andReturn();
        assertThat(result.getResponse().getContentAsString()).contains("verification");
    }

    @Test
    public void testPost_BadRequest() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("testmail@test.eu");
        request.setUsername("a");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPassword("P@ssw0rd");
        request.setConfirmPassword("P@ssw0rd");
        String requestBody = this.serialize(request);

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
        var user = this.userRepository.findById("uid-active-1").orElseThrow();
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail(user.getEmail());
        request.setUsername("someusername");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPassword("P@ssw0rd");
        request.setConfirmPassword("P@ssw0rd");
        String requestBody = this.serialize(request);

        MvcResult result = mockMvc.perform(
                post("/v1/account")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isConflict()).andReturn();
        assertThat(result.getResponse().getContentAsString()).contains("email");
    }

    @Test
    public void testVerificationResend() throws Exception {
        var user = this.userRepository.findById("uid-not-verified-1").orElseThrow();

        VerificationResendRequest request = new VerificationResendRequest();
        request.setEmail(user.getEmail());
        String requestBody = this.serialize(request);

        var result = mockMvc.perform(
                post("/v1/account/verification/resend")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("verification");
    }

    @Test
    public void testVerificationResend_NotFound() throws Exception {
        VerificationResendRequest request = new VerificationResendRequest();
        request.setEmail("fake-mail");
        String requestBody = this.serialize(request);

        mockMvc.perform(
                post("/v1/account/verification/resend")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isNotFound());
    }

    @Test
    public void testVerify() throws Exception {
        var verification = this.verificationRepository.findByToken("active-token-1").orElseThrow();
        AccountVerificationRequest request = new AccountVerificationRequest();
        request.setToken(verification.getToken());
        request.setPin(verification.getPin());
        String requestBody = this.serialize(request);

        mockMvc.perform(
                put("/v1/account/verify")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isNoContent());
    }

    @Test
    public void testVerify_NotFound() throws Exception {
        var verification = this.verificationRepository.findByToken("active-token-1").orElseThrow();
        AccountVerificationRequest request = new AccountVerificationRequest();
        request.setToken("fake-token");
        request.setPin(verification.getPin());
        String requestBody = this.serialize(request);

        mockMvc.perform(
                put("/v1/account/verify")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isNotFound());
    }

    @Test
    public void testVerify_BadRequest() throws Exception {
        var verification = this.verificationRepository.findByToken("active-token-1").orElseThrow();        AccountVerificationRequest request = new AccountVerificationRequest();
        request.setToken(verification.getToken());
        request.setPin("fake-pin");
        String requestBody = this.serialize(request);

        var result = mockMvc.perform(
                put("/v1/account/verify")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isBadRequest()).andReturn();

        assertThat(result.getResponse().getContentAsString()).isNotEmpty();
    }

    @Test
    public void testGetAvailability() throws Exception {
        String url = String.format(
                "/v1/account/availability?field=email&value=%s",
                URLEncoder.encode("some-fake-email@email.com", StandardCharsets.UTF_8)
        );
        var result = mockMvc.perform(
                get(url)
        ).andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("available");
    }
}
