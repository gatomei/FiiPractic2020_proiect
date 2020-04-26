package fii.practic.health.control.service;

import fii.practic.health.entity.model.Appointment;
import fii.practic.health.entity.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
public class AppointmentServiceImpl  implements  AppointmentService{

    private AppointmentRepository appointmentRepository;

    @Autowired
    public AppointmentServiceImpl(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }
    @Override
    public Appointment save(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    @Override
    public List<Appointment> getAll() {
        return appointmentRepository.findAll();
    }

    @Override
    public Appointment getById(Long id) {
        return appointmentRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(Appointment appointment) {
        appointmentRepository.delete(appointment);
    }

    @Override
    public List<Appointment> getAllByDoctor(Long id) {
        return appointmentRepository.findAppointmentsByDoctorId(id);
    }

    @Override
    public List<Appointment> getAllByPatient(Long id) {
        return appointmentRepository.findAppointmentsByPatientId(id);
    }

    @Override
    public List<Appointment> getFutureAppointmentsByDoctor(Long id) {
        return appointmentRepository.findAppointmentsByStartTimeIsAfterAndDoctorId(new Date(), id);
    }

    @Override
    public List<Appointment> getFutureAppointments() {
        return appointmentRepository.findAppointmentsByStartTimeIsAfter(new Date());
    }

    @Override
    public List<Appointment> getDoneAppointments() {

        Date now=new Date();
        return appointmentRepository.findAppointmentsByEndTimeIsBeforeAndTookPlaceEquals(now, false);
    }

    @Override
    public boolean intervalIsBooked(Appointment appointment) {
        List<Appointment> appointments = appointmentRepository.findAppointmentsByDoctorIdAndStartTimeIsBeforeAndEndTimeIsAfter(appointment.getDoctorId(), appointment.getStartTime(), appointment.getStartTime());
        return appointments != null;
    }


}
