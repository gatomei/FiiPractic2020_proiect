package fii.practic.health.control.service;

import fii.practic.health.entity.model.Patient;

import java.util.List;

public interface PatientService {

    /**
     * Searches and returns a list containing all the patients
     * @return a list of patients
     */
    List<Patient> getAll();

    /**
     * Searches for a petient by his id
     * @param id the patient id
     * @return the found patient
     */
    Patient getById(Long id);

    /**
     * Creates a new patient entity
     * @param patient the patient to be created
     * @return the new patient
     */
    Patient save(Patient patient);

    /**
     * Updates an existing patient with the specified data
     * @param patient the patient to be updated
     * @return the updated patient
     */
    Patient update(Patient patient);

    /**
     * Partially updates an existing patient
     * @param patient the patient to be updated
     * @return the updated patient
     */
    Patient patch(Patient patient);

    /**
     * Deletes an existing patient
     * @param patient the patient to be deleted
     */
    void delete(Patient patient);

}
