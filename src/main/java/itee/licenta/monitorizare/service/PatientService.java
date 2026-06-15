package itee.licenta.monitorizare.service;

import itee.licenta.monitorizare.domain.Patient;
import itee.licenta.monitorizare.repository.DoctorRepository;
import itee.licenta.monitorizare.repository.PatientRepository;
import itee.licenta.monitorizare.security.AuthoritiesConstants;
import itee.licenta.monitorizare.security.SecurityUtils;
import itee.licenta.monitorizare.service.dto.PatientDTO;
import itee.licenta.monitorizare.service.mapper.PatientMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link itee.licenta.monitorizare.domain.Patient}.
 */
@Service
@Transactional
public class PatientService {

    private static final Logger LOG = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;

    private final DoctorRepository doctorRepository;

    private final PatientMapper patientMapper;

    public PatientService(PatientRepository patientRepository, DoctorRepository doctorRepository, PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.patientMapper = patientMapper;
    }

    /**
     * Save a patient.
     */
    public PatientDTO save(PatientDTO patientDTO) {
        LOG.debug("Request to save Patient : {}", patientDTO);
        Patient patient = patientMapper.toEntity(patientDTO);
        patient = patientRepository.save(patient);
        return patientMapper.toDto(patient);
    }

    /**
     * Update a patient.
     */
    public PatientDTO update(PatientDTO patientDTO) {
        LOG.debug("Request to update Patient : {}", patientDTO);
        Patient patient = patientMapper.toEntity(patientDTO);
        patient = patientRepository.save(patient);
        return patientMapper.toDto(patient);
    }

    /**
     * Partially update a patient.
     */
    public Optional<PatientDTO> partialUpdate(PatientDTO patientDTO) {
        LOG.debug("Request to partially update Patient : {}", patientDTO);

        return patientRepository
            .findById(patientDTO.getId())
            .map(existingPatient -> {
                patientMapper.partialUpdate(existingPatient, patientDTO);
                return existingPatient;
            })
            .map(patientRepository::save)
            .map(patientMapper::toDto);
    }

    /**
     * Get all patients - filtered by role.
     * Admin sees all, Doctor sees only their patients, Patient sees only themselves.
     */
    public Page<PatientDTO> findAllWithEagerRelationships(Pageable pageable) {
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("");

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return patientRepository.findAllWithEagerRelationships(pageable).map(patientMapper::toDto);
        }

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.DOCTOR)) {
            return doctorRepository
                .findByUserLogin(currentLogin)
                .map(doctor -> patientRepository.findByDoctorId(doctor.getId(), pageable).map(patientMapper::toDto))
                .orElse(Page.empty());
        }

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.PATIENT)) {
            return patientRepository
                .findByUserLogin(currentLogin)
                .map(patient -> {
                    Page<PatientDTO> page = new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(patientMapper.toDto(patient)),
                        pageable,
                        1
                    );
                    return page;
                })
                .orElse(Page.empty());
        }

        return Page.empty();
    }

    /**
     * Get one patient by id - with access check.
     */
    @Transactional(readOnly = true)
    public Optional<PatientDTO> findOne(Long id) {
        LOG.debug("Request to get Patient : {}", id);
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("");

        Optional<Patient> patient = patientRepository.findOneWithEagerRelationships(id);

        if (patient.isEmpty()) {
            return Optional.empty();
        }

        // Admin can see all
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return patient.map(patientMapper::toDto);
        }

        // Doctor can see only their patients
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.DOCTOR)) {
            return patient
                .filter(
                    p -> p.getDoctor() != null && p.getDoctor().getUser() != null && currentLogin.equals(p.getDoctor().getUser().getLogin())
                )
                .map(patientMapper::toDto);
        }

        // Patient can see only themselves
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.PATIENT)) {
            return patient.filter(p -> p.getUser() != null && currentLogin.equals(p.getUser().getLogin())).map(patientMapper::toDto);
        }

        return Optional.empty();
    }

    /**
     * Delete the patient by id.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Patient : {}", id);
        patientRepository.deleteById(id);
    }
}
