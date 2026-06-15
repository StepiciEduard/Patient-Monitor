package itee.licenta.monitorizare.service;

import itee.licenta.monitorizare.domain.Doctor;
import itee.licenta.monitorizare.repository.DoctorRepository;
import itee.licenta.monitorizare.service.dto.DoctorDTO;
import itee.licenta.monitorizare.service.mapper.DoctorMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link itee.licenta.monitorizare.domain.Doctor}.
 */
@Service
@Transactional
public class DoctorService {

    private static final Logger LOG = LoggerFactory.getLogger(DoctorService.class);

    private final DoctorRepository doctorRepository;

    private final DoctorMapper doctorMapper;

    public DoctorService(DoctorRepository doctorRepository, DoctorMapper doctorMapper) {
        this.doctorRepository = doctorRepository;
        this.doctorMapper = doctorMapper;
    }

    /**
     * Save a doctor.
     *
     * @param doctorDTO the entity to save.
     * @return the persisted entity.
     */
    public DoctorDTO save(DoctorDTO doctorDTO) {
        LOG.debug("Request to save Doctor : {}", doctorDTO);
        Doctor doctor = doctorMapper.toEntity(doctorDTO);
        doctor = doctorRepository.save(doctor);
        return doctorMapper.toDto(doctor);
    }

    /**
     * Update a doctor.
     *
     * @param doctorDTO the entity to save.
     * @return the persisted entity.
     */
    public DoctorDTO update(DoctorDTO doctorDTO) {
        LOG.debug("Request to update Doctor : {}", doctorDTO);
        Doctor doctor = doctorMapper.toEntity(doctorDTO);
        doctor = doctorRepository.save(doctor);
        return doctorMapper.toDto(doctor);
    }

    /**
     * Partially update a doctor.
     *
     * @param doctorDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<DoctorDTO> partialUpdate(DoctorDTO doctorDTO) {
        LOG.debug("Request to partially update Doctor : {}", doctorDTO);

        return doctorRepository
            .findById(doctorDTO.getId())
            .map(existingDoctor -> {
                doctorMapper.partialUpdate(existingDoctor, doctorDTO);

                return existingDoctor;
            })
            .map(doctorRepository::save)
            .map(doctorMapper::toDto);
    }

    /**
     * Get all the doctors.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<DoctorDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Doctors");
        return doctorRepository.findAll(pageable).map(doctorMapper::toDto);
    }

    /**
     * Get all the doctors with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<DoctorDTO> findAllWithEagerRelationships(Pageable pageable) {
        return doctorRepository.findAllWithEagerRelationships(pageable).map(doctorMapper::toDto);
    }

    /**
     * Get one doctor by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<DoctorDTO> findOne(Long id) {
        LOG.debug("Request to get Doctor : {}", id);
        return doctorRepository.findOneWithEagerRelationships(id).map(doctorMapper::toDto);
    }

    /**
     * Delete the doctor by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Doctor : {}", id);
        doctorRepository.deleteById(id);
    }
}
