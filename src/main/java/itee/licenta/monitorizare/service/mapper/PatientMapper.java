package itee.licenta.monitorizare.service.mapper;

import itee.licenta.monitorizare.domain.Doctor;
import itee.licenta.monitorizare.domain.Patient;
import itee.licenta.monitorizare.domain.User;
import itee.licenta.monitorizare.service.dto.DoctorDTO;
import itee.licenta.monitorizare.service.dto.PatientDTO;
import itee.licenta.monitorizare.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Patient} and its DTO {@link PatientDTO}.
 */
@Mapper(componentModel = "spring")
public interface PatientMapper extends EntityMapper<PatientDTO, Patient> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    @Mapping(target = "doctor", source = "doctor", qualifiedByName = "doctorSpecialization")
    PatientDTO toDto(Patient s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("doctorSpecialization")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "specialization", source = "specialization")
    DoctorDTO toDtoDoctorSpecialization(Doctor doctor);
}
