package pl.kj.bachelors.identity.application.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kj.bachelors.identity.application.dto.response.health.HealthCheckResponse;
import pl.kj.bachelors.identity.application.exception.NotAuthorizedHttpException;
import pl.kj.bachelors.identity.application.service.HealthCheckService;
import pl.kj.bachelors.identity.domain.annotation.Authentication;
import pl.kj.bachelors.identity.domain.config.ApiConfig;
import pl.kj.bachelors.identity.domain.exception.AccessDeniedException;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.security.action.Action;
import pl.kj.bachelors.identity.domain.service.ModelValidator;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;

@RestController
@RequestMapping(value = "/v1/health")
@Tag(name = "Health")
public class HealthCheckApiController extends BaseApiController {
    private final HealthCheckService healthCheckService;

    HealthCheckApiController(@Autowired HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponse(
            responseCode = "200",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = HealthCheckResponse.class)
            )
    )
    public ResponseEntity<HealthCheckResponse> getHealthCheck() {
        var report = this.healthCheckService.check();
        return ResponseEntity.ok(this.map(report, HealthCheckResponse.class));
    }
}