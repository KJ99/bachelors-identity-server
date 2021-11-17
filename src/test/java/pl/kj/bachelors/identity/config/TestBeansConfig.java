package pl.kj.bachelors.identity.config;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@Configuration
public class TestBeansConfig {
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
