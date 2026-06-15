package itee.licenta.monitorizare.service.mapper;

import static itee.licenta.monitorizare.domain.DoctorAsserts.*;
import static itee.licenta.monitorizare.domain.DoctorTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DoctorMapperTest {

    private DoctorMapper doctorMapper;

    @BeforeEach
    void setUp() {
        doctorMapper = new DoctorMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getDoctorSample1();
        var actual = doctorMapper.toEntity(doctorMapper.toDto(expected));
        assertDoctorAllPropertiesEquals(expected, actual);
    }
}
