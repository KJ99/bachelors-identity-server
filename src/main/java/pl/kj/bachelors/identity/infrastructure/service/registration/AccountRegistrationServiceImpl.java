package pl.kj.bachelors.identity.infrastructure.service.registration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kj.bachelors.identity.domain.model.entity.UserVerification;
import pl.kj.bachelors.identity.domain.service.registration.AccountRegistrationService;
import pl.kj.bachelors.identity.domain.config.ApiConfig;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;
import pl.kj.bachelors.identity.infrastructure.repository.UserVerificationRepository;

import java.util.concurrent.ExecutionException;

@Service
public class AccountRegistrationServiceImpl implements AccountRegistrationService {
    private final CreateUserService createUserService;
    private final UserRepository userRepo;
    private final ApiConfig apiConfig;
    private final AccountVerificationService verificationService;
    private final UserVerificationRepository verificationRepository;

    public AccountRegistrationServiceImpl(
            @Autowired CreateUserService createUserService,
            @Autowired UserRepository userRepo,
            @Autowired ApiConfig apiConfig,
            @Autowired AccountVerificationService verificationService,
            @Autowired UserVerificationRepository verificationRepository) {
        this.createUserService = createUserService;
        this.userRepo = userRepo;
        this.apiConfig = apiConfig;
        this.verificationService = verificationService;
        this.verificationRepository = verificationRepository;
    }

    @Override
    @Transactional(rollbackFor = DataIntegrityViolationException.class)
    public UserVerification registerAccount(String email, String username, String firstName, String lastName, String password) throws ExecutionException, InterruptedException {
        var user = this.createUserService.createUser(email, username, firstName, lastName, password);

        this.userRepo.save(user);

        UserVerification verification = this.verificationService.createVerification(user);

        this.verificationRepository.save(verification);

        return verification;
    }
}
