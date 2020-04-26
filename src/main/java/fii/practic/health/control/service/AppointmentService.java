package fii.practic.health.control.service;

import fii.practic.health.entity.model.Appointment;

import java.util.List;

public interface AppointmentService {

    Appointment save(Appointment appointment);

    List<Appointment> getAll();

    Appointment getById(Long id);

    void delete(Appointment appointment);

    List<Appointment> getAllByDoctor(Long id);

    List<Appointment> getAllByPatient(Long id);

    List<Appointment> getFutureAppointmentsByDoctor(Long id);

    List<Appointment> getFutureAppointments();
}
