package fii.practic.health.control.service;

import fii.practic.health.entity.model.Appointment;

import java.util.Date;
import java.util.List;

public interface AppointmentService {

    /**
     * Saves a new appointment object in the database
     * @param appointment is the entity to be saved in the database
     * @return the new appointment created
     */
    Appointment save(Appointment appointment);

    /**
     * Returns a list of all the appointments from the database
     * @return the list of appointments
     */
    List<Appointment> getAll();

    /**
     * Returns the appointment with the specified id, from the database
     * @param id the id of Appointment object
     * @return appointment with the specified object if exists
     */
    Appointment getById(Long id);

    /**
     * Deletes an appointment from the database
     * @param appointment the appointment object to be deleted
     */
    void delete(Appointment appointment);

    /**
     *
     * @param id identifies the doctor
     * @return list of appointments for the specified doctor
     */
    List<Appointment> getAllByDoctor(Long id);

    /**
     *
     * @param id identifies the patient
     * @return list of appointments for the specified patient
     */
    List<Appointment> getAllByPatient(Long id);

    /**
     * Returns a list of appointments that will take place in the future for a specified doctor
     * @param id id of doctor whose appointments are searched
     * @return list of appointments that will take place in the future
     */
    List<Appointment> getFutureAppointmentsByDoctor(Long id);

    /**
     * Searches and returns a list of all the appointments that are to take place in the future
     * @return list of appointments that did not take place
     */
    List<Appointment> getFutureAppointments();


    /**
     * Searches and returns a list of all the appointments that took place
     * @return list of appointments that already took place
     */
    List<Appointment> getDoneAppointments();

    /**
     * Searches if the interval of the specified appointment is already booked
     * Returns true if so, otherwise false
     * @param appointment is the appointment whose interval is checked
     * @return
     */
    boolean intervalIsBooked(Appointment appointment);
}
