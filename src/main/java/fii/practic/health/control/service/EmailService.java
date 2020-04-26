package fii.practic.health.control.service;

import org.springframework.mail.SimpleMailMessage;

public interface EmailService {

    void sendSimpleEmail(String to, String subject, String text);

}
