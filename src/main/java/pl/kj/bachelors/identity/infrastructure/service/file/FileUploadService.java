package pl.kj.bachelors.identity.infrastructure.service.file;

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.kj.bachelors.identity.domain.config.UploadConfig;
import pl.kj.bachelors.identity.domain.exception.AggregatedApiError;
import pl.kj.bachelors.identity.domain.model.entity.UploadedFile;
import pl.kj.bachelors.identity.domain.service.file.FileUploader;
import pl.kj.bachelors.identity.domain.service.file.FileValidator;
import pl.kj.bachelors.identity.infrastructure.config.GoogleStorageConfig;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.UUID;

@Service
public class FileUploadService implements FileUploader {
    private final FileValidator validator;
    private final UploadConfig config;
    private final Storage storage;
    private GoogleStorageConfig storageConfig;

    @Autowired
    public FileUploadService(FileValidator validator, UploadConfig config, Storage storage, GoogleStorageConfig storageConfig) {
        this.validator = validator;
        this.config = config;
        this.storage = storage;
        this.storageConfig = storageConfig;
    }

    @Override
    public UploadedFile processUpload(
            final MultipartFile file,
            final String[] allowedMediaTypes,
            final long maxFileSize
    ) throws IOException, AggregatedApiError {
        this.validator.ensureThatFileIsValid(file.getBytes(), allowedMediaTypes, maxFileSize);

        String fileName = this.generateFileName();

        Bucket bucket = this.storage.get(this.storageConfig.getBucketName());
        bucket.create(fileName, file.getBytes());

        return this.createUploadedFile(file, fileName);
    }

    private UploadedFile createUploadedFile(final MultipartFile file, final String fileName) throws IOException {
        Tika tika = new Tika();

        var uploadedFile = new UploadedFile();
        uploadedFile.setDirectory(this.config.getDestinationDir());
        uploadedFile.setFileName(fileName);
        uploadedFile.setOriginalFileName(file.getOriginalFilename());
        uploadedFile.setMediaType(tika.detect(file.getBytes()));

        return uploadedFile;
    }

    private String generateFileName() {
        String result;
        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        long randomNumber = Double.valueOf(Math.random() * 1000000).longValue();
        String content = String.valueOf(timeInMillis).concat("_").concat(String.valueOf(randomNumber));
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(content.getBytes(StandardCharsets.UTF_8));
            result = DatatypeConverter.printHexBinary(md5.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            result = content;
        }

        return result;
    }
}
