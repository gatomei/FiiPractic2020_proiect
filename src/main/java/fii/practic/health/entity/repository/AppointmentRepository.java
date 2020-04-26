package fii.practic.health.entity.repository;

import fii.practic.health.entity.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findAppointmentsByDoctorId(Long id);
    List<Appointment> findAppointmentsByPatientId(Long id);
    List<Appointment> findAppointmentsByStartTimeIsAfter(Date now);
    List<Appointment> findAppointmentsByStartTimeIsAfterAndDoctorId(Date now, Long id);
    List<Appointment> findAppointmentsByEndTimeIsBeforeAndTookPlaceEquals(Date now, boolean tookPlace);
    List<Appointment> findAppointmentsByDoctorIdAndStartTimeIsBeforeAndEndTimeIsAfter(Long id, Date start, Date end);

}
