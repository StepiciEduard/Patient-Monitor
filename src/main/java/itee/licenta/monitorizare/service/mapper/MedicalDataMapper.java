package itee.licenta.monitorizare.service.mapper;

import itee.licenta.monitorizare.domain.MedicalData;
import itee.licenta.monitorizare.domain.Patient;
import itee.licenta.monitorizare.service.dto.MedicalDataDTO;
import itee.licenta.monitorizare.service.dto.PatientDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link MedicalData} and its DTO {@link MedicalDataDTO}.
 */
@Mapper(componentModel = "spring")
public interface MedicalDataMapper extends EntityMapper<MedicalDataDTO, MedicalData> {
    @Mapping(target = "patient", source = "patient", qualifiedByName = "patientId")
    MedicalDataDTO toDto(MedicalData s);

    @Named("patientId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PatientDTO toDtoPatientId(Patient patient);
}
