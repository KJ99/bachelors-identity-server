package pl.kj.bachelors.identity.infrastructure.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.kj.bachelors.identity.domain.service.file.FileUploader;
import pl.kj.bachelors.identity.infrastructure.service.FileUploadService;

@Configuration
public class FileServiceConfig {

    @Bean
    public FileUploader fileService(@Autowired FileUploadService implementation) {
        return implementation;
    }

}
