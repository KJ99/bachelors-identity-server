package pl.kj.bachelors.identity.infrastructure.service.registration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kj.bachelors.identity.application.exception.ConflictHttpException;
import pl.kj.bachelors.identity.application.model.validation.ValidationViolation;
import pl.kj.bachelors.identity.domain.service.registration.AccountRegistrationService;
import pl.kj.bachelors.identity.domain.config.ApiConfig;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;

@Service
public class AccountRegistrationServiceImpl implements AccountRegistrationService {
    private CreateUserService createUserService;
    private UserRepository userRepo;
    private ApiConfig apiConfig;

    public AccountRegistrationServiceImpl(
            @Autowired CreateUserService createUserService,
            @Autowired UserRepository userRepo,
            @Autowired ApiConfig apiConfig
    ) {
        this.createUserService = createUserService;
        this.userRepo = userRepo;
        this.apiConfig = apiConfig;
    }

    @Override
    @Transactional(rollbackFor = ConflictHttpException.class)
    public void registerAccount(String email, String username, String firstName, String lastName, String password) throws ConflictHttpException {
        var user = this.createUserService.createUser(email, username, firstName, lastName, password);
        this.userRepo.save(user);
        try {
            this.userRepo.flush();
        } catch (DataIntegrityViolationException ex) {
            throw createConflictException(ex);
        }
    }

    private ConflictHttpException createConflictException(DataIntegrityViolationException source) {
        String specificMessage = source.getMostSpecificCause().getMessage();

        String code = null;
        String path = null;
        if (specificMessage.contains("UN_EMAIL")) {
            code = "ID.012";
            path = "email";
        } else if (specificMessage.contains("UN_USERNAME")) {
            code = "ID.011";
            path = "username";
        }

        String message = this.apiConfig.getErrors().get(code);

        var ex = new ConflictHttpException();

        if(path != null) {
            ex.setError(new ValidationViolation(message, code, path));
        }

        return ex;
    }
}
