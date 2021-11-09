package pl.kj.bachelors.identity.domain.service.jwt;
import io.jsonwebtoken.Claims;
import pl.kj.bachelors.identity.domain.exception.JwtInvalidException;

public interface JwtParser {
    void validateToken(String jwt) throws JwtInvalidException;
    String getUid(String jwt);
    Claims parseClaims(String jwt);
}
