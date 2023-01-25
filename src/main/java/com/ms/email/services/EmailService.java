package com.ms.email.services;

import com.ms.email.enums.StatusEmail;
import com.ms.email.models.EmailModel;
import com.ms.email.repositories.EmailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class EmailService {
    @Autowired
    private EmailRepository emailRepository;
    @Autowired
    private JavaMailSender mailSender;

    @Retryable(value = RuntimeException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public EmailModel sendEmail(EmailModel emailModel) {
        emailModel.setSendDateEmail(LocalDateTime.now());
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailModel.getEmailFrom());
            message.setTo(emailModel.getEmailTo());
            message.setSubject(emailModel.getSubject());
            message.setText(emailModel.getText());
            mailSender.send(message);

            emailModel.setStatusEmail(StatusEmail.SENT);
            return emailRepository.save(emailModel);
        }catch (Exception e){
            log.error("> MsEmail.EmailService.sendEmail | Failed sending email to " + emailModel.getEmailTo()
                        + " | Retrying... | ERROR: "  + e.getMessage());
            throw new RuntimeException();
        }
    }

    @Recover
    public EmailModel retryFailure(EmailModel emailModel) {
        emailModel.setStatusEmail(StatusEmail.ERROR);
        emailRepository.save(emailModel);
        log.error("> MsEmail.EmailService.sendEmail | Send the message and try again 3 times, but it still fails");
        return emailModel;
    }
}
