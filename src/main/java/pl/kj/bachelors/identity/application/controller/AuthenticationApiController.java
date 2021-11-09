package pl.kj.bachelors.identity.application.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kj.bachelors.identity.application.exception.NotFoundHttpException;
import pl.kj.bachelors.identity.domain.config.ApiConfig;
import pl.kj.bachelors.identity.domain.exception.JwtInvalidException;
import pl.kj.bachelors.identity.domain.model.User;
import pl.kj.bachelors.identity.domain.service.ModelValidator;
import pl.kj.bachelors.identity.domain.service.jwt.JwtGenerator;
import pl.kj.bachelors.identity.domain.service.jwt.JwtHttpManager;
import pl.kj.bachelors.identity.domain.service.jwt.JwtVerifier;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/v1/auth")
public class AuthenticationApiController extends BaseApiController {

    AuthenticationApiController(
            @Autowired ModelMapper mapper,
            @Value("${spring.profiles.active}") String activeProfile,
            @Autowired ModelValidator validator,
            @Autowired ApiConfig apiConfig) {
        super(mapper, activeProfile, validator, apiConfig);
    }
}
