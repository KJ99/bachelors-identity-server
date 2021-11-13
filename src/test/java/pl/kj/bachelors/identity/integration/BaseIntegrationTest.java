package pl.kj.bachelors.identity.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultJwtBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import pl.kj.bachelors.identity.domain.config.JwtConfig;

import javax.crypto.spec.SecretKeySpec;
import java.util.Calendar;

@Sql(value = "/db.test/seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/db.test/clear.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class BaseIntegrationTest {
    @Autowired
    protected JwtConfig jwtConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    protected String generateValidAccessToken(String uid) {
        Calendar expiresAt = Calendar.getInstance();
        expiresAt.add(Calendar.HOUR, 10);

        return this.generateAccessToken(uid, expiresAt);
    }

    protected String generateExpiredAccessToken(String uid) {
        Calendar expiresAt = Calendar.getInstance();
        expiresAt.add(Calendar.HOUR, -10);

        return this.generateAccessToken(uid, expiresAt);
    }

    private String generateAccessToken(String uid, Calendar expiresAt) {
        SecretKeySpec spec = new SecretKeySpec(this.jwtConfig.getSecret().getBytes(), this.jwtConfig.getAlgorithm());

        DefaultJwtBuilder builder = new DefaultJwtBuilder();
        builder.setSubject(uid);
        builder.setExpiration(expiresAt.getTime());
        builder.signWith(SignatureAlgorithm.forName(this.jwtConfig.getAlgorithm()), spec);

        return builder.compact();
    }

    protected String serialize(Object model) {
        String json;
        try {
            json = this.objectMapper.writer().writeValueAsString(model);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            json = "";
        }

        return json;
    }
}
