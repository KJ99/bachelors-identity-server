package pl.kj.bachelors.identity.infrastructure.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultJwtParser;
import io.jsonwebtoken.impl.crypto.DefaultJwtSignatureValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.kj.bachelors.identity.domain.config.JwtConfig;
import pl.kj.bachelors.identity.domain.exception.JwtInvalidException;
import pl.kj.bachelors.identity.domain.service.jwt.JwtParser;

import javax.crypto.spec.SecretKeySpec;

@Service
public class ParseJwtService implements JwtParser {
    private final JwtConfig config;

    @Autowired
    public ParseJwtService(JwtConfig config) {
        this.config = config;
    }

    @Override
    public void validateToken(String jwt) throws JwtInvalidException {
        String[] chunks = jwt.split("\\.");
        if(chunks.length < 3) {
            throw new JwtInvalidException();
        }

        this.checkSignature(chunks);
        this.parseClaims(jwt);

    }

    @Override
    public String getUid(String jwt) {
        var claims = this.parseClaims(jwt);

        return claims.getSubject();
    }

    @Override
    public Claims parseClaims(String jwt) {
        SecretKeySpec spec = new SecretKeySpec(this.config.getSecret().getBytes(), this.config.getAlgorithm());
        DefaultJwtParser parser = new DefaultJwtParser();
        parser.setSigningKey(spec);

        return parser.parseClaimsJws(jwt).getBody();
    }

    private void checkSignature(String[] tokenChunks) throws JwtInvalidException {
        SignatureAlgorithm algorithm = SignatureAlgorithm.forName(this.config.getAlgorithm());
        SecretKeySpec spec = new SecretKeySpec(this.config.getSecret().getBytes(), this.config.getAlgorithm());
        DefaultJwtSignatureValidator validator = new DefaultJwtSignatureValidator(algorithm, spec);

        String tokenWithoutSignature = String.join(".", tokenChunks[0], tokenChunks[1]);

        if(!validator.isValid(tokenWithoutSignature, tokenChunks[2])) {
            throw new JwtInvalidException();
        }
    }
}
