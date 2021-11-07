package pl.kj.bachelors.identity.integration.application.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.kj.bachelors.identity.application.Application;
import pl.kj.bachelors.identity.application.controller.AccountApiController;
import pl.kj.bachelors.identity.domain.model.User;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        mockMvc.perform(
                post("/v1/account")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isCreated());
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
}
