package pl.kj.bachelors.identity.domain.service.mail;

import pl.kj.bachelors.identity.domain.model.User;
import pl.kj.bachelors.identity.domain.model.UserVerification;

public interface MailSender {
    void sendVerificationEmail(UserVerification verification);
}
