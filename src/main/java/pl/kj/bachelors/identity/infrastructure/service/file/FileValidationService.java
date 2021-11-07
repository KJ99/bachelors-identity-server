package pl.kj.bachelors.identity.infrastructure.service.file;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import pl.kj.bachelors.identity.application.exception.BadRequestHttpException;
import pl.kj.bachelors.identity.application.model.validation.ValidationViolation;
import pl.kj.bachelors.identity.domain.service.file.FileValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FileValidationService implements FileValidator {
    @Override
    public void ensureThatFileIsValid(
            final byte[] content,
            final String[] allowedMediaTypes,
            final int maxFileSize
    ) throws BadRequestHttpException {
        List<ValidationViolation> violations = new ArrayList<>();
        Tika tika = new Tika();
        String mediaType = tika.detect(content);
        if (!Arrays.asList(allowedMediaTypes).contains(mediaType)) {
            violations.add(new ValidationViolation("File type not allowed", "FILE.01", "file"));
        }
        if (content.length > maxFileSize) {
            violations.add(new ValidationViolation("File is too large",  "FILE.02", "file"));
        }

        if(violations.size() > 0) {
            var ex = new BadRequestHttpException();
            ex.setErrors(violations);

            throw ex;
        }
    }
}
