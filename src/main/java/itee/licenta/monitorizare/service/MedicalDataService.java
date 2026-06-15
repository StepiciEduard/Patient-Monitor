package itee.licenta.monitorizare.service;

import itee.licenta.monitorizare.domain.MedicalData;
import itee.licenta.monitorizare.repository.DoctorRepository;
import itee.licenta.monitorizare.repository.MedicalDataRepository;
import itee.licenta.monitorizare.repository.PatientRepository;
import itee.licenta.monitorizare.security.AuthoritiesConstants;
import itee.licenta.monitorizare.security.SecurityUtils;
import itee.licenta.monitorizare.service.dto.MedicalDataDTO;
import itee.licenta.monitorizare.service.mapper.MedicalDataMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link itee.licenta.monitorizare.domain.MedicalData}.
 */
@Service
@Transactional
public class MedicalDataService {

    private static final Logger LOG = LoggerFactory.getLogger(MedicalDataService.class);

    private final MedicalDataRepository medicalDataRepository;

    private final MedicalDataMapper medicalDataMapper;

    private final DoctorRepository doctorRepository;

    private final PatientRepository patientRepository;

    public MedicalDataService(
        MedicalDataRepository medicalDataRepository,
        MedicalDataMapper medicalDataMapper,
        DoctorRepository doctorRepository,
        PatientRepository patientRepository
    ) {
        this.medicalDataRepository = medicalDataRepository;
        this.medicalDataMapper = medicalDataMapper;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Save a medicalData.
     */
    public MedicalDataDTO save(MedicalDataDTO medicalDataDTO) {
        LOG.debug("Request to save MedicalData : {}", medicalDataDTO);
        MedicalData medicalData = medicalDataMapper.toEntity(medicalDataDTO);
        medicalData = medicalDataRepository.save(medicalData);
        return medicalDataMapper.toDto(medicalData);
    }

    /**
     * Update a medicalData.
     */
    public MedicalDataDTO update(MedicalDataDTO medicalDataDTO) {
        LOG.debug("Request to update MedicalData : {}", medicalDataDTO);
        MedicalData medicalData = medicalDataMapper.toEntity(medicalDataDTO);
        medicalData = medicalDataRepository.save(medicalData);
        return medicalDataMapper.toDto(medicalData);
    }

    /**
     * Partially update a medicalData.
     */
    public Optional<MedicalDataDTO> partialUpdate(MedicalDataDTO medicalDataDTO) {
        LOG.debug("Request to partially update MedicalData : {}", medicalDataDTO);

        return medicalDataRepository
            .findById(medicalDataDTO.getId())
            .map(existingMedicalData -> {
                medicalDataMapper.partialUpdate(existingMedicalData, medicalDataDTO);
                return existingMedicalData;
            })
            .map(medicalDataRepository::save)
            .map(medicalDataMapper::toDto);
    }

    /**
     * Get all medical data - filtered by role.
     * Admin sees all, Doctor sees only their patients' data, Patient sees only own data.
     */
    @Transactional(readOnly = true)
    public Page<MedicalDataDTO> findAll(Pageable pageable) {
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("");

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return medicalDataRepository.findAll(pageable).map(medicalDataMapper::toDto);
        }

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.DOCTOR)) {
            return doctorRepository
                .findByUserLogin(currentLogin)
                .map(doctor -> medicalDataRepository.findByDoctorId(doctor.getId(), pageable).map(medicalDataMapper::toDto))
                .orElse(Page.empty());
        }

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.PATIENT)) {
            return medicalDataRepository.findByPatientUserLogin(currentLogin, pageable).map(medicalDataMapper::toDto);
        }

        return Page.empty();
    }

    /**
     * Get one medicalData by id.
     */
    @Transactional(readOnly = true)
    public Optional<MedicalDataDTO> findOne(Long id) {
        LOG.debug("Request to get MedicalData : {}", id);
        return medicalDataRepository.findById(id).map(medicalDataMapper::toDto);
    }

    /**
     * Delete the medicalData by id.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete MedicalData : {}", id);
        medicalDataRepository.deleteById(id);
    }
}
