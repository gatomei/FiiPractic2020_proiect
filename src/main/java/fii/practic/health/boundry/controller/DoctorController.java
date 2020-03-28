package fii.practic.health.boundry.controller;

import fii.practic.health.boundry.dto.DoctorDTO;
import fii.practic.health.control.service.PatientService;
import fii.practic.health.entity.model.Doctor;
import fii.practic.health.control.service.DoctorService;
import fii.practic.health.entity.model.Patient;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/doctors")
public class DoctorController {

    private DoctorService doctorService;
    private ModelMapper modelMapper;
    private PatientService patientService;

    @Autowired
    public DoctorController(DoctorService doctorService, ModelMapper modelMapper, PatientService patientService) {
        this.doctorService = doctorService;
        this.modelMapper = modelMapper;
        this.patientService = patientService;
    }

    @GetMapping
    public ResponseEntity<List<DoctorDTO>> getDoctors() {
        List<Doctor> doctors = doctorService.getAll();

        return new ResponseEntity<>((List<DoctorDTO>) modelMapper.map(doctors, new TypeToken<List<DoctorDTO>>(){}.getType()), HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<DoctorDTO> getById(@PathVariable("id") Long id) {
        Doctor doctor = doctorService.getById(id);

        return new ResponseEntity<>(modelMapper.map(doctor, DoctorDTO.class), HttpStatus.OK);
    }

    @GetMapping(value = "/filter")
    public ResponseEntity<List<DoctorDTO>> getDoctorsByPatientsFirstName(@RequestParam(value = "firstName") String firstName){
        List<Doctor> doctors = doctorService.findDoctorsByPatientsFirstName(firstName);

        return new ResponseEntity<>((List<DoctorDTO>) modelMapper.map(doctors, new TypeToken<List<DoctorDTO>>(){}.getType()), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<DoctorDTO> save(@RequestBody DoctorDTO doctorDTO){
        Doctor newDoctor = doctorService.save(modelMapper.map(doctorDTO, Doctor.class));

        return new ResponseEntity<>(modelMapper.map(newDoctor, DoctorDTO.class), HttpStatus.CREATED);
    }

    @PatchMapping(value = "/{id}")
    public ResponseEntity<DoctorDTO> patch(@PathVariable Long id, @RequestBody DoctorDTO doctorDTO){
        Doctor dbDoctor = doctorService.getById(id);

        if(dbDoctor != null) {
            modelMapper.map(doctorDTO, dbDoctor);

            return new ResponseEntity<>(modelMapper.map(doctorService.patch(dbDoctor), DoctorDTO.class), HttpStatus.OK);
        }

        return null;
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<DoctorDTO> update(@PathVariable Long id, @RequestBody DoctorDTO doctorDTO){
        Doctor dbDoctor = doctorService.getById(id);

        if(dbDoctor != null) {
            modelMapper.getConfiguration().setSkipNullEnabled(false);
            modelMapper.map(doctorDTO, dbDoctor);
            modelMapper.getConfiguration().setSkipNullEnabled(true);

            return new ResponseEntity<>(modelMapper.map(doctorService.update(dbDoctor), DoctorDTO.class), HttpStatus.OK);
        }

        return null;
    }

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
        }
        return ResponseEntity.noContent().build();
    }
}
