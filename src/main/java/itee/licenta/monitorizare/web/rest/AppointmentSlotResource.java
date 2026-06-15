package itee.licenta.monitorizare.web.rest;

import itee.licenta.monitorizare.repository.AppointmentSlotRepository;
import itee.licenta.monitorizare.service.AppointmentSlotQueryService;
import itee.licenta.monitorizare.service.AppointmentSlotService;
import itee.licenta.monitorizare.service.criteria.AppointmentSlotCriteria;
import itee.licenta.monitorizare.service.dto.AppointmentSlotDTO;
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
 * REST controller for managing {@link itee.licenta.monitorizare.domain.AppointmentSlot}.
 */
@RestController
@RequestMapping("/api/appointment-slots")
public class AppointmentSlotResource {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentSlotResource.class);

    private static final String ENTITY_NAME = "appointmentSlot";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AppointmentSlotService appointmentSlotService;

    private final AppointmentSlotRepository appointmentSlotRepository;

    private final AppointmentSlotQueryService appointmentSlotQueryService;

    public AppointmentSlotResource(
        AppointmentSlotService appointmentSlotService,
        AppointmentSlotRepository appointmentSlotRepository,
        AppointmentSlotQueryService appointmentSlotQueryService
    ) {
        this.appointmentSlotService = appointmentSlotService;
        this.appointmentSlotRepository = appointmentSlotRepository;
        this.appointmentSlotQueryService = appointmentSlotQueryService;
    }

    /**
     * {@code POST  /appointment-slots} : Create a new appointmentSlot.
     *
     * @param appointmentSlotDTO the appointmentSlotDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new appointmentSlotDTO, or with status {@code 400 (Bad Request)} if the appointmentSlot has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<AppointmentSlotDTO> createAppointmentSlot(@Valid @RequestBody AppointmentSlotDTO appointmentSlotDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save AppointmentSlot : {}", appointmentSlotDTO);
        if (appointmentSlotDTO.getId() != null) {
            throw new BadRequestAlertException("A new appointmentSlot cannot already have an ID", ENTITY_NAME, "idexists");
        }
        appointmentSlotDTO = appointmentSlotService.save(appointmentSlotDTO);
        return ResponseEntity.created(new URI("/api/appointment-slots/" + appointmentSlotDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, appointmentSlotDTO.getId().toString()))
            .body(appointmentSlotDTO);
    }

    /**
     * {@code PUT  /appointment-slots/:id} : Updates an existing appointmentSlot.
     *
     * @param id the id of the appointmentSlotDTO to save.
     * @param appointmentSlotDTO the appointmentSlotDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated appointmentSlotDTO,
     * or with status {@code 400 (Bad Request)} if the appointmentSlotDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the appointmentSlotDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentSlotDTO> updateAppointmentSlot(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AppointmentSlotDTO appointmentSlotDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update AppointmentSlot : {}, {}", id, appointmentSlotDTO);
        if (appointmentSlotDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, appointmentSlotDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!appointmentSlotRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        appointmentSlotDTO = appointmentSlotService.update(appointmentSlotDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, appointmentSlotDTO.getId().toString()))
            .body(appointmentSlotDTO);
    }

    /**
     * {@code PATCH  /appointment-slots/:id} : Partial updates given fields of an existing appointmentSlot, field will ignore if it is null
     *
     * @param id the id of the appointmentSlotDTO to save.
     * @param appointmentSlotDTO the appointmentSlotDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated appointmentSlotDTO,
     * or with status {@code 400 (Bad Request)} if the appointmentSlotDTO is not valid,
     * or with status {@code 404 (Not Found)} if the appointmentSlotDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the appointmentSlotDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AppointmentSlotDTO> partialUpdateAppointmentSlot(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody AppointmentSlotDTO appointmentSlotDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update AppointmentSlot partially : {}, {}", id, appointmentSlotDTO);
        if (appointmentSlotDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, appointmentSlotDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!appointmentSlotRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AppointmentSlotDTO> result = appointmentSlotService.partialUpdate(appointmentSlotDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, appointmentSlotDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /appointment-slots} : get all the appointmentSlots.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of appointmentSlots in body.
     */
    @GetMapping("")
    public ResponseEntity<List<AppointmentSlotDTO>> getAllAppointmentSlots(
        AppointmentSlotCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get AppointmentSlots by criteria: {}", criteria);

        Page<AppointmentSlotDTO> page = appointmentSlotQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /appointment-slots/count} : count all the appointmentSlots.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countAppointmentSlots(AppointmentSlotCriteria criteria) {
        LOG.debug("REST request to count AppointmentSlots by criteria: {}", criteria);
        return ResponseEntity.ok().body(appointmentSlotQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /appointment-slots/:id} : get the "id" appointmentSlot.
     *
     * @param id the id of the appointmentSlotDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the appointmentSlotDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentSlotDTO> getAppointmentSlot(@PathVariable("id") Long id) {
        LOG.debug("REST request to get AppointmentSlot : {}", id);
        Optional<AppointmentSlotDTO> appointmentSlotDTO = appointmentSlotService.findOne(id);
        return ResponseUtil.wrapOrNotFound(appointmentSlotDTO);
    }

    /**
     * {@code DELETE  /appointment-slots/:id} : delete the "id" appointmentSlot.
     *
     * @param id the id of the appointmentSlotDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointmentSlot(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete AppointmentSlot : {}", id);
        appointmentSlotService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
