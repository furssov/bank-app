package com.example.mailsenderservice.service;

public interface MailSender {
    void sendMessage(String toAddress, String theme, String message);
}
