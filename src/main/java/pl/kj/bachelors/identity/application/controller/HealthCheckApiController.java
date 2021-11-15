package pl.kj.bachelors.identity.application.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kj.bachelors.identity.application.dto.response.health.HealthCheckResponse;
import pl.kj.bachelors.identity.application.service.HealthCheckService;

@RestController
@RequestMapping(value = "/v1/health")
@Tag(name = "Health")
public class HealthCheckApiController extends BaseApiController {
    private final HealthCheckService healthCheckService;

    @Autowired
    HealthCheckApiController(HealthCheckService healthCheckService) {
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