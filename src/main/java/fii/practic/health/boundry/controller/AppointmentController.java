package fii.practic.health.boundry.controller;

import fii.practic.health.boundry.dto.AppointmentDTO;
import fii.practic.health.boundry.exceptions.BadRequestException;
import fii.practic.health.boundry.exceptions.NotFoundException;
import fii.practic.health.control.service.AppointmentService;
import fii.practic.health.control.service.DoctorService;
import fii.practic.health.control.service.EmailServiceImpl;
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
    private EmailServiceImpl emailService;
    private SimpleMailMessage templatePatient;
    private SimpleMailMessage templateDoctor;

    @Autowired
    public AppointmentController(AppointmentService appointmentService, ModelMapper modelMapper, DoctorService doctorService,
                                 PatientService patientService, EmailServiceImpl emailService,
                                 @Qualifier("appointmentCreatedPatientTemplate") SimpleMailMessage templatePatient,
                                 @Qualifier("appointmentCreatedDoctorTemplate") SimpleMailMessage templateDoctor)
    {
        this.appointmentService = appointmentService;
        this.modelMapper = modelMapper;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.emailService = emailService;
        this.templatePatient = templatePatient;
        this.templateDoctor = templateDoctor;
    }

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

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        String formatedDate = dateFormat.format(newAppointment.getStartTime());
        String formatedTime = timeFormat.format(newAppointment.getStartTime());

        String textPatient = String.format(templatePatient.getText(),doctorDb.getFirstName(), doctorDb.getLastName(), formatedDate, formatedTime);
        String textDoctor = String.format(templateDoctor.getText(),patientDb.getFirstName(), patientDb.getLastName(), formatedDate, formatedTime);

        emailService.sendSimpleEmail(patientDb.getEmail().getEmail(), "New appointment",textPatient);
        emailService.sendSimpleEmail(doctorDb.getEmail().getEmail(), "New appointment", textDoctor);

        logger.info(String.format("Appointment with id %d was created",newAppointment.getId()));

        return new ResponseEntity<>(modelMapper.map(newAppointment, AppointmentDTO.class), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AppointmentDTO>> getAppointments()
    {
        List<Appointment> appointments = appointmentService.getAll();

        return new ResponseEntity<>((List<AppointmentDTO>) modelMapper.map(appointments, new TypeToken<List<AppointmentDTO>>(){}.getType()), HttpStatus.OK);

    }

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

    @GetMapping(value="/doctor/{id}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByDoctorId(@PathVariable Long id)
    {
        List<Appointment> appointments = appointmentService.getAllByDoctor(id);

        return new ResponseEntity<>((List<AppointmentDTO>) modelMapper.map(appointments, new TypeToken<List<AppointmentDTO>>(){}.getType()), HttpStatus.OK);

    }

    @GetMapping(value="/patient/{id}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPatientId(@PathVariable Long id)
    {
        List<Appointment> appointments = appointmentService.getAllByPatient(id);

        return new ResponseEntity<>((List<AppointmentDTO>) modelMapper.map(appointments, new TypeToken<List<AppointmentDTO>>(){}.getType()), HttpStatus.OK);

    }

    @GetMapping(value="/future")
    public ResponseEntity<List<AppointmentDTO>> getFutureAppointments()
    {
        List<Appointment> appointments = appointmentService.getFutureAppointments();

        return new ResponseEntity<>((List<AppointmentDTO>) modelMapper.map(appointments, new TypeToken<List<AppointmentDTO>>(){}.getType()), HttpStatus.OK);

    }

    @GetMapping(value="/future/{id}")
    public ResponseEntity<List<AppointmentDTO>> getFutureAppointments(@PathVariable Long id)
    {
        List<Appointment> appointments = appointmentService.getFutureAppointmentsByDoctor(id);

        return new ResponseEntity<>((List<AppointmentDTO>) modelMapper.map(appointments, new TypeToken<List<AppointmentDTO>>(){}.getType()), HttpStatus.OK);

    }




}
