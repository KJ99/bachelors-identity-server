package pl.kj.bachelors.identity.infrastructure.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.kj.bachelors.identity.application.model.validation.ValidationViolation;
import pl.kj.bachelors.identity.domain.service.ModelValidator;
import pl.kj.bachelors.identity.domain.config.ApiConfig;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ValidationService implements ModelValidator {
    private final Validator validator;
    private final ApiConfig apiConfig;

    public ValidationService(
            @Autowired Validator validator,
            @Autowired ApiConfig apiConfig
            ) {
        this.validator = validator;
        this.apiConfig = apiConfig;
    }

    @Override
    public <T> Collection<ValidationViolation> validateModel(T model) {
        return this.validator.validate(model)
                .stream()
                .map(this::createValidationViolation)
                .collect(Collectors.toList());
    }

    private ValidationViolation createValidationViolation(ConstraintViolation<?> violation) {
        String code = violation.getMessage();
        String detailMessage = this.apiConfig.getErrors().get(code);
        if(detailMessage == null) {
            detailMessage = "Value of the field is not valid";
            code = null;
        }

        return new ValidationViolation(detailMessage, code, this.getViolationPath(violation));
    }

    private String getViolationPath(ConstraintViolation<?> violation) {
        List<String> pathParts = new ArrayList<>();
        for(Path.Node n : violation.getPropertyPath()) {
            pathParts.add(n.getName());
        }

        return String.join("/", pathParts);
    }
}
