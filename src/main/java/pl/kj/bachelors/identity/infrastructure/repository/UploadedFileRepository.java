package pl.kj.bachelors.identity.infrastructure.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.kj.bachelors.identity.domain.model.UploadedFile;

@Repository
public interface UploadedFileRepository extends CrudRepository<UploadedFile, Integer> {
    @Override
    <S extends UploadedFile> S save(S entity);

}
