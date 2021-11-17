package pl.kj.bachelors.identity.infrastructure.service.file;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.kj.bachelors.identity.domain.model.entity.UploadedFile;
import pl.kj.bachelors.identity.domain.service.file.FileReader;
import pl.kj.bachelors.identity.infrastructure.config.GoogleStorageConfig;

@Service
public class FileReadService implements FileReader {
    private final Storage storage;
    private final GoogleStorageConfig config;

    @Autowired
    public FileReadService(Storage storage, GoogleStorageConfig config) {
        this.storage = storage;
        this.config = config;
    }

    @Override
    public byte[] readFile(UploadedFile uploadedFile) {
        BlobId blobId = BlobId.of(this.config.getBucketName(), uploadedFile.getFileName());
        Blob blob = this.storage.get(blobId);

        return blob.getContent();
    }
}
