package pl.kj.bachelors.identity.domain.constraint;

import pl.kj.bachelors.identity.domain.validator.FieldValueMatchValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FieldValueMatchValidator.class)
@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldValueMatch {
    String message() default "Fields values don't match!";
    String field();
    String target();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
