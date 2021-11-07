package pl.kj.bachelors.identity.domain.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import pl.kj.bachelors.identity.domain.config.PasswordConfig;
import pl.kj.bachelors.identity.domain.constraint.Password;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Service
public class PasswordValidator implements ConstraintValidator<Password, String> {
    @Autowired
    PasswordConfig config;

    @Override
    public void initialize(Password constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String pass, ConstraintValidatorContext context) {

        return pass.length() >= config.getMinLength() && pass.length() <  config.getMaxLength() &&
                pass.matches(".*[A-Z].*") &&
                pass.matches(".*[a-z].*") &&
                pass.matches(".*[0-9].*") &&
                pass.matches(".*([!-/]|[:-@]|[\\[-`]|[{-~]).*");
    }
}
