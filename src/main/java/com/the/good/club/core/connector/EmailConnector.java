package com.the.good.club.core.connector;

import jakarta.mail.MessagingException;
import java.nio.charset.StandardCharsets;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailConnector {
    private static final String FROM_ADDRESS = "bojko002@gmail.com";
    private static final String FROM_NAME    = "The Good Club";

    @Autowired
    private JavaMailSender emailSender;

    public void sendSimpleMessage(String to, String subject, String text) throws UnsupportedEncodingException {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);
            helper.setFrom(new InternetAddress(
                FROM_ADDRESS, FROM_NAME, StandardCharsets.UTF_8.name()));

            emailSender.send(message);
        } catch (MailException | MessagingException exception) {
            System.out.println(exception.getMessage());
        }
    }
}
