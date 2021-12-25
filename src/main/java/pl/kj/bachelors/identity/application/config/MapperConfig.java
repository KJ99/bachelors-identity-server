package pl.kj.bachelors.identity.application.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;
import pl.kj.bachelors.identity.application.dto.response.ProfileResponse;
import pl.kj.bachelors.identity.application.dto.response.PublicProfileResponse;
import pl.kj.bachelors.identity.application.dto.response.UploadedFileResponse;
import pl.kj.bachelors.identity.application.dto.response.UserVerificationResponse;
import pl.kj.bachelors.identity.application.dto.response.health.HealthCheckResponse;
import pl.kj.bachelors.identity.application.dto.response.health.SingleCheckResponse;
import pl.kj.bachelors.identity.application.model.HealthCheckResult;
import pl.kj.bachelors.identity.application.model.SingleCheckResult;
import pl.kj.bachelors.identity.domain.config.ApiConfig;
import pl.kj.bachelors.identity.domain.model.entity.UploadedFile;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.model.entity.UserVerification;
import pl.kj.bachelors.identity.domain.model.update.UserUpdateModel;

import java.util.stream.Collectors;

@Configuration
public class MapperConfig {
    private final ApiConfig config;

    @Autowired
    public MapperConfig(ApiConfig config) {
        this.config = config;
    }

    @Bean
    public ModelMapper mapper() {
        ModelMapper mapper = new ModelMapper();

        mapper.addMappings(new PropertyMap<SingleCheckResult, SingleCheckResponse>() {
            @Override
            protected void configure() {
                using(ctx -> ((SingleCheckResult) ctx.getSource()).isActive() ? "On" : "Off")
                        .map(source, destination.getStatus());
            }
        });

        mapper.addMappings(new PropertyMap<HealthCheckResult, HealthCheckResponse>() {
            @Override
            protected void configure() {
                using(ctx -> ((HealthCheckResult) ctx.getSource())
                        .getResults()
                        .stream()
                        .map(item -> mapper().map(item, SingleCheckResponse.class))
                        .collect(Collectors.toList())
                ).map(source, destination.getResults());
            }
        });

        mapper.addMappings(new PropertyMap<UploadedFile, UploadedFileResponse>() {
            @Override
            protected void configure() {
                map().setFileName(source.getOriginalFileName());
            }
        });

        mapper.addMappings(new PropertyMap<UserVerification, UserVerificationResponse>() {
            @Override
            protected void configure() {
                map().setVerificationToken(source.getToken());
            }
        });

        mapper.addMappings(new PropertyMap<User, ProfileResponse>() {
            @Override
            protected void configure() {
                map().setId(source.getUid());
                map().setUsername(source.getUserName());
                using(ctx -> {
                    User src = (User) ctx.getSource();
                    UriComponentsBuilder uriBuilder = UriComponentsBuilder
                            .newInstance()
                            .host(config.getHost())
                            .scheme("https");

                    return src.getPicture() != null
                            ? uriBuilder
                            .path("/v1/resources/{id}/download")
                            .buildAndExpand(src.getPicture().getId())
                            .toUriString()
                            : null;
                }).map(source, destination.getPictureUrl());

                using(ctx -> {
                    User src = (User) ctx.getSource();
                    return src.getPicture() != null ? src.getPicture().getId() : null;
                }).map(source, destination.getPictureId());
            }
        });

        mapper.addMappings(new PropertyMap<User, PublicProfileResponse>() {
            @Override
            protected void configure() {
                map().setId(source.getUid());
                using(ctx -> {
                    User src = (User) ctx.getSource();
                    UriComponentsBuilder uriBuilder = UriComponentsBuilder
                            .newInstance()
                            .host(config.getHost())
                            .scheme("https");

                    return src.getPicture() != null
                            ? uriBuilder
                            .path("/v1/resources/{id}/download")
                            .buildAndExpand(src.getPicture().getId())
                            .toUriString()
                            : null;
                }).map(source, destination.getPictureUrl());
            }
        });

        mapper.addMappings(new PropertyMap<User, UserUpdateModel>() {
            @Override
            protected void configure() {
                map().setUsername(source.getUserName());
                using(ctx -> {
                    User src = (User) ctx.getSource();
                    return src.getPicture() != null ? src.getPicture().getId() : null;
                }).map(source, destination.getPictureId());
            }
        });

        mapper.addMappings(new PropertyMap<UserUpdateModel, User>() {
            @Override
            protected void configure() {
                map().setUserName(source.getUsername());
                using(ctx -> {
                    UserUpdateModel src = (UserUpdateModel) ctx.getSource();
                    UploadedFile file = null;
                    if(src.getPictureId() != null) {
                        file = new UploadedFile();
                        file.setId(src.getPictureId());
                    }

                    return file;
                }).map(source, destination.getPicture());
            }
        });

        return mapper;
    }
}
