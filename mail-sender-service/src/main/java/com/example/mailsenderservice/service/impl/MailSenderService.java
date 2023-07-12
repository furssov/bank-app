package com.example.mailsenderservice.service.impl;

import com.example.mailsenderservice.service.MailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@PropertySource("classpath:application.properties")
public class MailSenderService implements MailSender {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String username;

    @Autowired
    public MailSenderService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendMessage(String toAddress, String theme, String message) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject(theme);
        simpleMailMessage.setText(message);
        simpleMailMessage.setFrom(username);
        simpleMailMessage.setTo(toAddress);
        javaMailSender.send(simpleMailMessage);
    }
}
