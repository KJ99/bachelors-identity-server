package pl.kj.bachelors.identity.domain.service.update;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import pl.kj.bachelors.identity.domain.exception.AggregatedApiError;

public interface UpdateService<T, U> {
    void processUpdate(T original, JsonPatch patch, Class<U> updateModelClass) throws JsonPatchException, JsonProcessingException, AggregatedApiError;
}
