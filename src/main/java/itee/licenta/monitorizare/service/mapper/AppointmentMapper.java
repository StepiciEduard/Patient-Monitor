package itee.licenta.monitorizare.service.mapper;

import itee.licenta.monitorizare.domain.Appointment;
import itee.licenta.monitorizare.domain.AppointmentSlot;
import itee.licenta.monitorizare.domain.Doctor;
import itee.licenta.monitorizare.domain.Patient;
import itee.licenta.monitorizare.service.dto.AppointmentDTO;
import itee.licenta.monitorizare.service.dto.AppointmentSlotDTO;
import itee.licenta.monitorizare.service.dto.DoctorDTO;
import itee.licenta.monitorizare.service.dto.PatientDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Appointment} and its DTO {@link AppointmentDTO}.
 */
@Mapper(componentModel = "spring")
public interface AppointmentMapper extends EntityMapper<AppointmentDTO, Appointment> {
    @Mapping(target = "slot", source = "slot", qualifiedByName = "appointmentSlotId")
    @Mapping(target = "patient", source = "patient", qualifiedByName = "patientId")
    @Mapping(target = "doctor", source = "doctor", qualifiedByName = "doctorSpecialization")
    AppointmentDTO toDto(Appointment s);

    @Named("appointmentSlotId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    AppointmentSlotDTO toDtoAppointmentSlotId(AppointmentSlot appointmentSlot);

    @Named("patientId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PatientDTO toDtoPatientId(Patient patient);

    @Named("doctorSpecialization")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "specialization", source = "specialization")
    DoctorDTO toDtoDoctorSpecialization(Doctor doctor);
}
