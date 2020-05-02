package fii.practic.health.boundry.controller;

import fii.practic.health.boundry.dto.DoctorDTO;
import fii.practic.health.boundry.exceptions.BadRequestException;
import fii.practic.health.boundry.exceptions.NotFoundException;
import fii.practic.health.control.service.MailService;
import fii.practic.health.control.service.PatientService;
import fii.practic.health.entity.model.Doctor;
import fii.practic.health.control.service.DoctorService;
import fii.practic.health.entity.model.Patient;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/doctors")
public class DoctorController {

    private DoctorService doctorService;
    /**
     * Used for mapping DTO to entity
     */
    private ModelMapper modelMapper;
    private PatientService patientService;
    /**
     * Used for sending email to the doctor, when his entity is created
     */
    private MailService emailService;



    private static final Logger logger = LoggerFactory.getLogger(DoctorController.class);

    @Autowired
    public DoctorController(DoctorService doctorService, ModelMapper modelMapper, PatientService patientService,
                            MailService emailService) {
        this.doctorService = doctorService;
        this.modelMapper = modelMapper;
        this.patientService = patientService;
        this.emailService = emailService;
    }

    /**
     * Returns a list of all the doctors
     * @return ResponseEntity containing a list of all the doctors
     */
    @GetMapping
    public ResponseEntity<List<DoctorDTO>> getDoctors() {
        List<Doctor> doctors = doctorService.getAll();

        return new ResponseEntity<>((List<DoctorDTO>) modelMapper.map(doctors, new TypeToken<List<DoctorDTO>>(){}.getType()), HttpStatus.OK);
    }

    /**
     * Searches and returns a doctor identified by his id
     * @param id uniquely identifies the doctor
     * @return
     * @throws NotFoundException
     */
    @GetMapping(value = "/{id}")
    public ResponseEntity<DoctorDTO> getById(@PathVariable("id") Long id) throws NotFoundException {
        Doctor doctor = doctorService.getById(id);

        if(doctor == null){
            logger.error(String.format("Doctor with id %d was not found", id));
            throw new NotFoundException(String.format("Doctor with id %d was not found", id));

        }

        return new ResponseEntity<>(modelMapper.map(doctor, DoctorDTO.class), HttpStatus.OK);
    }

    /**
     * Searches and returns a list of doctors identified by patient's firstName
     * @param firstName Patient's firstName
     * @return ResponseEntity containing a list of doctors
     */
    @GetMapping(value = "/filter")
    public ResponseEntity<List<DoctorDTO>> getDoctorsByPatientsFirstName(@RequestParam(value = "firstName") String firstName){
        List<Doctor> doctors = doctorService.findDoctorsByPatientsFirstName(firstName);

        return new ResponseEntity<>((List<DoctorDTO>) modelMapper.map(doctors, new TypeToken<List<DoctorDTO>>(){}.getType()), HttpStatus.OK);
    }

    /**
     * Creates a new doctor with the data specified
     * When a doctor is created, he receives an email as a confirmation
     * @param doctorDTO contains the doctor information given by the client
     * @return ResponseEntity containing the created doctor
     */
    @PostMapping
    public ResponseEntity<DoctorDTO> save(@RequestBody DoctorDTO doctorDTO){
        Doctor newDoctor = doctorService.save(modelMapper.map(doctorDTO, Doctor.class));

        //String text = String.format(template.getText(),newDoctor.getFirstName(), newDoctor.getLastName(), newDoctor.toString());
       // emailService.sendSimpleEmail(doctorDTO.getEmail().getEmail(),"Doctor entity created", text);
        emailService.sendNewDoctorEmail(newDoctor);
        logger.info(String.format("Doctor entity with id %d  was successfully created.",newDoctor.getId()));

        return new ResponseEntity<>(modelMapper.map(newDoctor, DoctorDTO.class), HttpStatus.CREATED);
    }

    /**
     * Updates(partially) an existing doctor's information, with the new information from the client, if the doctor exists
     * @param id uniquely identifies the doctor
     * @param doctorDTO DTO containg the new information
     * @return ResponseEntity with the doctor's updated data or null if the doctor does not exists
     */
    @PatchMapping(value = "/{id}")
    public ResponseEntity<DoctorDTO> patch(@PathVariable Long id, @RequestBody DoctorDTO doctorDTO){
        Doctor dbDoctor = doctorService.getById(id);

        if(dbDoctor != null) {
            modelMapper.map(doctorDTO, dbDoctor);
            logger.info(String.format("Doctor entity with id %d was successfully updated.", dbDoctor.getId()));
            return new ResponseEntity<>(modelMapper.map(doctorService.patch(dbDoctor), DoctorDTO.class), HttpStatus.OK);
        }

        return null;
    }


    /**
     * Updates an existing doctor's information, with the new information from the client, if the doctor with the specified id exists
     * @param id id uniquely identifies the doctor
     * @param doctorDTO DTO containg the new information
     * @return ResponseEntity with the doctor's updated data or null if the doctor does not exists
     * @throws NotFoundException if the doctor with the specified id does not exist
     * @throws BadRequestException if the id in the path does not correspond with the id from the DTO
     */
    @PutMapping(value = "/{id}")
    public ResponseEntity<DoctorDTO> update(@PathVariable Long id, @RequestBody DoctorDTO doctorDTO) throws NotFoundException, BadRequestException {
        if(!id.equals(doctorDTO.getId())){
            throw new BadRequestException(String.format("Id from PathVariable %d is different from id in Request body %d", id, doctorDTO.getId()));
        }

        Doctor dbDoctor = doctorService.getById(id);

        if(dbDoctor == null){
            throw new NotFoundException(String.format("Doctor with id %d was not found", id));
        }


        modelMapper.getConfiguration().setSkipNullEnabled(false);
        modelMapper.map(doctorDTO, dbDoctor);
        modelMapper.getConfiguration().setSkipNullEnabled(true);

        logger.info(String.format("Doctor entity with id %d was successfully updated.", dbDoctor.getId()));

        return new ResponseEntity<>(modelMapper.map(doctorService.update(dbDoctor), DoctorDTO.class), HttpStatus.OK);
    }

    /**
     * Deletes an existing doctor.
     * The doctor is searched by his id and if the entity exists, it is deleted
     * @param id the doctor id
     * @return an empty ResponseEntity
     */
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        Doctor dbDoctor = doctorService.getById(id);

        if(dbDoctor != null){
            List<Patient> patients = dbDoctor.getPatients();

            for (Patient patient : patients){
                patient.setDoctor(null);
                patientService.save(patient);
            }
            dbDoctor.setPatients(null);
            doctorService.save(dbDoctor);
            doctorService.delete(dbDoctor);

            logger.info(String.format("Doctor entity with id %d was successfully deleted.", dbDoctor.getId()));
        }
        return ResponseEntity.noContent().build();
    }
}
