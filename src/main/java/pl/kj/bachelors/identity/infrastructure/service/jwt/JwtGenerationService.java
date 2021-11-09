package pl.kj.bachelors.identity.infrastructure.service.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultJwtBuilder;
import io.jsonwebtoken.impl.crypto.DefaultJwtSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.kj.bachelors.identity.domain.config.JwtConfig;
import pl.kj.bachelors.identity.domain.model.JwtClaims;
import pl.kj.bachelors.identity.domain.model.JwtHeader;
import pl.kj.bachelors.identity.domain.model.User;
import pl.kj.bachelors.identity.domain.service.jwt.JwtGenerator;

import javax.crypto.spec.SecretKeySpec;
import java.util.Calendar;

@Service
public class JwtGenerationService implements JwtGenerator {
    private final JwtConfig config;
    private final ObjectMapper objectMapper;

    public JwtGenerationService(@Autowired JwtConfig config, @Autowired ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    @Override
    public String generateJwt(User user) {
        Calendar issuedAt = Calendar.getInstance();
        Calendar expiresAt = (Calendar) issuedAt.clone();
        expiresAt.add(Calendar.MINUTE, (int) this.config.getValidTimeInMinutes());

        var builder = new DefaultJwtBuilder();
        builder.setHeaderParam("alg", this.config.getAlgorithm());
        builder.setHeaderParam("typ", "JWT");

        builder.setSubject(user.getUid());
        builder.setIssuedAt(issuedAt.getTime());
        builder.setExpiration(expiresAt.getTime());

        SignatureAlgorithm algorithm = SignatureAlgorithm.forName(config.getAlgorithm());
        SecretKeySpec spec = new SecretKeySpec(this.config.getSecret().getBytes(), config.getAlgorithm());

        builder.signWith(algorithm, spec);

        return builder.compact();
    }
}
