package fii.practic.health.control.service;

import fii.practic.health.boundry.controller.AppointmentController;
import fii.practic.health.entity.model.Appointment;
import fii.practic.health.entity.model.Doctor;
import fii.practic.health.entity.model.Patient;
import fii.practic.health.entity.model.Person;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
@Service("mailService")
public class MailServiceImpl implements MailService {

    JavaMailSender mailSender;

    VelocityEngine velocityEngine;

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender, VelocityEngine velocityEngine) {
        this.mailSender = mailSender;
        this.velocityEngine = velocityEngine;

    }

    private final String doctorTemplatePath = "/vmtemplates/doctorTemplate.vm";
    private final String appointmentDoctorTemplatePath = "/vmtemplates/appointmentDoctorTemplate.vm";
    private final String appointmentPatientTemplatePath = "/vmtemplates/appointmentPatientTemplate.vm";


    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);


    @Override
    public void sendNewDoctorEmail(Doctor doctor) {


        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setSubject("Doctor entity created");
            helper.setFrom("gatomei.fiipractic@gmail.com");
            helper.setTo(doctor.getEmail().getEmail());

            Map<String, Object> model = new HashMap<String, Object>();
            model.put("doctor", doctor);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            model.put("now", dtf.format(now));

            String text = getTemplateContent(model, doctorTemplatePath);

            helper.setText(text, true);
        };

        try {
            mailSender.send(preparator);
            logger.info("Email " + "Doctor entity created" + " has been sent successfully");
        } catch (MailException ex) {

            logger.error(ex.getMessage());
        }
    }

    @Override
    public void sendNewAppointmentDoctorEmail(Doctor doctor,Patient patient,  Appointment appointment) {


        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setSubject("New appointment created");
            helper.setFrom("gatomei.fiipractic@gmail.com");
            helper.setTo(doctor.getEmail().getEmail());

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

            String formatedDate = dateFormat.format(appointment.getStartTime());
            String formatedTime = timeFormat.format(appointment.getStartTime());

            Map<String, Object> model = new HashMap<String, Object>();
            model.put("doctor", doctor);
            model.put("patient", patient);
            model.put("date", formatedDate);
            model.put("time", formatedTime);
            model.put("cause", appointment.getCause());

            String text = getTemplateContent(model, appointmentDoctorTemplatePath);

            helper.setText(text, true);
        };

        try {
            mailSender.send(preparator);
            logger.info("Email " + "New appointment created" + " has been sent successfully");
        } catch (MailException ex) {

            logger.error(ex.getMessage());
        }
    }

    @Override
    public void sendNewAppointmentPatientEmail(Patient patient, Doctor doctor, Appointment appointment) {
        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setSubject("New appointment created");
            helper.setFrom("gatomei.fiipractic@gmail.com");
            helper.setTo(patient.getEmail().getEmail());

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

            String formatedDate = dateFormat.format(appointment.getStartTime());
            String formatedTime = timeFormat.format(appointment.getStartTime());

            Map<String, Object> model = new HashMap<String, Object>();
            model.put("doctor", doctor);
            model.put("patient", patient);
            model.put("date", formatedDate);
            model.put("time", formatedTime);
            model.put("cause", appointment.getCause());

            String text = getTemplateContent(model, appointmentPatientTemplatePath);

            helper.setText(text, true);
        };

        try {
            mailSender.send(preparator);
            logger.info("Email " + "New appointment created" + " has been sent successfully");
        } catch (MailException ex) {

            logger.error(ex.getMessage());
        }
    }



    public String getTemplateContent(Map<String, Object> model, String path) {
        StringBuffer content = new StringBuffer();
        try {
            content.append(VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, path, model));
            return content.toString();
        } catch (Exception e) {
            logger.error("Exception occured while processing email velocity template:" + e.getMessage());
        }
        return "";
    }

}