package itee.licenta.monitorizare.service.mapper;

import static itee.licenta.monitorizare.domain.PatientAsserts.*;
import static itee.licenta.monitorizare.domain.PatientTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PatientMapperTest {

    private PatientMapper patientMapper;

    @BeforeEach
    void setUp() {
        patientMapper = new PatientMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getPatientSample1();
        var actual = patientMapper.toEntity(patientMapper.toDto(expected));
        assertPatientAllPropertiesEquals(expected, actual);
    }
}
