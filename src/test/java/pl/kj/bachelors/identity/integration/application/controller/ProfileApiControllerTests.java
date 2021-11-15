package pl.kj.bachelors.identity.integration.application.controller;

import com.google.common.net.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import pl.kj.bachelors.identity.application.Application;
import pl.kj.bachelors.identity.application.dto.request.ChangePasswordRequest;
import pl.kj.bachelors.identity.infrastructure.service.auth.PasswordUpdateServiceImpl;
import pl.kj.bachelors.identity.integration.BaseIntegrationTest;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProfileApiControllerTests extends BaseIntegrationTest {

    @Test
    public void testChangePassword_NoContent() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("foobar");
        request.setNewPassword("P@ssw0rdooo0");
        request.setConfirmPassword("P@ssw0rdooo0");
        String requestBody = this.serialize(request);
        String auth = String.format("%s %s", this.jwtConfig.getType(), this.generateValidAccessToken("uid-active-1"));

        this.mockMvc.perform(
                put("/v1/profile/password")
                        .contentType("application/json")
                        .content(requestBody.getBytes(StandardCharsets.UTF_8))
                        .header(HttpHeaders.AUTHORIZATION, auth)
        ).andExpect(status().isNoContent());
    }

    @Test
    public void testChangePassword_BadRequest_PasswordsNotMatch() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("foobar");
        request.setNewPassword("P@ssw0rdooo0");
        request.setConfirmPassword("some-not-matching");
        String requestBody = this.serialize(request);
        String auth = String.format("%s %s", this.jwtConfig.getType(), this.generateValidAccessToken("uid-active-1"));

        MvcResult result = this.mockMvc.perform(
                put("/v1/profile/password")
                        .content(requestBody.getBytes(StandardCharsets.UTF_8))
                        .contentType("application/json")
                        .header(HttpHeaders.AUTHORIZATION, auth)
        ).andExpect(status().isBadRequest()).andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("ID.004");
    }

    @Test
    public void testChangePassword_BadRequest_WrongCurrentPass() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("fake-pass");
        request.setNewPassword("P@ssw0rdooo0");
        request.setConfirmPassword("P@ssw0rdooo0");
        String requestBody = this.serialize(request);
        String auth = String.format("%s %s", this.jwtConfig.getType(), this.generateValidAccessToken("uid-active-1"));

        MvcResult result = this.mockMvc.perform(
                put("/v1/profile/password")
                        .contentType("application/json")
                        .content(requestBody.getBytes(StandardCharsets.UTF_8))
                        .header(HttpHeaders.AUTHORIZATION, auth)
        ).andExpect(status().isBadRequest()).andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("ID.201");

    }

    @Test
    public void testChangePassword_Unauthorized() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("foobar");
        request.setNewPassword("some-new-pass");
        request.setConfirmPassword("some-new-pass");
        String requestBody = this.serialize(request);

        this.mockMvc.perform(
                put("/v1/profile/password")
                        .contentType("application/json")
                        .content(requestBody.getBytes(StandardCharsets.UTF_8))
        ).andExpect(status().isUnauthorized());
    }

    @Test
    public void testPatch_NoContent() throws Exception {
        String patchString = String.format(
                "[" +
                        "{\"op\": \"replace\", \"path\": \"/first_name\", \"value\": \"%s\"}," +
                        "{\"op\": \"add\", \"path\": \"/picture_id\", \"value\": \"%d\"}" +
                        "]",
                "Roman",
                1
        );
        String auth = String.format("%s %s", this.jwtConfig.getType(), this.generateValidAccessToken("uid-active-1"));

        this.mockMvc.perform(
                patch("/v1/profile")
                        .contentType("application/json")
                        .content(patchString.getBytes(StandardCharsets.UTF_8))
                        .header(HttpHeaders.AUTHORIZATION, auth)
        ).andExpect(status().isNoContent());
    }

    @Test
    public void testPatch_BadRequest() throws Exception {
        String patchString = String.format(
                "[" +
                        "{\"op\": \"replace\", \"path\": \"/first_name\", \"value\": \"%s\"}," +
                        "{\"op\": \"add\", \"path\": \"/picture_id\", \"value\": \"%d\"}" +
                        "]",
                "",
                1
        );
        String auth = String.format("%s %s", this.jwtConfig.getType(), this.generateValidAccessToken("uid-active-1"));

        this.mockMvc.perform(
                patch("/v1/profile")
                        .contentType("application/json")
                        .content(patchString.getBytes(StandardCharsets.UTF_8))
                        .header(HttpHeaders.AUTHORIZATION, auth)
        ).andExpect(status().isBadRequest());
    }

    @Test
    public void testPatch_Unauthorized() throws Exception {
        String patchString = String.format(
                "[" +
                        "{\"op\": \"replace\", \"path\": \"/first_name\", \"value\": \"%s\"}," +
                        "{\"op\": \"add\", \"path\": \"/picture_id\", \"value\": \"%d\"}" +
                        "]",
                "Roman",
                1
        );

        this.mockMvc.perform(
                patch("/v1/profile")
                        .contentType("application/json")
                        .content(patchString.getBytes(StandardCharsets.UTF_8))
        ).andExpect(status().isUnauthorized());
    }

    @Test
    public void testPatch_Forbidden() throws Exception {
        String patchString = String.format(
                "[" +
                        "{\"op\": \"replace\", \"path\": \"/first_name\", \"value\": \"%s\"}," +
                        "{\"op\": \"add\", \"path\": \"/picture_id\", \"value\": \"%d\"}" +
                        "]",
                "Roman",
                1
        );
        String auth = String.format("%s %s", this.jwtConfig.getType(), this.generateExpiredAccessToken("uid-active-1"));

        this.mockMvc.perform(
                patch("/v1/profile")
                        .contentType("application/json")
                        .content(patchString.getBytes(StandardCharsets.UTF_8))
                        .header(HttpHeaders.AUTHORIZATION, auth)
        ).andExpect(status().isForbidden());
    }

    @Test
    public void testGet() throws Exception {
        String auth = String.format("%s %s", this.jwtConfig.getType(), this.generateValidAccessToken("uid-active-1"));

        MvcResult result = this.mockMvc.perform(
                    get("/v1/profile")
                            .header(HttpHeaders.AUTHORIZATION, auth)
                )
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();

        assertThat(responseBody).contains("first_name");
        assertThat(responseBody).contains("last_name");
        assertThat(responseBody).contains("picture_url");
        assertThat(responseBody).contains("id");
    }

    @Test
    public void testGetPublicProfile() throws Exception {
        String auth = String.format("%s %s", this.jwtConfig.getType(), this.generateValidAccessToken("uid-active-1"));

        MvcResult result = this.mockMvc.perform(
                        get("/v1/profile/uid-active-2")
                                .header(HttpHeaders.AUTHORIZATION, auth)
                )
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();

        assertThat(responseBody).contains("first_name");
        assertThat(responseBody).contains("last_name");
        assertThat(responseBody).contains("id");
    }
}
