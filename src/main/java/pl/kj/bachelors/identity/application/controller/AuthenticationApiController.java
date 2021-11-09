package pl.kj.bachelors.identity.application.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kj.bachelors.identity.domain.config.ApiConfig;
import pl.kj.bachelors.identity.domain.service.ModelValidator;

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
