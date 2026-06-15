package itee.licenta.monitorizare.service.mapper;

import itee.licenta.monitorizare.domain.Doctor;
import itee.licenta.monitorizare.domain.User;
import itee.licenta.monitorizare.service.dto.DoctorDTO;
import itee.licenta.monitorizare.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Doctor} and its DTO {@link DoctorDTO}.
 */
@Mapper(componentModel = "spring")
public interface DoctorMapper extends EntityMapper<DoctorDTO, Doctor> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    DoctorDTO toDto(Doctor s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
