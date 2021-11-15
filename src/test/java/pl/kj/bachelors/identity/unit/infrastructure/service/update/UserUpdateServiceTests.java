package pl.kj.bachelors.identity.unit.infrastructure.service.update;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.diff.JsonDiff;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import pl.kj.bachelors.identity.BaseTest;
import pl.kj.bachelors.identity.application.Application;
import pl.kj.bachelors.identity.domain.exception.AggregatedApiError;
import pl.kj.bachelors.identity.domain.exception.ValidationViolation;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.model.update.UserUpdateModel;
import pl.kj.bachelors.identity.infrastructure.repository.UserRepository;
import pl.kj.bachelors.identity.infrastructure.service.update.UserUpdateServiceImpl;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

public class UserUpdateServiceTests extends BaseTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserUpdateServiceImpl service;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testProcessUpdate_Correct() throws IOException {
        User user = this.userRepository.findById("uid-active-1").orElseThrow();
        String newFirstName = "Marry";
        String patchString = String.format(
                "[" +
                        "{\"op\": \"replace\", \"path\": \"/first_name\", \"value\": \"%s\"}," +
                        "{\"op\": \"replace\", \"path\": \"/picture_id\", \"value\": %d}" +
                        "]",
                newFirstName,
                1
        );
        JsonPatch patch = JsonPatch.fromJson(this.objectMapper.readTree(patchString));

        Throwable thrown = catchThrowable(() -> this.service.processUpdate(user, patch, UserUpdateModel.class));

        assertThat(thrown).isNull();
        assertThat(user.getFirstName()).isEqualTo(newFirstName);
        assertThat(user.getPicture()).isNotNull();
        assertThat(user.getPicture().getId()).isEqualTo(1);

    }

    @Test
    public void testProcessUpdate_BadValidation() throws IOException {
        User user = this.userRepository.findById("uid-active-1").orElseThrow();
        String newFirstName = "";
        String patchString = String.format(
                "[" +
                        "{\"op\": \"replace\", \"path\": \"/first_name\", \"value\": \"%s\"}," +
                        "{\"op\": \"add\", \"path\": \"/picture_id\", \"value\": \"%d\"}" +
                        "]",
                newFirstName,
                1
        );
        JsonPatch patch = JsonPatch.fromJson(this.objectMapper.readTree(patchString));

        Throwable thrown = catchThrowable(() -> this.service.processUpdate(user, patch, UserUpdateModel.class));
        assertThat(thrown).isInstanceOf(AggregatedApiError.class);
        AggregatedApiError aggregatedApiError = (AggregatedApiError) thrown;
        assertThat(aggregatedApiError.getErrors().stream().anyMatch(it -> it.getCode().equals("ID.002"))).isTrue();
    }


}
