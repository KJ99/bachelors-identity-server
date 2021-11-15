package pl.kj.bachelors.identity.domain.service.file;

import org.springframework.web.multipart.MultipartFile;
import pl.kj.bachelors.identity.domain.exception.AggregatedApiError;
import pl.kj.bachelors.identity.domain.model.entity.UploadedFile;

import java.io.IOException;

public interface FileUploader {

    UploadedFile processUpload(
            final MultipartFile file,
            final String[] allowedMediaTypes,
            final long maxFileSize
    ) throws IOException, AggregatedApiError;
}
