package itee.licenta.monitorizare.service;

import itee.licenta.monitorizare.domain.AppointmentSlot;
import itee.licenta.monitorizare.repository.AppointmentSlotRepository;
import itee.licenta.monitorizare.service.dto.AppointmentSlotDTO;
import itee.licenta.monitorizare.service.mapper.AppointmentSlotMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link itee.licenta.monitorizare.domain.AppointmentSlot}.
 */
@Service
@Transactional
public class AppointmentSlotService {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentSlotService.class);

    private final AppointmentSlotRepository appointmentSlotRepository;

    private final AppointmentSlotMapper appointmentSlotMapper;

    public AppointmentSlotService(AppointmentSlotRepository appointmentSlotRepository, AppointmentSlotMapper appointmentSlotMapper) {
        this.appointmentSlotRepository = appointmentSlotRepository;
        this.appointmentSlotMapper = appointmentSlotMapper;
    }

    /**
     * Save a appointmentSlot.
     *
     * @param appointmentSlotDTO the entity to save.
     * @return the persisted entity.
     */
    public AppointmentSlotDTO save(AppointmentSlotDTO appointmentSlotDTO) {
        LOG.debug("Request to save AppointmentSlot : {}", appointmentSlotDTO);
        AppointmentSlot appointmentSlot = appointmentSlotMapper.toEntity(appointmentSlotDTO);
        appointmentSlot = appointmentSlotRepository.save(appointmentSlot);
        return appointmentSlotMapper.toDto(appointmentSlot);
    }

    /**
     * Update a appointmentSlot.
     *
     * @param appointmentSlotDTO the entity to save.
     * @return the persisted entity.
     */
    public AppointmentSlotDTO update(AppointmentSlotDTO appointmentSlotDTO) {
        LOG.debug("Request to update AppointmentSlot : {}", appointmentSlotDTO);
        AppointmentSlot appointmentSlot = appointmentSlotMapper.toEntity(appointmentSlotDTO);
        appointmentSlot = appointmentSlotRepository.save(appointmentSlot);
        return appointmentSlotMapper.toDto(appointmentSlot);
    }

    /**
     * Partially update a appointmentSlot.
     *
     * @param appointmentSlotDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<AppointmentSlotDTO> partialUpdate(AppointmentSlotDTO appointmentSlotDTO) {
        LOG.debug("Request to partially update AppointmentSlot : {}", appointmentSlotDTO);

        return appointmentSlotRepository
            .findById(appointmentSlotDTO.getId())
            .map(existingAppointmentSlot -> {
                appointmentSlotMapper.partialUpdate(existingAppointmentSlot, appointmentSlotDTO);

                return existingAppointmentSlot;
            })
            .map(appointmentSlotRepository::save)
            .map(appointmentSlotMapper::toDto);
    }

    /**
     * Get all the appointmentSlots with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<AppointmentSlotDTO> findAllWithEagerRelationships(Pageable pageable) {
        return appointmentSlotRepository.findAllWithEagerRelationships(pageable).map(appointmentSlotMapper::toDto);
    }

    /**
     * Get one appointmentSlot by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<AppointmentSlotDTO> findOne(Long id) {
        LOG.debug("Request to get AppointmentSlot : {}", id);
        return appointmentSlotRepository.findOneWithEagerRelationships(id).map(appointmentSlotMapper::toDto);
    }

    /**
     * Delete the appointmentSlot by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete AppointmentSlot : {}", id);
        appointmentSlotRepository.deleteById(id);
    }
}
