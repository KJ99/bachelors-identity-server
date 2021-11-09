package pl.kj.bachelors.identity.unit.infrastructure.service.jwt;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultJwtBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pl.kj.bachelors.identity.application.Application;
import pl.kj.bachelors.identity.domain.config.JwtConfig;
import pl.kj.bachelors.identity.domain.model.AuthResult;
import pl.kj.bachelors.identity.domain.model.AuthResultDetail;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.model.payload.TokenAuthPayload;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;
import pl.kj.bachelors.identity.infrastructure.service.jwt.RefreshTokenService;

import javax.crypto.spec.SecretKeySpec;
import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = { Application.class })
public class RefreshTokenServiceTests {
    @Autowired
    private RefreshTokenService service;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testRefreshToken_Correct() {
        Calendar accessExpiresAt = Calendar.getInstance();
        Calendar refreshExpiresAt = Calendar.getInstance();
        accessExpiresAt.add(Calendar.HOUR, -1);
        refreshExpiresAt.add(Calendar.HOUR, 1);
        var user = this.loadUser(true);
        String access = this.getJwt(accessExpiresAt,user.getUid());
        String refresh = this.getJwt(refreshExpiresAt, user.getUid());

        AuthResult<TokenAuthPayload> result = this.service.refreshToken(access, refresh);

        assertThat(result.getDetail()).isEqualTo(AuthResultDetail.SUCCESS);
        assertThat(result.getPayload()).isNotNull();
        assertThat(result.getPayload().getTokenType()).isEqualTo(this.jwtConfig.getType());
        assertThat(result.getPayload().getAccessToken())
                .isNotEmpty()
                .isNotEqualTo(access);
        assertThat(result.getPayload().getRefreshToken())
                .isNotEmpty()
                .isNotEqualTo(refresh);
    }

    @Test
    public void testRefreshToken_InconsistentUids() {
        Calendar accessExpiresAt = Calendar.getInstance();
        Calendar refreshExpiresAt = Calendar.getInstance();
        accessExpiresAt.add(Calendar.HOUR, -1);
        refreshExpiresAt.add(Calendar.HOUR, 1);
        var user = this.loadUser(false);
        String access = this.getJwt(accessExpiresAt,user.getUid());
        String refresh = this.getJwt(refreshExpiresAt,user.getUid());

        AuthResult<TokenAuthPayload> result = this.service.refreshToken(access, refresh);

        assertThat(result.getDetail()).isEqualTo(AuthResultDetail.INVALID_TOKEN);
        assertThat(result.getPayload()).isNull();
    }

    @Test
    public void testRefreshToken_InactiveUser() {
        Calendar accessExpiresAt = Calendar.getInstance();
        Calendar refreshExpiresAt = Calendar.getInstance();
        accessExpiresAt.add(Calendar.HOUR, -1);
        refreshExpiresAt.add(Calendar.HOUR, 1);

        String access = this.getJwt(accessExpiresAt,"uid-1");
        String refresh = this.getJwt(refreshExpiresAt,"uid-2");

        AuthResult<TokenAuthPayload> result = this.service.refreshToken(access, refresh);

        assertThat(result.getDetail()).isEqualTo(AuthResultDetail.INVALID_TOKEN);
        assertThat(result.getPayload()).isNull();
    }

    private String getJwt(Calendar expiresAt, String uid) {
        SecretKeySpec spec = new SecretKeySpec(this.jwtConfig.getSecret().getBytes(), this.jwtConfig.getAlgorithm());

        DefaultJwtBuilder builder = new DefaultJwtBuilder();
        builder.setSubject(uid);
        builder.setExpiration(expiresAt.getTime());
        builder.signWith(SignatureAlgorithm.forName(this.jwtConfig.getAlgorithm()), spec);

        return builder.compact();
    }

    private User loadUser(boolean active) {
        User user = new User();
        user.setUid("uid-1");
        user.setUserName("username");
        user.setEmail("email@email.pl");
        user.setFirstName("Ab");
        user.setLastName("Ba");
        user.setPassword("pass");
        user.setSalt("salt");
        user.setActive(active);
        this.userRepository.saveAndFlush(user);

        return user;
    }
}
