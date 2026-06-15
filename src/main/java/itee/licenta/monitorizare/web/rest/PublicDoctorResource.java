package itee.licenta.monitorizare.web.rest;

import itee.licenta.monitorizare.domain.Doctor;
import itee.licenta.monitorizare.repository.DoctorRepository;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Transactional(readOnly = true)
public class PublicDoctorResource {

    private final DoctorRepository doctorRepository;

    public PublicDoctorResource(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @GetMapping("/public/doctors")
    public ResponseEntity<List<Map<String, Object>>> getPublicDoctorList() {
        List<Doctor> doctors = doctorRepository.findAllWithToOneRelationships();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Doctor doctor : doctors) {
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("id", doctor.getId());
            dto.put("specialization", doctor.getSpecialization());
            dto.put("officeLocation", doctor.getOfficeLocation());
            dto.put("firstName", doctor.getUser().getFirstName());
            dto.put("lastName", doctor.getUser().getLastName());
            dto.put(
                "displayName",
                "Dr. " + doctor.getUser().getLastName() + " " + doctor.getUser().getFirstName() + " \u2014 " + doctor.getSpecialization()
            );
            result.add(dto);
        }

        return ResponseEntity.ok(result);
    }
}
