package fii.practic.health.control.service;

import fii.practic.health.entity.model.Doctor;

import java.util.List;

public interface DoctorService {

    /**
     * Searches and returns all the doctors
     * @return a list of doctors
     */
    List<Doctor> getAll();

    /**
     * Searches a doctor by his id
     * @param id the doctor's id
     * @return a doctor entity
     */
    Doctor getById(Long id);

    /**
     * Creates a new doctor with the specified data
     * @param doctor the new doctor
     * @return the new doctor entity
     */
    Doctor save(Doctor doctor);

    /**
     * Searches doctors by their patien's first name
     * @param firstName the patient first name
     * @return
     */
    List<Doctor> findDoctorsByPatientsFirstName(String firstName);

    /**
     * Updates the data of an existing doctor entity
     * @param doctor the doctor entity with his new data
     * @return the updated doctor entity
     */
    Doctor update(Doctor doctor);

    /**
     * Partially updates an existing doctor entity
     * @param doctor the doctor entity with his new data
     * @return the updated doctor entity
     */
    Doctor patch(Doctor doctor);

    /**
     * Deletes a doctor entity
     * @param doctor the doctor to be deleted
     */
    void delete(Doctor doctor);
}
