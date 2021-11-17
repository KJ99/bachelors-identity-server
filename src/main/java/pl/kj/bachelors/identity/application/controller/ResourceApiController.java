package pl.kj.bachelors.identity.application.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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
import pl.kj.bachelors.identity.application.dto.response.error.ValidationErrorResponse;
import pl.kj.bachelors.identity.domain.annotation.Authentication;
import pl.kj.bachelors.identity.domain.config.UploadConfig;
import pl.kj.bachelors.identity.domain.exception.AggregatedApiError;
import pl.kj.bachelors.identity.domain.exception.CredentialsNotFoundException;
import pl.kj.bachelors.identity.domain.exception.ResourceNotFoundException;
import pl.kj.bachelors.identity.domain.model.entity.UploadedFile;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.service.file.FileReader;
import pl.kj.bachelors.identity.domain.service.file.FileUploader;
import pl.kj.bachelors.identity.infrastructure.repository.UploadedFileRepository;

import java.io.IOException;

@RestController
@RequestMapping("/v1/resources")
@Tag(name = "Resources")
public class ResourceApiController extends BaseApiController {

    private final FileUploader fileUploadService;
    private final UploadedFileRepository uploadedFileRepository;
    private final UploadConfig config;
    private final FileReader fileReader;

    @Autowired
    ResourceApiController(
            FileUploader fileUploadService,
            UploadedFileRepository uploadedFileRepository,
            UploadConfig config,
            FileReader fileReader
    ) {
        this.fileUploadService = fileUploadService;
        this.uploadedFileRepository = uploadedFileRepository;
        this.config = config;
        this.fileReader = fileReader;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    @Authentication
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BasicCreatedResponse.class)
                    ),
                    description = "File uploaded successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<UploadedFileResponse> post(
            @RequestParam("file") final MultipartFile file
    ) throws IOException, CredentialsNotFoundException, AggregatedApiError {
        User user = this.getUser().orElseThrow(CredentialsNotFoundException::new);
        final UploadedFile resultEntity = this.fileUploadService.processUpload(
                file,
                this.config.getAllowedTypes(),
                this.config.getMaxSize()
        );

        this.uploadedFileRepository.save(resultEntity);

        this.logger.info(
                String.format("File with name %s (original: %s) was uploaded by user %s from address %s",
                        resultEntity.getFileName(),
                        resultEntity.getOriginalFileName(),
                        user.getUid(),
                        this.currentRequest.getRemoteAddr()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(this.map(resultEntity, UploadedFileResponse.class));
    }

    @GetMapping("/{id}")
    @ApiResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UploadedFileResponse.class)
            ),
            description = "File data"
    )
    public ResponseEntity<UploadedFileResponse> getParticular(@PathVariable("id") int id)
            throws ResourceNotFoundException {
        final UploadedFile uploadedFile = this.uploadedFileRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
        return ResponseEntity.ok(this.map(uploadedFile, UploadedFileResponse.class));
    }

    @GetMapping("/{id}/download")
    @Cacheable("files")
    public ResponseEntity<Resource> download(@PathVariable("id") Integer id)
            throws ResourceNotFoundException {
        final UploadedFile uploadedFile = this.uploadedFileRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);

        byte[] content = this.fileReader.readFile(uploadedFile);
        ByteArrayResource resource = new ByteArrayResource(content);

        this.logger.info(
                String.format("File with name %s was sent to address %s",
                        uploadedFile.getFileName(),
                        this.currentRequest.getRemoteAddr()
                )
        );

        return ResponseEntity
                .ok()
                .contentLength(resource.contentLength())
                .contentType(MediaType.parseMediaType(uploadedFile.getMediaType()))
                .body(resource);
    }
}
