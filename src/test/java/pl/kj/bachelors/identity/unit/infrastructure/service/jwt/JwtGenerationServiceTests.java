package pl.kj.bachelors.identity.unit.infrastructure.service.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.impl.DefaultJwtParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pl.kj.bachelors.identity.application.Application;
import pl.kj.bachelors.identity.domain.model.JwtClaims;
import pl.kj.bachelors.identity.domain.model.User;
import pl.kj.bachelors.identity.infrastructure.service.jwt.JwtGenerationService;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = {Application.class})
public class JwtGenerationServiceTests {
    @Autowired
    private JwtGenerationService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGenerateJwt() throws IOException {
        User user = this.getUser();

        String token = service.generateJwt(user);
        String[] chunks = token.split("\\.");
        assertThat(chunks).hasSize(3);
        String json = new String(Base64.getDecoder().decode(chunks[1].getBytes()));
        JwtClaims claims = this.objectMapper.readValue(json, JwtClaims.class);
        long currentTimeInSeconds = Calendar.getInstance().getTimeInMillis() / 1000;
        assertThat(claims.getSub()).isEqualTo(user.getUid());
        assertThat(claims.getExp()).isGreaterThan(currentTimeInSeconds);
    }

    private User getUser() {
        var user = new User();
        user.setUid("uid-1");
        user.setEmail("user@user.user");
        user.setUserName("user1");
        user.setFirstName("Ab");
        user.setLastName("Ba");
        user.setPassword("pass");
        user.setSalt("salt");

        return user;
    }
}
