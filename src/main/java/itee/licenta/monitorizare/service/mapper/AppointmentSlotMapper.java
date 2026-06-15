package itee.licenta.monitorizare.service.mapper;

import itee.licenta.monitorizare.domain.AppointmentSlot;
import itee.licenta.monitorizare.domain.Doctor;
import itee.licenta.monitorizare.service.dto.AppointmentSlotDTO;
import itee.licenta.monitorizare.service.dto.DoctorDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link AppointmentSlot} and its DTO {@link AppointmentSlotDTO}.
 */
@Mapper(componentModel = "spring")
public interface AppointmentSlotMapper extends EntityMapper<AppointmentSlotDTO, AppointmentSlot> {
    @Mapping(target = "doctor", source = "doctor", qualifiedByName = "doctorSpecialization")
    AppointmentSlotDTO toDto(AppointmentSlot s);

    @Named("doctorSpecialization")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "specialization", source = "specialization")
    DoctorDTO toDtoDoctorSpecialization(Doctor doctor);
}
