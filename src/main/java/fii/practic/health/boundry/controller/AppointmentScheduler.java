package fii.practic.health.boundry.controller;

import fii.practic.health.control.service.AppointmentService;
import fii.practic.health.entity.model.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;


@Component
public class AppointmentScheduler {
    private static final Logger logger = LoggerFactory.getLogger(AppointmentScheduler.class);

    @Autowired
    private AppointmentService appointmentService;


    @Scheduled(fixedRate = 300000)
    public void reportCurrentTime() {
        List<Appointment> appointmentsDone = appointmentService.getDoneAppointments();
        for(Appointment appointment:appointmentsDone)
        {
            appointment.setTookPlace(true);
            appointmentService.save(appointment);

            logger.info("Appointment with id {} took place", appointment.getId());
        }


    }

}
