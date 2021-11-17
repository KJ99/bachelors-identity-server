package pl.kj.bachelors.identity.application.config;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import pl.kj.bachelors.identity.infrastructure.config.GoogleStorageConfig;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@Configuration
public class StorageConfig {
    @Bean
    @Profile("local")
    public Storage storageLocal(GoogleStorageConfig config) throws IOException {
        Resource googleJson = new ClassPathResource(config.getLocalCredentialsPath());
        Credentials credentials = GoogleCredentials.fromStream(googleJson.getInputStream());
        StorageOptions.Builder storageBuilder = StorageOptions.newBuilder();
        storageBuilder.setCredentials(credentials);
        storageBuilder.setProjectId(config.getProjectId());

        return storageBuilder.build().getService();
    }

    @Bean
    @Profile("prod")
    public Storage storageProd() {
        return StorageOptions.newBuilder().build().getService();
    }

    @Bean
    @Profile("test")
    public Storage storage() {
        Storage storage = Mockito.mock(Storage.class);
        Blob blobMock = Mockito.mock(Blob.class);
        Mockito.when(blobMock.getContent()).thenReturn(new byte[10]);
        Bucket bucketMock = Mockito.mock(Bucket.class);
        Mockito.when(bucketMock.create(anyString(), any(byte[].class))).thenReturn(blobMock);

        Mockito.when(storage.get(anyString())).thenReturn(bucketMock);
        Mockito.when(storage.get(any(BlobId.class))).thenReturn(blobMock);

        return storage;
    }
}
