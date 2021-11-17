package pl.kj.bachelors.identity.infrastructure.service.file;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import pl.kj.bachelors.identity.domain.exception.AggregatedApiError;
import pl.kj.bachelors.identity.domain.exception.ValidationViolation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FileValidationService {

    public void ensureThatFileIsValid(
            final byte[] content,
            final String[] allowedMediaTypes,
            final long maxFileSize
    ) throws AggregatedApiError {
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
            var ex = new AggregatedApiError();
            ex.setErrors(violations);

            throw ex;
        }
    }
}
