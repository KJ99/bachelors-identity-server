package pl.kj.bachelors.identity.application.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kj.bachelors.identity.application.dto.request.ChangePasswordRequest;
import pl.kj.bachelors.identity.application.dto.response.ProfileResponse;
import pl.kj.bachelors.identity.application.dto.response.PublicProfileResponse;
import pl.kj.bachelors.identity.application.dto.response.error.ValidationErrorResponse;
import pl.kj.bachelors.identity.application.exception.BadRequestHttpException;
import pl.kj.bachelors.identity.application.exception.NotAuthorizedHttpException;
import pl.kj.bachelors.identity.application.exception.NotFoundHttpException;
import pl.kj.bachelors.identity.domain.annotation.Authentication;
import pl.kj.bachelors.identity.domain.exception.AggregatedApiError;
import pl.kj.bachelors.identity.domain.exception.ValidationViolation;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.model.update.UserUpdateModel;
import pl.kj.bachelors.identity.domain.service.auth.PasswordUpdateService;
import pl.kj.bachelors.identity.domain.service.update.UserUpdateService;

@RestController
@RequestMapping("/v1/profile")
@Authentication
@Tag(name = "Profile")
public class ProfileApiController extends BaseApiController {
    private final PasswordUpdateService passwordUpdateService;
    private final UserUpdateService userUpdateService;

    @Autowired
    public ProfileApiController(PasswordUpdateService passwordUpdateService, UserUpdateService userUpdateService) {
        this.passwordUpdateService = passwordUpdateService;
        this.userUpdateService = userUpdateService;
    }

    @PutMapping("/password")
    @SecurityRequirement(name = "JWT")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    content = @Content(schema = @Schema(hidden = true)),
                    description = "User's password changed successfully"),
            @ApiResponse(
                    responseCode = "400",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ValidationErrorResponse.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request)
            throws BadRequestHttpException, NotAuthorizedHttpException, ValidationViolation {
        this.ensureThatModelIsValid(request);
        User user = this.getUser().orElseThrow(NotAuthorizedHttpException::new);

        this.passwordUpdateService.updatePassword(
                user,
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        return ResponseEntity.noContent().build();
    }

    @PatchMapping
    @SecurityRequirement(name = "JWT")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    content = @Content(schema = @Schema(hidden = true)),
                    description = "User updated successfully"),
            @ApiResponse(
                    responseCode = "400",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ValidationErrorResponse.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<?> patch(@RequestBody JsonPatch jsonPatch)
            throws NotAuthorizedHttpException, JsonPatchException, AggregatedApiError, JsonProcessingException {
        User user = this.getUser().orElseThrow(NotAuthorizedHttpException::new);
        this.userUpdateService.processUpdate(user, jsonPatch, UserUpdateModel.class);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @SecurityRequirement(name = "JWT")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProfileResponse.class)
                    ),
                    description = "Current user's profile"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ProfileResponse> get() throws NotAuthorizedHttpException {
        User user = this.getUser().orElseThrow(NotAuthorizedHttpException::new);

        return ResponseEntity.ok(this.map(user, ProfileResponse.class));
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "JWT")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PublicProfileResponse.class)
                    ),
                    description = "User's public profile"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<PublicProfileResponse> getPublicProfile(@PathVariable("id") String id) throws NotFoundHttpException {
        User user = this.userRepository.findById(id).orElseThrow(NotFoundHttpException::new);

        return ResponseEntity.ok(this.map(user, PublicProfileResponse.class));
    }
}
