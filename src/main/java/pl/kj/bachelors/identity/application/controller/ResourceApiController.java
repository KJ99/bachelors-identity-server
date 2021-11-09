package pl.kj.bachelors.identity.application.controller;

import io.swagger.annotations.ApiOperation;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.kj.bachelors.identity.application.dto.response.BasicCreatedResponse;
import pl.kj.bachelors.identity.application.dto.response.UploadedFileResponse;
import pl.kj.bachelors.identity.application.exception.BadRequestHttpException;
import pl.kj.bachelors.identity.application.exception.NotFoundHttpException;
import pl.kj.bachelors.identity.domain.config.ApiConfig;
import pl.kj.bachelors.identity.domain.model.UploadedFile;
import pl.kj.bachelors.identity.domain.service.ModelValidator;
import pl.kj.bachelors.identity.domain.service.file.FileUploader;
import pl.kj.bachelors.identity.infrastructure.repository.UploadedFileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/v1/resources")
public class ResourceApiController extends BaseApiController {

    private final FileUploader fileUploadService;
    private final UploadedFileRepository uploadedFileRepository;
    private final int maxFileSize;
    private final String[] allowedMediaTypes;

    ResourceApiController(
            @Autowired ModelMapper mapper,
            @Value("spring.profiles.active") String activeProfile,
            @Autowired ApiConfig apiConfig,
            @Autowired FileUploader fileUploadService,
            @Autowired UploadedFileRepository uploadedFileRepository,
            @Value("${file-upload.max-size}") int maxFileSize,
            @Value("${file-upload.allowed-types}") String[] allowedMediaTypes,
            @Autowired ModelValidator validator
            ) {
        super(mapper, activeProfile, validator, apiConfig);
        this.fileUploadService = fileUploadService;
        this.uploadedFileRepository = uploadedFileRepository;
        this.maxFileSize = maxFileSize;
        this.allowedMediaTypes = allowedMediaTypes;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "Upload a file", code = 201, response = BasicCreatedResponse.class)
    @Transactional
    public ResponseEntity<?> post(
            @RequestParam("file") final MultipartFile file
    ) throws IOException, BadRequestHttpException {
        final UploadedFile resultEntity = this.fileUploadService.processUpload(
                file,
                this.allowedMediaTypes,
                this.maxFileSize
        );

        this.uploadedFileRepository.save(resultEntity);

        var response = new BasicCreatedResponse<>(resultEntity.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UploadedFileResponse> getParticular(@PathVariable("id") int id) throws NotFoundHttpException {
        final UploadedFile uploadedFile = this.uploadedFileRepository.findById(id).orElseThrow(NotFoundHttpException::new);
        return ResponseEntity.ok(this.map(uploadedFile, UploadedFileResponse.class));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable("id") Integer id) throws NotFoundHttpException, IOException {
        final UploadedFile uploadedFile = this.uploadedFileRepository.findById(id).orElseThrow(NotFoundHttpException::new);
        final Path path = Paths.get(uploadedFile.getDirectory(), uploadedFile.getFileName());
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

        return ResponseEntity
                .ok()
                .contentLength(resource.contentLength())
                .contentType(MediaType.parseMediaType(uploadedFile.getMediaType()))
                .body(resource);
    }
}
