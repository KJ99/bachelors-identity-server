package pl.kj.bachelors.identity.domain.service.jwt;
import pl.kj.bachelors.identity.domain.exception.JwtInvalidException;

public interface JwtVerifier {
    void validateToken(String jwt) throws JwtInvalidException;
    String getUid(String jwt);
}
