package itee.licenta.monitorizare.service.mapper;

import static itee.licenta.monitorizare.domain.MedicalDataAsserts.*;
import static itee.licenta.monitorizare.domain.MedicalDataTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MedicalDataMapperTest {

    private MedicalDataMapper medicalDataMapper;

    @BeforeEach
    void setUp() {
        medicalDataMapper = new MedicalDataMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getMedicalDataSample1();
        var actual = medicalDataMapper.toEntity(medicalDataMapper.toDto(expected));
        assertMedicalDataAllPropertiesEquals(expected, actual);
    }
}
