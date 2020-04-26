package fii.practic.health.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Configuration
public class EmailConfig {
    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername("gatomei.fiipractic@gmail.com");
        mailSender.setPassword("fiipractic2020");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }


    @Bean
    public SimpleMailMessage doctorCreatedTemplate() {
        SimpleMailMessage message = new SimpleMailMessage();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        message.setText(
                "Welcome,\n%s %s\nYour Entity was succesfully created today, "+dtf.format(now)+
                        ".\nYour data information is:%s\nKind regards,\nFiiPracticApp Team");
        return message;
    }
    @Bean
    public SimpleMailMessage appointmentCreatedPatientTemplate() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setText(
                "Hello,\n\nYour appointment was created at doctor %s %s.\nAppointment will take place on date %s at time %s.\n\nKind regards,\nFiiPracticApp Team");
        return message;
    }

    @Bean
    public SimpleMailMessage appointmentCreatedDoctorTemplate() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setText(
                "Hello,\n\nA new appointment was created for patient %s %s.\nAppointment will take place on date %s at time %s.\n\nKind regards,\nFiiPracticApp Team");
        return message;
    }
}
