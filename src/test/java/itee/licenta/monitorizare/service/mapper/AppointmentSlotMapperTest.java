package itee.licenta.monitorizare.service.mapper;

import static itee.licenta.monitorizare.domain.AppointmentSlotAsserts.*;
import static itee.licenta.monitorizare.domain.AppointmentSlotTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppointmentSlotMapperTest {

    private AppointmentSlotMapper appointmentSlotMapper;

    @BeforeEach
    void setUp() {
        appointmentSlotMapper = new AppointmentSlotMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAppointmentSlotSample1();
        var actual = appointmentSlotMapper.toEntity(appointmentSlotMapper.toDto(expected));
        assertAppointmentSlotAllPropertiesEquals(expected, actual);
    }
}
