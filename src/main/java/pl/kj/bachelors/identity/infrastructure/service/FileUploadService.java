package pl.kj.bachelors.identity.infrastructure.service;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.kj.bachelors.identity.application.exception.BadRequestHttpException;
import pl.kj.bachelors.identity.domain.model.UploadedFile;
import pl.kj.bachelors.identity.domain.service.file.FileUploader;
import pl.kj.bachelors.identity.domain.service.file.FileValidator;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

@Service
public class FileUploadService implements FileUploader {
    private final FileValidator validator;
    private final String destinationDirectory;

    public FileUploadService(
            @Autowired FileValidator validator,
            @Value("${file-upload.destination-dir}") String destinationDirectory
    ) {
        this.validator = validator;
        this.destinationDirectory = destinationDirectory;
    }

    @Override
    public UploadedFile processUpload(
            final MultipartFile file,
            final String[] allowedMediaTypes,
            final int maxFileSize
    ) throws IOException, BadRequestHttpException {
        this.validator.ensureThatFileIsValid(file.getBytes(), allowedMediaTypes, maxFileSize);

        String fileName = this.generateFileName();
        Path path = Paths.get(this.destinationDirectory, fileName);
        Files.write(path, file.getBytes());

        return this.createUploadedFile(file, fileName);
    }

    private UploadedFile createUploadedFile(final MultipartFile file, final String fileName) throws IOException {
        Tika tika = new Tika();

        var uploadedFile = new UploadedFile();
        uploadedFile.setDirectory(this.destinationDirectory);
        uploadedFile.setFileName(fileName);
        uploadedFile.setOriginalFileName(file.getOriginalFilename());
        uploadedFile.setMediaType(tika.detect(file.getBytes()));

        return uploadedFile;
    }

    private String generateFileName() {
        String result = null;
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
