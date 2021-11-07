package pl.kj.bachelors.identity.application.controller;

import org.apache.commons.lang3.NotImplementedException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kj.bachelors.identity.application.dto.request.RegistrationRequest;
import pl.kj.bachelors.identity.application.exception.BadRequestHttpException;
import pl.kj.bachelors.identity.application.exception.ConflictHttpException;
import pl.kj.bachelors.identity.domain.service.ModelValidator;
import pl.kj.bachelors.identity.domain.service.registration.AccountRegistrationService;

@RestController
@RequestMapping("/v1/account")
public class AccountApiController extends BaseApiController {
    private final AccountRegistrationService service;
    AccountApiController(
            @Autowired ModelMapper mapper,
            @Value("spring.profiles.active") String activeProfile,
            @Autowired ModelValidator validator,
            @Autowired AccountRegistrationService service
    ) {
        super(mapper, activeProfile, validator);
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody RegistrationRequest request)
            throws BadRequestHttpException, ConflictHttpException {
        this.ensureThatModelIsValid(request);
        this.service.registerAccount(
                request.getEmail(),
                request.getUsername(),
                request.getFirstName(),
                request.getLastName(),
                request.getPassword()
        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
