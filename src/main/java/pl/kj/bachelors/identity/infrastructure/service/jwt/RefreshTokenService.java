package pl.kj.bachelors.identity.infrastructure.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.kj.bachelors.identity.domain.config.JwtConfig;
import pl.kj.bachelors.identity.domain.exception.JwtInvalidException;
import pl.kj.bachelors.identity.domain.model.AuthResult;
import pl.kj.bachelors.identity.domain.model.AuthResultDetail;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.model.payload.TokenAuthPayload;
import pl.kj.bachelors.identity.domain.service.jwt.TokenRefresher;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;

@Service
public class RefreshTokenService implements TokenRefresher {
    private final ParseJwtService parseService;
    private final JwtGenerationService generationService;
    private final UserRepository userRepository;
    private final JwtConfig jwtConfig;

    public RefreshTokenService(
            @Autowired ParseJwtService parseService,
            @Autowired JwtGenerationService generationService,
            @Autowired UserRepository userRepository,
            @Autowired JwtConfig jwtConfig) {
        this.parseService = parseService;
        this.generationService = generationService;
        this.userRepository = userRepository;
        this.jwtConfig = jwtConfig;
    }

    @Override
    public AuthResult<TokenAuthPayload> refreshToken(String accessToken, String refreshToken) {
        AuthResult<TokenAuthPayload> result = new AuthResult<>();
        try {
            this.parseService.validateToken(refreshToken);
            String uid = this.getUid(accessToken, refreshToken);
            User user = this.getUser(uid);
            TokenAuthPayload payload = new TokenAuthPayload();
            payload.setAccessToken(this.generationService.generateAccessToken(user));
            payload.setRefreshToken(this.generationService.generateRefreshToken(user));
            payload.setTokenType(this.jwtConfig.getType());

            result.setSuccess(true);
            result.setDetail(AuthResultDetail.SUCCESS);
            result.setPayload(payload);
        } catch (JwtInvalidException | NotFoundException | IllegalAccessException e) {
            result.setSuccess(false);
            result.setDetail(AuthResultDetail.INVALID_TOKEN);
        }

        return result;
    }

    private String getUid(String accessToken, String refreshToken) throws JwtInvalidException {
        String refreshUid = fetchUidFromToken(refreshToken);
        if(!fetchUidFromToken(accessToken).equals(refreshUid)) {
            throw new JwtInvalidException();
        }

        return refreshUid;
    }

    private User getUser(String uid) throws IllegalAccessException, NotFoundException {
        User user = this.userRepository.findById(uid).orElseThrow(() -> new NotFoundException("User not exists"));
        if(!user.isActive()) {
            throw new IllegalAccessException("Account is not active");
        }

        return user;
    }

    private String fetchUidFromToken(String token) {
        Claims claims;
        try {
            claims = this.parseService.parseClaims(token);
        } catch (ExpiredJwtException ex) {
            claims = ex.getClaims();
        }

        return claims.getSubject();
    }
}
