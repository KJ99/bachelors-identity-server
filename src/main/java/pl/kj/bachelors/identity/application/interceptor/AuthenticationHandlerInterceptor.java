package pl.kj.bachelors.identity.application.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import pl.kj.bachelors.identity.domain.annotation.Authentication;
import pl.kj.bachelors.identity.domain.config.ApiConfig;
import pl.kj.bachelors.identity.domain.exception.JwtInvalidException;
import pl.kj.bachelors.identity.domain.exception.ValidationViolation;
import pl.kj.bachelors.identity.domain.service.jwt.AccessTokenFetcher;
import pl.kj.bachelors.identity.domain.service.jwt.JwtParser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class AuthenticationHandlerInterceptor implements HandlerInterceptor {
    private final AccessTokenFetcher tokenFetcher;
    private final JwtParser parser;
    private final ApiConfig apiConfig;
    private final ObjectMapper objectMapper;
    private final Logger logger;

    @Autowired
    public AuthenticationHandlerInterceptor(
            AccessTokenFetcher tokenFetcher,
            JwtParser parser,
            ApiConfig apiConfig,
            ObjectMapper objectMapper
    ) {
        this.tokenFetcher = tokenFetcher;
        this.parser = parser;
        this.apiConfig = apiConfig;
        this.objectMapper = objectMapper;
        this.logger = LoggerFactory.getLogger(AuthenticationHandlerInterceptor.class);
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) throws Exception {
        boolean result;
        if(!(handler instanceof HandlerMethod)) {
            result = true;
        } else {
            Optional<Authentication> authValue = this.findAuthenticationStrategy(handler);
            result = authValue.isEmpty() || this.handleAuthentication(authValue.get(), request, response);
        }

        return result;
    }

    private Optional<Authentication> findAuthenticationStrategy(Object handler) {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Authentication authentication = handlerMethod.getMethodAnnotation(Authentication.class);
        if(authentication == null) {
            authentication = handlerMethod.getBeanType().getAnnotation(Authentication.class);
        }

        return Optional.ofNullable(authentication);
    }

    private boolean handleAuthentication(Authentication auth, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Optional<String> tokenValue = this.tokenFetcher.getAccessTokenFromRequest(request);

        return tokenValue.isPresent()
                ? this.handlePresentToken(tokenValue.get(), auth, request, response)
                : this.handleInvalidToken(auth, response, HttpStatus.UNAUTHORIZED, "");
    }

    private boolean handlePresentToken(
            String token,
            Authentication auth,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        boolean result;
        try {
            this.parser.validateToken(token);
            String uid = this.parser.getUid(token);
            request.setAttribute("uid", uid);
            this.logger.info(
                    String.format(
                            "User %s was successfully authenticated with access token from address %s",
                            uid,
                            request.getRemoteAddr()
                    )
            );
            result = true;
        } catch (Exception e) {
            result = this.handleInvalidToken(auth, response, HttpStatus.UNAUTHORIZED, "");
        }

        return result;
    }

    private boolean handleInvalidToken(
            Authentication auth,
            HttpServletResponse response,
            HttpStatus errorStatus,
            String responseContent
    )
            throws IOException {
        boolean result = !auth.required();
        if(!result) {
            response.setStatus(errorStatus.value());
            response.getWriter().write(responseContent);
            this.logger.info("Request was rejected due to failed authentication (HTTP code: %s)");
        }

        return result;
    }

    private String createTokenExpiredResponse() throws JsonProcessingException {
        String code = "ID.100";
        ValidationViolation violation = new ValidationViolation(apiConfig.getErrors().get(code), code, null);

        return this.objectMapper.writer().writeValueAsString(violation);
    }
}
