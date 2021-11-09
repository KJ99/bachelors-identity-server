package pl.kj.bachelors.identity.integration.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultJwtBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.kj.bachelors.identity.application.Application;
import pl.kj.bachelors.identity.domain.model.User;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;

@SpringBootTest
@ContextConfiguration(classes = {Application.class})
@AutoConfigureMockMvc
public class AuthenticationApiControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;


    private User loadUser() {
        User user = new User();
        user.setUid("uid-1");
        user.setUserName("username");
        user.setEmail("email@email.pl");
        user.setFirstName("Ab");
        user.setLastName("Ba");
        user.setPassword("pass");
        user.setSalt("salt");
        this.userRepository.saveAndFlush(user);

        return user;
    }
}
