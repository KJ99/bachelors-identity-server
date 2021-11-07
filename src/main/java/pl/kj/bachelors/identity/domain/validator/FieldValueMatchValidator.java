package pl.kj.bachelors.identity.domain.validator;

import org.springframework.beans.BeanWrapperImpl;
import pl.kj.bachelors.identity.domain.constraint.FieldValueMatch;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FieldValueMatchValidator implements ConstraintValidator<FieldValueMatch, Object> {
    private String field;
    private String target;

    @Override
    public void initialize(FieldValueMatch constraint) {
        this.field = constraint.field();
        this.target = constraint.target();
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        Object fieldValue = new BeanWrapperImpl(obj).getPropertyValue(this.field);
        Object targetValue = new BeanWrapperImpl(obj).getPropertyValue(this.target);

        boolean valid;
        if (fieldValue != null) {
            valid = fieldValue.equals(targetValue);
        } else {
            valid = targetValue == null;
        }

        return valid;
    }
}
