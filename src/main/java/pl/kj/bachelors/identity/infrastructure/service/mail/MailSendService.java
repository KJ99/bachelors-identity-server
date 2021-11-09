package pl.kj.bachelors.identity.infrastructure.service.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pl.kj.bachelors.identity.domain.model.UserVerification;
import pl.kj.bachelors.identity.domain.service.mail.MailSender;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MailSendService implements MailSender {
    private final JavaMailSender emailSender;
    private final String from;

    public MailSendService(@Autowired JavaMailSender emailSender, @Value("${spring.mail.from}") String from) {
        this.emailSender = emailSender;
        this.from = from;
    }

    @Override
    public void sendVerificationEmail(UserVerification verification) {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        String receiver = verification.getUser().getEmail();
        String content = String.format("Your verification PIN is %s", verification.getPin());
        String subject = "Account Verification";
        SimpleMailMessage message = this.createPlainTextMessage(receiver, subject, content);
        this.sendMessage(message, 3);
    }

    private SimpleMailMessage createPlainTextMessage(final String to, final String subject, final String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(this.from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        return message;
    }

    private void sendMessage(final SimpleMailMessage message, final int nThread) {
        ExecutorService executor = Executors.newFixedThreadPool(nThread);
        executor.execute(() -> this.emailSender.send(message));
    }
}
