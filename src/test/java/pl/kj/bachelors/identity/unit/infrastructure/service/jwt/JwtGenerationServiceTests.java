package pl.kj.bachelors.identity.unit.infrastructure.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultJwtParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pl.kj.bachelors.identity.application.Application;
import pl.kj.bachelors.identity.domain.config.JwtConfig;
import pl.kj.bachelors.identity.domain.model.User;
import pl.kj.bachelors.identity.infrastructure.service.jwt.JwtGenerationService;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = {Application.class})
public class JwtGenerationServiceTests {
    @Autowired
    private JwtGenerationService service;

    @Autowired
    private JwtConfig jwtConfig;

    @Test
    public void testGenerateAccessToken() {
        User user = this.getUser();

        String token = this.service.generateAccessToken(user);
        String[] chunks = token.split("\\.");
        assertThat(chunks).hasSize(3);
        Claims claims = this.getTokenClaims(token);
        long currentTimeInSeconds = Calendar.getInstance().getTimeInMillis() / 1000;
        assertThat(claims.getSubject()).isEqualTo(user.getUid());
        assertThat(claims.getExpiration().getTime()).isGreaterThan(currentTimeInSeconds);
    }

    @Test
    public void testGenerateRefreshToken() {
        User user = this.getUser();

        String token = this.service.generateRefreshToken(user);
        String[] chunks = token.split("\\.");
        assertThat(chunks).hasSize(3);
        Claims claims = this.getTokenClaims(token);
        assertThat(claims.getSubject()).isEqualTo(user.getUid());
        assertThat(claims.getExpiration()).isNull();
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

    private Claims getTokenClaims(String jwt) {
        DefaultJwtParser parser = new DefaultJwtParser();
        parser.setSigningKey(new SecretKeySpec(this.jwtConfig.getSecret().getBytes(), this.jwtConfig.getAlgorithm()));
        return parser.parseClaimsJws(jwt).getBody();
    }
}
