package pl.kj.bachelors.identity.domain.security.voter;

import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.security.action.Action;

public interface Voter<T, A extends Action> {
    boolean vote(T subject, A action,  User user);
    Class<T> getSupportedSubjectType();
    A[] getSupportedActions();
}
