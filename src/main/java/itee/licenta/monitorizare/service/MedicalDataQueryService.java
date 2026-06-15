package itee.licenta.monitorizare.service;

import itee.licenta.monitorizare.domain.*; // for static metamodels
import itee.licenta.monitorizare.domain.MedicalData;
import itee.licenta.monitorizare.repository.MedicalDataRepository;
import itee.licenta.monitorizare.service.criteria.MedicalDataCriteria;
import itee.licenta.monitorizare.service.dto.MedicalDataDTO;
import itee.licenta.monitorizare.service.mapper.MedicalDataMapper;
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
 * Service for executing complex queries for {@link MedicalData} entities in the database.
 * The main input is a {@link MedicalDataCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link MedicalDataDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class MedicalDataQueryService extends QueryService<MedicalData> {

    private static final Logger LOG = LoggerFactory.getLogger(MedicalDataQueryService.class);

    private final MedicalDataRepository medicalDataRepository;

    private final MedicalDataMapper medicalDataMapper;

    public MedicalDataQueryService(MedicalDataRepository medicalDataRepository, MedicalDataMapper medicalDataMapper) {
        this.medicalDataRepository = medicalDataRepository;
        this.medicalDataMapper = medicalDataMapper;
    }

    /**
     * Return a {@link Page} of {@link MedicalDataDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<MedicalDataDTO> findByCriteria(MedicalDataCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<MedicalData> specification = createSpecification(criteria);
        return medicalDataRepository.findAll(specification, page).map(medicalDataMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(MedicalDataCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<MedicalData> specification = createSpecification(criteria);
        return medicalDataRepository.count(specification);
    }

    /**
     * Function to convert {@link MedicalDataCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<MedicalData> createSpecification(MedicalDataCriteria criteria) {
        Specification<MedicalData> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), MedicalData_.id),
                buildRangeSpecification(criteria.getTimestamp(), MedicalData_.timestamp),
                buildRangeSpecification(criteria.getHeartRate(), MedicalData_.heartRate),
                buildRangeSpecification(criteria.getSpo2(), MedicalData_.spo2),
                buildRangeSpecification(criteria.getTemperature(), MedicalData_.temperature),
                buildRangeSpecification(criteria.getSystolicBp(), MedicalData_.systolicBp),
                buildRangeSpecification(criteria.getDiastolicBp(), MedicalData_.diastolicBp),
                buildRangeSpecification(criteria.getHrv(), MedicalData_.hrv),
                buildRangeSpecification(criteria.getQtInterval(), MedicalData_.qtInterval),
                buildRangeSpecification(criteria.getBnp(), MedicalData_.bnp),
                buildRangeSpecification(criteria.getBloodGlucose(), MedicalData_.bloodGlucose),
                buildRangeSpecification(criteria.getRespiratoryRate(), MedicalData_.respiratoryRate),
                buildRangeSpecification(criteria.getFev1(), MedicalData_.fev1),
                buildRangeSpecification(criteria.getEtco2(), MedicalData_.etco2),
                buildRangeSpecification(criteria.getAnomalyScore(), MedicalData_.anomalyScore),
                buildSpecification(criteria.getIsAnomaly(), MedicalData_.isAnomaly),
                buildSpecification(criteria.getPatientId(), root -> root.join(MedicalData_.patient, JoinType.LEFT).get(Patient_.id))
            );
        }
        return specification;
    }
}
