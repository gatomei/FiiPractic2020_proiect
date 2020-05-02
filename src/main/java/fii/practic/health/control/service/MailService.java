package fii.practic.health.control.service;

import fii.practic.health.entity.model.Appointment;
import fii.practic.health.entity.model.Doctor;
import fii.practic.health.entity.model.Patient;

public interface MailService {
    /**
     * Sends an email to the doctor, when his entity is created
     * @param doctor the new created doctor
     */
    void sendNewDoctorEmail(final Doctor doctor);

    /**
     * Sends an email to the doctor, when a patient creates an appointment
     * @param doctor the doctor that the patient wants to see
     * @param patient the patient who creates appointment
     * @param appointment the new appointment created
     */
    void sendNewAppointmentDoctorEmail(Doctor doctor, Patient patient, Appointment appointment);

    /**
     * Sends an email to the patient, when his new doctor's appointment
     * @param patient the patient who creates the appointment
     * @param doctor that the patient wants to see
     * @param appointment the new appointment created
     */
    void sendNewAppointmentPatientEmail( Patient patient,Doctor doctor, Appointment appointment);

}