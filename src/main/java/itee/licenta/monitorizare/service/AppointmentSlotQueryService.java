package itee.licenta.monitorizare.service;

import itee.licenta.monitorizare.domain.*; // for static metamodels
import itee.licenta.monitorizare.domain.AppointmentSlot;
import itee.licenta.monitorizare.repository.AppointmentSlotRepository;
import itee.licenta.monitorizare.service.criteria.AppointmentSlotCriteria;
import itee.licenta.monitorizare.service.dto.AppointmentSlotDTO;
import itee.licenta.monitorizare.service.mapper.AppointmentSlotMapper;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link AppointmentSlot} entities in the database.
 * The main input is a {@link AppointmentSlotCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link AppointmentSlotDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class AppointmentSlotQueryService extends QueryService<AppointmentSlot> {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentSlotQueryService.class);

    private final AppointmentSlotRepository appointmentSlotRepository;

    private final AppointmentSlotMapper appointmentSlotMapper;

    public AppointmentSlotQueryService(AppointmentSlotRepository appointmentSlotRepository, AppointmentSlotMapper appointmentSlotMapper) {
        this.appointmentSlotRepository = appointmentSlotRepository;
        this.appointmentSlotMapper = appointmentSlotMapper;
    }

    /**
     * Return a {@link Page} of {@link AppointmentSlotDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AppointmentSlotDTO> findByCriteria(AppointmentSlotCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<AppointmentSlot> specification = createSpecification(criteria);
        return appointmentSlotRepository.findAll(specification, page).map(appointmentSlotMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(AppointmentSlotCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<AppointmentSlot> specification = createSpecification(criteria);
        return appointmentSlotRepository.count(specification);
    }

    /**
     * Function to convert {@link AppointmentSlotCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<AppointmentSlot> createSpecification(AppointmentSlotCriteria criteria) {
        Specification<AppointmentSlot> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), AppointmentSlot_.id),
                buildRangeSpecification(criteria.getStartTime(), AppointmentSlot_.startTime),
                buildRangeSpecification(criteria.getEndTime(), AppointmentSlot_.endTime),
                buildSpecification(criteria.getIsAvailable(), AppointmentSlot_.isAvailable),
                buildSpecification(criteria.getDoctorId(), root -> root.join(AppointmentSlot_.doctor, JoinType.LEFT).get(Doctor_.id))
            );
        }
        return specification;
    }
}
