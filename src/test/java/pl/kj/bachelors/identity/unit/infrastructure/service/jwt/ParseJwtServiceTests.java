package pl.kj.bachelors.identity.unit.infrastructure.service.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultJwtBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.kj.bachelors.identity.BaseTest;
import pl.kj.bachelors.identity.domain.config.JwtConfig;
import pl.kj.bachelors.identity.domain.exception.JwtInvalidException;
import pl.kj.bachelors.identity.infrastructure.service.jwt.ParseJwtService;

import javax.crypto.spec.SecretKeySpec;
import java.util.Calendar;

import static org.assertj.core.api.Assertions.*;

public class ParseJwtServiceTests extends BaseTest {
    @Autowired
    private ParseJwtService service;

    @Autowired
    private JwtConfig jwtConfig;

    @Test
    public void testGetUid() {
        Calendar expiresAt = Calendar.getInstance();
        expiresAt.add(Calendar.HOUR, 1);
        String jwt = this.getJwt(expiresAt, this.jwtConfig.getSecret());

        String uid = this.service.getUid(jwt);

        assertThat(uid).isEqualTo("uid-1");
    }

    @Test
    public void testValidateToken_Correct() {
        Calendar expiresAt = Calendar.getInstance();
        expiresAt.add(Calendar.HOUR, 1);
        final String jwt = this.getJwt(expiresAt, this.jwtConfig.getSecret());

        Throwable thrown = catchThrowable(() -> this.service.validateToken(jwt));

        assertThat(thrown).isNull();
    }

    @Test
    public void testValidateToken_Invalid() {
        Calendar expiresAt = Calendar.getInstance();
        expiresAt.add(Calendar.HOUR, 100);
        final String jwt = this.getJwt(expiresAt, "fake-secret");

        assertThatThrownBy(() -> this.service.validateToken(jwt))
                .isInstanceOf(JwtInvalidException.class);
    }

    @Test
    public void testValidateToken_Expired() {
        Calendar expiresAt = Calendar.getInstance();
        expiresAt.add(Calendar.HOUR, -100);
        final String jwt = this.getJwt(expiresAt, this.jwtConfig.getSecret());

        assertThatThrownBy(() -> this.service.validateToken(jwt))
                .isInstanceOf(ExpiredJwtException.class);
    }

    private String getJwt(Calendar expiresAt, String secret) {
        SecretKeySpec spec = new SecretKeySpec(secret.getBytes(), this.jwtConfig.getAlgorithm());

        DefaultJwtBuilder builder = new DefaultJwtBuilder();
        builder.setSubject("uid-1");
        builder.setExpiration(expiresAt.getTime());
        builder.signWith(SignatureAlgorithm.forName(this.jwtConfig.getAlgorithm()), spec);

        return builder.compact();
    }
}
