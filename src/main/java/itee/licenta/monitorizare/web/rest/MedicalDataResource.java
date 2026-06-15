package itee.licenta.monitorizare.web.rest;

import itee.licenta.monitorizare.repository.MedicalDataRepository;
import itee.licenta.monitorizare.service.MedicalDataQueryService;
import itee.licenta.monitorizare.service.MedicalDataService;
import itee.licenta.monitorizare.service.criteria.MedicalDataCriteria;
import itee.licenta.monitorizare.service.dto.MedicalDataDTO;
import itee.licenta.monitorizare.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link itee.licenta.monitorizare.domain.MedicalData}.
 */
@RestController
@RequestMapping("/api/medical-data")
public class MedicalDataResource {

    private static final Logger LOG = LoggerFactory.getLogger(MedicalDataResource.class);

    private static final String ENTITY_NAME = "medicalData";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MedicalDataService medicalDataService;

    private final MedicalDataRepository medicalDataRepository;

    private final MedicalDataQueryService medicalDataQueryService;

    public MedicalDataResource(
        MedicalDataService medicalDataService,
        MedicalDataRepository medicalDataRepository,
        MedicalDataQueryService medicalDataQueryService
    ) {
        this.medicalDataService = medicalDataService;
        this.medicalDataRepository = medicalDataRepository;
        this.medicalDataQueryService = medicalDataQueryService;
    }

    /**
     * {@code POST  /medical-data} : Create a new medicalData.
     *
     * @param medicalDataDTO the medicalDataDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new medicalDataDTO, or with status {@code 400 (Bad Request)} if the medicalData has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<MedicalDataDTO> createMedicalData(@Valid @RequestBody MedicalDataDTO medicalDataDTO) throws URISyntaxException {
        LOG.debug("REST request to save MedicalData : {}", medicalDataDTO);
        if (medicalDataDTO.getId() != null) {
            throw new BadRequestAlertException("A new medicalData cannot already have an ID", ENTITY_NAME, "idexists");
        }
        medicalDataDTO = medicalDataService.save(medicalDataDTO);
        return ResponseEntity.created(new URI("/api/medical-data/" + medicalDataDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, medicalDataDTO.getId().toString()))
            .body(medicalDataDTO);
    }

    /**
     * {@code PUT  /medical-data/:id} : Updates an existing medicalData.
     *
     * @param id the id of the medicalDataDTO to save.
     * @param medicalDataDTO the medicalDataDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated medicalDataDTO,
     * or with status {@code 400 (Bad Request)} if the medicalDataDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the medicalDataDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MedicalDataDTO> updateMedicalData(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody MedicalDataDTO medicalDataDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update MedicalData : {}, {}", id, medicalDataDTO);
        if (medicalDataDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, medicalDataDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!medicalDataRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        medicalDataDTO = medicalDataService.update(medicalDataDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, medicalDataDTO.getId().toString()))
            .body(medicalDataDTO);
    }

    /**
     * {@code PATCH  /medical-data/:id} : Partial updates given fields of an existing medicalData, field will ignore if it is null
     *
     * @param id the id of the medicalDataDTO to save.
     * @param medicalDataDTO the medicalDataDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated medicalDataDTO,
     * or with status {@code 400 (Bad Request)} if the medicalDataDTO is not valid,
     * or with status {@code 404 (Not Found)} if the medicalDataDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the medicalDataDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<MedicalDataDTO> partialUpdateMedicalData(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody MedicalDataDTO medicalDataDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update MedicalData partially : {}, {}", id, medicalDataDTO);
        if (medicalDataDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, medicalDataDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!medicalDataRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MedicalDataDTO> result = medicalDataService.partialUpdate(medicalDataDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, medicalDataDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /medical-data} : get all the medicalData.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of medicalData in body.
     */
    @GetMapping("")
    public ResponseEntity<List<MedicalDataDTO>> getAllMedicalData(
        MedicalDataCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get MedicalData by criteria: {}", criteria);

        Page<MedicalDataDTO> page = medicalDataService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /medical-data/count} : count all the medicalData.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countMedicalData(MedicalDataCriteria criteria) {
        LOG.debug("REST request to count MedicalData by criteria: {}", criteria);
        return ResponseEntity.ok().body(medicalDataQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /medical-data/:id} : get the "id" medicalData.
     *
     * @param id the id of the medicalDataDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the medicalDataDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MedicalDataDTO> getMedicalData(@PathVariable("id") Long id) {
        LOG.debug("REST request to get MedicalData : {}", id);
        Optional<MedicalDataDTO> medicalDataDTO = medicalDataService.findOne(id);
        return ResponseUtil.wrapOrNotFound(medicalDataDTO);
    }

    /**
     * {@code DELETE  /medical-data/:id} : delete the "id" medicalData.
     *
     * @param id the id of the medicalDataDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicalData(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete MedicalData : {}", id);
        medicalDataService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
