package itee.licenta.monitorizare.service;

import itee.licenta.monitorizare.domain.*; // for static metamodels
import itee.licenta.monitorizare.domain.Patient;
import itee.licenta.monitorizare.repository.PatientRepository;
import itee.licenta.monitorizare.service.criteria.PatientCriteria;
import itee.licenta.monitorizare.service.dto.PatientDTO;
import itee.licenta.monitorizare.service.mapper.PatientMapper;
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
 * Service for executing complex queries for {@link Patient} entities in the database.
 * The main input is a {@link PatientCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link PatientDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class PatientQueryService extends QueryService<Patient> {

    private static final Logger LOG = LoggerFactory.getLogger(PatientQueryService.class);

    private final PatientRepository patientRepository;

    private final PatientMapper patientMapper;

    public PatientQueryService(PatientRepository patientRepository, PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
    }

    /**
     * Return a {@link Page} of {@link PatientDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<PatientDTO> findByCriteria(PatientCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Patient> specification = createSpecification(criteria);
        return patientRepository.findAll(specification, page).map(patientMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(PatientCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Patient> specification = createSpecification(criteria);
        return patientRepository.count(specification);
    }

    /**
     * Function to convert {@link PatientCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Patient> createSpecification(PatientCriteria criteria) {
        Specification<Patient> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), Patient_.id),
                buildStringSpecification(criteria.getCnp(), Patient_.cnp),
                buildStringSpecification(criteria.getPhoneNumber(), Patient_.phoneNumber),
                buildStringSpecification(criteria.getAddress(), Patient_.address),
                buildSpecification(criteria.getPatientType(), Patient_.patientType),
                buildSpecification(criteria.getPatientSubtype(), Patient_.patientSubtype),
                buildRangeSpecification(criteria.getDateOfBirth(), Patient_.dateOfBirth),
                buildStringSpecification(criteria.getGender(), Patient_.gender),
                buildRangeSpecification(criteria.getHba1c(), Patient_.hba1c),
                buildRangeSpecification(criteria.getBmi(), Patient_.bmi),
                buildRangeSpecification(criteria.getFev1Baseline(), Patient_.fev1Baseline),
                buildSpecification(criteria.getUserId(), root -> root.join(Patient_.user, JoinType.LEFT).get(User_.id)),
                buildSpecification(criteria.getDoctorId(), root -> root.join(Patient_.doctor, JoinType.LEFT).get(Doctor_.id))
            );
        }
        return specification;
    }
}
