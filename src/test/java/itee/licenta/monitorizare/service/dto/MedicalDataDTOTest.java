package itee.licenta.monitorizare.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import itee.licenta.monitorizare.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MedicalDataDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MedicalDataDTO.class);
        MedicalDataDTO medicalDataDTO1 = new MedicalDataDTO();
        medicalDataDTO1.setId(1L);
        MedicalDataDTO medicalDataDTO2 = new MedicalDataDTO();
        assertThat(medicalDataDTO1).isNotEqualTo(medicalDataDTO2);
        medicalDataDTO2.setId(medicalDataDTO1.getId());
        assertThat(medicalDataDTO1).isEqualTo(medicalDataDTO2);
        medicalDataDTO2.setId(2L);
        assertThat(medicalDataDTO1).isNotEqualTo(medicalDataDTO2);
        medicalDataDTO1.setId(null);
        assertThat(medicalDataDTO1).isNotEqualTo(medicalDataDTO2);
    }
}
