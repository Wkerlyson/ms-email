package com.ms.email.services;

import com.ms.email.enums.StatusEmail;
import com.ms.email.models.EmailModel;
import com.ms.email.repositories.EmailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class EmailService {
    @Autowired
    private EmailRepository emailRepository;
    @Value("${spring.mail.password}")
    private String pass;

    @Autowired
    private JavaMailSender mailSender;

    public EmailModel sendEmail(EmailModel emailModel) {
        log.error(pass);
        emailModel.setSendDateEmail(LocalDateTime.now());
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailModel.getEmailFrom());
            message.setTo(emailModel.getEmailTo());
            message.setSubject(emailModel.getSubject());
            message.setText(emailModel.getText());
            mailSender.send(message);

            emailModel.setStatusEmail(StatusEmail.SENT);
        }catch (MailException e){
            emailModel.setStatusEmail(StatusEmail.ERROR);
            log.error("Error sending email to " + emailModel.getEmailTo() + " | ERROR: "  + e.getMessage());
        }finally {
            return emailRepository.save(emailModel);
        }
    }
}
