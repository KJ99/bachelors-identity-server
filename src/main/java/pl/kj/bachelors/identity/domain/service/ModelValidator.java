package pl.kj.bachelors.identity.domain.service;

import pl.kj.bachelors.identity.domain.exception.ValidationViolation;

import java.util.Collection;

public interface ModelValidator {
    <T> Collection<ValidationViolation> validateModel(T model);
}
