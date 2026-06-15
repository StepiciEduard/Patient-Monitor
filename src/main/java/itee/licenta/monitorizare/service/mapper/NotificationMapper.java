package itee.licenta.monitorizare.service.mapper;

import itee.licenta.monitorizare.domain.Notification;
import itee.licenta.monitorizare.domain.Patient;
import itee.licenta.monitorizare.domain.User;
import itee.licenta.monitorizare.service.dto.NotificationDTO;
import itee.licenta.monitorizare.service.dto.PatientDTO;
import itee.licenta.monitorizare.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Notification} and its DTO {@link NotificationDTO}.
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper extends EntityMapper<NotificationDTO, Notification> {
    @Mapping(target = "recipientUser", source = "recipientUser", qualifiedByName = "userLogin")
    @Mapping(target = "senderUser", source = "senderUser", qualifiedByName = "userLogin")
    @Mapping(target = "relatedPatient", source = "relatedPatient", qualifiedByName = "patientId")
    NotificationDTO toDto(Notification s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("patientId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PatientDTO toDtoPatientId(Patient patient);
}
