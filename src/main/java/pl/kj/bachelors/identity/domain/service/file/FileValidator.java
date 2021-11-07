package pl.kj.bachelors.identity.domain.service.file;

import pl.kj.bachelors.identity.application.exception.BadRequestHttpException;

public interface FileValidator {
    void ensureThatFileIsValid(
            final byte[] content,
            final String[] allowedMediaTypes,
            final int maxFileSize
    ) throws BadRequestHttpException;
}
