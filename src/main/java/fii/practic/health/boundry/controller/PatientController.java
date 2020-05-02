package fii.practic.health.boundry.controller;

import fii.practic.health.boundry.dto.DoctorDTO;
import fii.practic.health.boundry.dto.PatientDTO;
import fii.practic.health.entity.model.Doctor;
import fii.practic.health.entity.model.Patient;
import fii.practic.health.control.service.DoctorService;
import fii.practic.health.control.service.PatientService;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/patients")
public class PatientController {

    private PatientService patientService;
    private DoctorService doctorService;
    private ModelMapper modelMapper;

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    @Autowired
    public PatientController(PatientService patientService, DoctorService doctorService, ModelMapper modelMapper) {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.modelMapper = modelMapper;
    }

    /**
     * Searches for and returns a list of all the patients
     * @return ResponseEntity containing a list of patients
     */
    @GetMapping
    public ResponseEntity<List<PatientDTO>> getPatients() {
        List<Patient> patients = patientService.getAll();

        return new ResponseEntity<>((List<PatientDTO>) modelMapper.map(patients, new TypeToken<List<PatientDTO>>(){}.getType()), HttpStatus.OK);
    }

    /**
     * Searches for and returns a patient identified by his id
     * @param id the patient's id
     * @return ResponseEntity containing the found patient
     */
    @GetMapping(value = "/{id}")
    public ResponseEntity<PatientDTO> getById(@PathVariable("id") Long id) {
        Patient patient = patientService.getById(id);

        return new ResponseEntity<>(modelMapper.map(patient, PatientDTO.class), HttpStatus.OK);
    }

    /**
     * Creates a new patient
     * @param patientDTO contains the patient entity data, given by the client
     * @return ResponseEntity containing the new created patient
     */
    @PostMapping
    public ResponseEntity<PatientDTO> save(@RequestBody PatientDTO patientDTO){
        Patient newPatient = patientService.save(modelMapper.map(patientDTO, Patient.class));

        logger.info(String.format("New patient entity with id %d was successfully created", newPatient.getId()));
        return new ResponseEntity<>(modelMapper.map(newPatient, PatientDTO.class), HttpStatus.CREATED);
    }

    /**
     * Partially updates an existing patient's information
     * The patient is identified by his id
     * @param id identifies the patient
     * @param patientDTO the new information that need to be updated
     * @return ResponseEntity containing the new updated patient
     */
    @PatchMapping(value = "/{id}")
    public ResponseEntity<PatientDTO> patch(@PathVariable Long id, @RequestBody PatientDTO patientDTO){
        Patient dbPatient = patientService.getById(id);

        if(dbPatient != null) {
            modelMapper.map(patientDTO, dbPatient);
            logger.info(String.format("Patient entity with id %d was successfully updated", dbPatient.getId()));
            return new ResponseEntity<>(modelMapper.map(patientService.patch(dbPatient), PatientDTO.class), HttpStatus.OK);
        }

        return null;
    }

    /**
     * Updates an existing patient's information
     * @param id uniqeuly identifies the patient
     * @param patientDTO the patient new info
     * @return ResponseEntity containing the new updated patient
     */
    @PutMapping(value = "/{id}")
    public ResponseEntity<PatientDTO> update(@PathVariable Long id, @RequestBody PatientDTO patientDTO){
        Patient dbPatient = patientService.getById(id);

        if(dbPatient != null) {
            modelMapper.getConfiguration().setSkipNullEnabled(false);
            modelMapper.map(patientDTO, dbPatient);
            modelMapper.getConfiguration().setSkipNullEnabled(true);

            logger.info(String.format("Patient entity with id %d was successfully updated", dbPatient.getId()));

            return new ResponseEntity<>(modelMapper.map(patientService.update(dbPatient), PatientDTO.class), HttpStatus.OK);
        }

        return null;
    }

    /**
     * Deletes an existing patient entity
     * @param id uniquely identifies the patient
     * @return ResponseEntity with no content
     */
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        Patient dbPatient = patientService.getById(id);

        if(dbPatient != null){
            patientService.delete(dbPatient);

            logger.info(String.format("Patient entity with id %d was successfully deleted", dbPatient.getId()));
        }

        return ResponseEntity.noContent().build();
    }
}
