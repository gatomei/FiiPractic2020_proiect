package fii.practic.health.boundry.controller;

import fii.practic.health.boundry.dto.AppointmentDTO;
import fii.practic.health.boundry.exceptions.BadRequestException;
import fii.practic.health.boundry.exceptions.NotFoundException;
import fii.practic.health.control.service.AppointmentService;
import fii.practic.health.control.service.DoctorService;
import fii.practic.health.control.service.MailService;
import fii.practic.health.control.service.PatientService;
import fii.practic.health.entity.model.Appointment;
import fii.practic.health.entity.model.Doctor;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value="/api/appointments")
public class AppointmentController {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

    private AppointmentService appointmentService;
    private ModelMapper modelMapper;
    private DoctorService doctorService;
    private PatientService patientService;
    private MailService emailService;


    @Autowired
    public AppointmentController(AppointmentService appointmentService, ModelMapper modelMapper, DoctorService doctorService,
                                 PatientService patientService, MailService emailService)
    {
        this.appointmentService = appointmentService;
        this.modelMapper = modelMapper;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.emailService = emailService;
    }


    /**
     * Creates a new appointment for a patient at his doctor, if the appointment's data is correct
     * If the patient with the specified id does not exist, thows NotFoundException
     * If the doctor with the specified id does not exist, thows NotFoundException
     * If the specified patient does not correspond to the specified doctor, throws BadRequestException
     * If the appointment's startTime is not in the future throws BadRequestException
     * If the appointment's startTime is not before appointment's endTime,throws BadRequestException
     * If the appointment's interval is already booked by somebody else,throws BadRequestException
     * @param appointmentDTO contains appointment object data
     * @return
     * @throws NotFoundException
     * @throws BadRequestException
     */
    @PostMapping
    public ResponseEntity<AppointmentDTO> save(@RequestBody AppointmentDTO appointmentDTO) throws NotFoundException, BadRequestException {
        Doctor doctorDb = doctorService.getById(appointmentDTO.getDoctorId());
        if(doctorDb == null){
            String message = String.format("Doctor with id %d was not found", appointmentDTO.getDoctorId());
            logger.error(message);
            throw new NotFoundException(message);
        }

        Patient patientDb = patientService.getById(appointmentDTO.getPatientId());
        if(patientDb == null){
            String message =String.format("Patient with id %d was not found", appointmentDTO.getPatientId());
            logger.error(message);
            throw new NotFoundException(message);
        }

        if(!doctorDb.getId().equals(patientDb.getDoctor().getId()))
        {
            String message = String.format("Doctor Id from Patient Entity %d is different from doctor id in Request body %d",patientDb.getDoctor().getId(),appointmentDTO.getDoctorId());
            logger.error(message);
            throw new BadRequestException(message);
        }

        Date startTime = appointmentDTO.getStartTime();
        Date endTime = appointmentDTO.getEndTime();
        if(startTime.after(endTime))
        {
            throw new BadRequestException("Appointment startTime should be before endTime");

        }
        if(startTime.before(new Date()))
        {
            throw new BadRequestException("Appointment date must be in the future");

        }
        if(appointmentService.intervalIsBooked(modelMapper.map(appointmentDTO, Appointment.class)))
        {
            throw  new BadRequestException("Appointment date interval is already booked");
        }


        Appointment newAppointment = appointmentService.save(modelMapper.map(appointmentDTO, Appointment.class));
        emailService.sendNewAppointmentDoctorEmail(doctorDb, patientDb, newAppointment);
        emailService.sendNewAppointmentPatientEmail(patientDb,doctorDb, newAppointment);

        logger.info(String.format("Appointment with id %d was created",newAppointment.getId()));

        return new ResponseEntity<>(modelMapper.map(newAppointment, AppointmentDTO.class), HttpStatus.CREATED);
    }

    /**
     * Returns all the appointments
     * @return ResponseEntity  that contains a list of all the appointments
     */
    @GetMapping
    public ResponseEntity<List<AppointmentDTO>> getAppointments()
    {
        List<Appointment> appointments = appointmentService.getAll();

        return new ResponseEntity<>((List<AppointmentDTO>) modelMapper.map(appointments, new TypeToken<List<AppointmentDTO>>(){}.getType()), HttpStatus.OK);

    }

    /**
     * Cancels an appointment, if it exists and if it did not take place and if it will not occur in less than an hour
     * @param id the id which identifies the appointment
     * @return ResponseEntity with no content
     * @throws BadRequestException if the appointment already took place or it will occur in less than an hour
     */
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) throws BadRequestException {

        Appointment appointmentDb = appointmentService.getById(id);

        if(appointmentDb!=null)
        {
           Date startTime =appointmentDb.getStartTime();
           Date now = new Date();
           if(startTime.before(now))
           {
               throw new BadRequestException("An appointment which already took place in the past can't be canceled");
           }

           //an appointment which will take place in less than an hour can not be canceled

           Date next_hour=new Date(System.currentTimeMillis() + 3600 * 1000);

            if(startTime.after(next_hour))
            {
                appointmentService.delete(appointmentDb);
                logger.info(String.format("Appointment with id %d was successfully canceled", appointmentDb.getId()));
            }

            else
            {
                throw new BadRequestException("An appointment which will occur in the next hour can't be canceled");
            }

        }
        return ResponseEntity.noContent().build();

    }

    /**
     * Returns a list of appointements at a specified doctor
     * The doctor is identified by an id
     * @param id the id which identifies uniquely a doctor
     * @return ResponseEntity containing the list of appointments
     */
    @GetMapping(value="/doctor/{id}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByDoctorId(@PathVariable Long id)
    {
        List<Appointment> appointments = appointmentService.getAllByDoctor(id);

        return new ResponseEntity<>((List<AppointmentDTO>) modelMapper.map(appointments, new TypeToken<List<AppointmentDTO>>(){}.getType()), HttpStatus.OK);

    }

    /**
     *  Returns a list of appointements of a patient
     *  The patient is specified by an id
     * @param id the id which identifies uniquely a patient
     * @return ResponseEntity containing the list of appointments
     */
    @GetMapping(value="/patient/{id}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPatientId(@PathVariable Long id)
    {
        List<Appointment> appointments = appointmentService.getAllByPatient(id);

        return new ResponseEntity<>((List<AppointmentDTO>) modelMapper.map(appointments, new TypeToken<List<AppointmentDTO>>(){}.getType()), HttpStatus.OK);

    }

    /**
     * Returns a list of all the future appointments
     * @return ResponseEntity containing the list of appointments
     */
    @GetMapping(value="/future")
    public ResponseEntity<List<AppointmentDTO>> getFutureAppointments()
    {
        List<Appointment> appointments = appointmentService.getFutureAppointments();

        return new ResponseEntity<>((List<AppointmentDTO>) modelMapper.map(appointments, new TypeToken<List<AppointmentDTO>>(){}.getType()), HttpStatus.OK);

    }

    /**
     * Returns a list of all the future appointments, for a specified doctor
     * The doctor is identified by an id
     * @param id uniquely identifies the doctor
     * @return ResponseEntity containing the list of appointments
     */
    @GetMapping(value="/future/{id}")
    public ResponseEntity<List<AppointmentDTO>> getFutureAppointments(@PathVariable Long id)
    {
        List<Appointment> appointments = appointmentService.getFutureAppointmentsByDoctor(id);

        return new ResponseEntity<>((List<AppointmentDTO>) modelMapper.map(appointments, new TypeToken<List<AppointmentDTO>>(){}.getType()), HttpStatus.OK);

    }




}
