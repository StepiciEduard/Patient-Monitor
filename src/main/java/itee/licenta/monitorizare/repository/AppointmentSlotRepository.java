package itee.licenta.monitorizare.repository;

import itee.licenta.monitorizare.domain.AppointmentSlot;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AppointmentSlot entity.
 */
@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long>, JpaSpecificationExecutor<AppointmentSlot> {
    default Optional<AppointmentSlot> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<AppointmentSlot> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<AppointmentSlot> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select appointmentSlot from AppointmentSlot appointmentSlot left join fetch appointmentSlot.doctor",
        countQuery = "select count(appointmentSlot) from AppointmentSlot appointmentSlot"
    )
    Page<AppointmentSlot> findAllWithToOneRelationships(Pageable pageable);

    @Query("select appointmentSlot from AppointmentSlot appointmentSlot left join fetch appointmentSlot.doctor")
    List<AppointmentSlot> findAllWithToOneRelationships();

    @Query(
        "select appointmentSlot from AppointmentSlot appointmentSlot left join fetch appointmentSlot.doctor where appointmentSlot.id =:id"
    )
    Optional<AppointmentSlot> findOneWithToOneRelationships(@Param("id") Long id);
}
