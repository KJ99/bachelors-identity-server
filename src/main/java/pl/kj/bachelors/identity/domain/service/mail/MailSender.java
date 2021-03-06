package pl.kj.bachelors.identity.domain.service.mail;

import pl.kj.bachelors.identity.domain.model.entity.PasswordReset;
import pl.kj.bachelors.identity.domain.model.entity.UserVerification;

public interface MailSender {
    void sendVerificationEmail(UserVerification verification);
    void sendPasswordResetEmail(PasswordReset reset);
}
