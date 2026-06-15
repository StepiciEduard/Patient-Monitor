package itee.licenta.monitorizare.service.mapper;

import itee.licenta.monitorizare.domain.ChatMessage;
import itee.licenta.monitorizare.domain.Patient;
import itee.licenta.monitorizare.service.dto.ChatMessageDTO;
import itee.licenta.monitorizare.service.dto.PatientDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ChatMessage} and its DTO {@link ChatMessageDTO}.
 */
@Mapper(componentModel = "spring")
public interface ChatMessageMapper extends EntityMapper<ChatMessageDTO, ChatMessage> {
    @Mapping(target = "patient", source = "patient", qualifiedByName = "patientId")
    ChatMessageDTO toDto(ChatMessage s);

    @Named("patientId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PatientDTO toDtoPatientId(Patient patient);
}
