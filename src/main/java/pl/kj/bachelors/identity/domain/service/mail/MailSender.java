package pl.kj.bachelors.identity.domain.service.mail;

import pl.kj.bachelors.identity.domain.model.User;

public interface MailSender {
    void sendVerificationEmail(User user);
}
