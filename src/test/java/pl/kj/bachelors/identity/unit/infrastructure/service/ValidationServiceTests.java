package pl.kj.bachelors.identity.unit.infrastructure.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pl.kj.bachelors.identity.application.Application;
import pl.kj.bachelors.identity.fixture.model.ExampleValidatableModel;
import pl.kj.bachelors.identity.infrastructure.service.ValidationService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = { Application.class })
public class ValidationServiceTests {
    @Autowired
    private ValidationService service;

    @Test
    public void testValidateModel_NoErrors() {
        var model = ExampleValidatableModel.getValidInstance();

        var result = this.service.validateModel(model);

        assertThat(result).isEmpty();
    }

    @Test
    public void testValidateModel_WithErrors() {
        var model = ExampleValidatableModel.getInvalidInstance();

        var result = this.service.validateModel(model);

        assertThat(result)
                .isNotEmpty()
                .hasSize(4);
        assertThat(result.stream().anyMatch(item -> item.getCode() != null && item.getCode().equals("ID.003"))).isTrue();
        assertThat(result.stream().allMatch(item -> item.getPath() != null)).isTrue();
    }
}