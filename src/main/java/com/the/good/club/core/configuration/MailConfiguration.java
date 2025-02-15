package com.the.good.club.core.configuration;

import com.the.good.club.core.service.SecretManagerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.IOException;
import java.util.Properties;

@Configuration
public class MailConfiguration {

    private static final String SMTP_GMAIL_COM = "smtp.gmail.com";
    private static final int GMAIL_PORT = 587;
    private static final String GMAIL_USERNAME_SECRET_KEY = "mail-username";
    private static final String GMAIL_PASSWORD_SECRET_KEY = "mail-password";
    private final SecretManagerService secretManagerService;

    public MailConfiguration(SecretManagerService secretManagerService) {
        this.secretManagerService = secretManagerService;
    }

    @Bean
    public JavaMailSender getJavaMailSender() throws IOException {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(SMTP_GMAIL_COM);
        mailSender.setPort(GMAIL_PORT);

        mailSender.setUsername(secretManagerService.getSecret(GMAIL_USERNAME_SECRET_KEY));
        mailSender.setPassword(secretManagerService.getSecret(GMAIL_PASSWORD_SECRET_KEY));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");

        return mailSender;
    }
}
