package pl.kj.bachelors.identity.application.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.kj.bachelors.identity.application.dto.response.health.HealthCheckResponse;
import pl.kj.bachelors.identity.application.service.HealthCheckService;

@RestController
@RequestMapping(value = "/v1/health")
public class HealthCheckApiController extends BaseApiController {
    private HealthCheckService healthCheckService;

    HealthCheckApiController(
            @Autowired ModelMapper mapper,
            @Value("spring.profiles.active") String activeProfile,
            @Autowired HealthCheckService healthCheckService
    ) {
        super(mapper, activeProfile);
        this.healthCheckService = healthCheckService;
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<HealthCheckResponse> getHealthCheck() {
        var report = this.healthCheckService.check();
        return ResponseEntity.ok(this.map(report, HealthCheckResponse.class));
    }
}