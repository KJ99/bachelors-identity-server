package pl.kj.bachelors.identity.domain.service.file;

import pl.kj.bachelors.identity.domain.exception.AggregatedApiError;

public interface FileValidator {
    void ensureThatFileIsValid(
            final byte[] content,
            final String[] allowedMediaTypes,
            final long maxFileSize
    ) throws AggregatedApiError;
}
