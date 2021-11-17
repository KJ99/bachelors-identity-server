package pl.kj.bachelors.identity.domain.service.file;

import pl.kj.bachelors.identity.domain.model.entity.UploadedFile;

public interface FileReader {
    byte[] readFile(UploadedFile uploadedFile);
}
