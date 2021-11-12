package pl.kj.bachelors.identity.application.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kj.bachelors.identity.domain.model.entity.User;

import java.util.Optional;

@RestController
@RequestMapping("/v1/ping")
public class PingApiController {

    @GetMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> ping() {
        return ResponseEntity.noContent().build();
    }

}
