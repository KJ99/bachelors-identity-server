package pl.kj.bachelors.identity.infrastructure.service.update;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.kj.bachelors.identity.domain.model.entity.UploadedFile;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.model.update.UserUpdateModel;
import pl.kj.bachelors.identity.domain.service.update.UserUpdateService;
import pl.kj.bachelors.identity.infrastructure.repository.UploadedFileRepository;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;
import pl.kj.bachelors.identity.infrastructure.service.ValidationService;

@Service
public class UserUpdateServiceImpl extends BaseEntityUpdateService<User, String, UserUpdateModel, UserRepository> implements UserUpdateService {
    private final UploadedFileRepository uploadedFileRepository;

    @Autowired
    protected UserUpdateServiceImpl(
            UserRepository repository,
            ValidationService validationService,
            ModelMapper modelMapper,
            ObjectMapper objectMapper,
            UploadedFileRepository uploadedFileRepository) {
        super(repository, validationService, modelMapper, objectMapper);
        this.uploadedFileRepository = uploadedFileRepository;
    }
}
