package pl.kj.bachelors.identity.application.controller;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kj.bachelors.identity.application.dto.request.ChangePasswordRequest;
import pl.kj.bachelors.identity.application.exception.BadRequestHttpException;
import pl.kj.bachelors.identity.application.exception.NotAuthorizedHttpException;
import pl.kj.bachelors.identity.domain.annotation.Authentication;
import pl.kj.bachelors.identity.domain.exception.ValidationViolation;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.service.auth.PasswordUpdateService;

@RestController
@RequestMapping("/v1/profile")
@Authentication
public class ProfileApiController extends BaseApiController {
    private final PasswordUpdateService passwordUpdateService;

    @Autowired
    public ProfileApiController(PasswordUpdateService passwordUpdateService) {
        this.passwordUpdateService = passwordUpdateService;
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request)
            throws BadRequestHttpException, NotAuthorizedHttpException, ValidationViolation {
        this.ensureThatModelIsValid(request);
        User user = this.getUser().orElseThrow(NotAuthorizedHttpException::new);

        this.passwordUpdateService.updatePassword(
                user,
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        return ResponseEntity.noContent().build();
    }
}
