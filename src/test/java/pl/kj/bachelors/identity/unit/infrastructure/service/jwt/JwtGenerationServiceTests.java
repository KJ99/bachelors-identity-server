package pl.kj.bachelors.identity.unit.infrastructure.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultJwtParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.kj.bachelors.identity.BaseTest;
import pl.kj.bachelors.identity.domain.config.JwtConfig;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;
import pl.kj.bachelors.identity.infrastructure.service.jwt.JwtGenerationService;

import javax.crypto.spec.SecretKeySpec;
import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtGenerationServiceTests extends BaseTest {
    @Autowired
    private JwtGenerationService service;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testGenerateAccessToken() {
        User user = this.userRepository.findById("uid-active-1").orElseThrow();

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
        User user = this.userRepository.findById("uid-active-1").orElseThrow();

        String token = this.service.generateRefreshToken(user);
        String[] chunks = token.split("\\.");
        assertThat(chunks).hasSize(3);
        Claims claims = this.getTokenClaims(token);
        assertThat(claims.getSubject()).isEqualTo(user.getUid());
        assertThat(claims.getExpiration()).isNull();
    }

    private Claims getTokenClaims(String jwt) {
        DefaultJwtParser parser = new DefaultJwtParser();
        parser.setSigningKey(new SecretKeySpec(this.jwtConfig.getSecret().getBytes(), this.jwtConfig.getAlgorithm()));
        return parser.parseClaimsJws(jwt).getBody();
    }
}
