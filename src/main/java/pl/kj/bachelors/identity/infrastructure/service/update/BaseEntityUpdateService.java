package pl.kj.bachelors.identity.infrastructure.service.update;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.kj.bachelors.identity.domain.exception.AggregatedApiError;
import pl.kj.bachelors.identity.domain.exception.ValidationViolation;
import pl.kj.bachelors.identity.infrastructure.service.ValidationService;

import java.lang.reflect.Field;
import java.util.Collection;

public abstract class BaseEntityUpdateService<T, PK, U, R extends JpaRepository<T, PK>> {
    protected final R repository;
    protected final ValidationService validationService;
    protected final ModelMapper modelMapper;
    protected final ObjectMapper objectMapper;

    protected BaseEntityUpdateService(
            R repository,
            ValidationService validationService,
            ModelMapper modelMapper,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.validationService = validationService;
        this.modelMapper = modelMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional(rollbackFor = ValidationViolation.class)
    public void processUpdate(T original, JsonPatch patch, Class<U> updateModelClass)
            throws JsonPatchException, JsonProcessingException, AggregatedApiError {
        U modelFromOriginal = this.modelMapper.map(original, updateModelClass);
        JsonNode patched = patch.apply(objectMapper.convertValue(modelFromOriginal, JsonNode.class));

        U updateModel = this.objectMapper.treeToValue(patched, updateModelClass);
        this.ensureThatModelIsValid(updateModel);

        this.applyUpdateModel(original, updateModel);
        this.repository.save(original);
    }

    private <V> void ensureThatModelIsValid(V model) throws AggregatedApiError {
        Collection<ValidationViolation> violations = this.validationService.validateModel(model);
        if(violations.size() > 0) {
            AggregatedApiError ex = new AggregatedApiError();
            ex.setErrors(violations);

            throw ex;
        }
    }

    protected void applyUpdateModel(T original, U updateModel) {
        this.modelMapper.map(updateModel, original);
    }
}
