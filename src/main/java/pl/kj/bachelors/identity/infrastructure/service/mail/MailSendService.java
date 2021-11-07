package pl.kj.bachelors.identity.infrastructure.service.mail;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;
import pl.kj.bachelors.identity.domain.model.User;
import pl.kj.bachelors.identity.domain.service.mail.MailSender;

@Service
public class MailSendService implements MailSender {
    @Override
    public void sendVerificationEmail(User user) {
        throw new NotImplementedException();
    }
}
