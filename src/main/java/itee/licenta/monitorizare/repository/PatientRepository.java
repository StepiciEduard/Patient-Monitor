package itee.licenta.monitorizare.repository;

import itee.licenta.monitorizare.domain.Patient;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Patient entity.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long>, JpaSpecificationExecutor<Patient> {
    default Optional<Patient> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Patient> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Patient> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select patient from Patient patient left join fetch patient.user left join fetch patient.doctor",
        countQuery = "select count(patient) from Patient patient"
    )
    Page<Patient> findAllWithToOneRelationships(Pageable pageable);

    @Query("select patient from Patient patient left join fetch patient.user left join fetch patient.doctor")
    List<Patient> findAllWithToOneRelationships();

    @Query("select patient from Patient patient left join fetch patient.user left join fetch patient.doctor where patient.id =:id")
    Optional<Patient> findOneWithToOneRelationships(@Param("id") Long id);

    @Query(
        value = "select patient from Patient patient left join fetch patient.user left join fetch patient.doctor where patient.doctor.id = :doctorId",
        countQuery = "select count(patient) from Patient patient where patient.doctor.id = :doctorId"
    )
    Page<Patient> findByDoctorId(@Param("doctorId") Long doctorId, Pageable pageable);

    @Query(
        "select patient from Patient patient left join fetch patient.user left join fetch patient.doctor where patient.user.login = :login"
    )
    Optional<Patient> findByUserLogin(@Param("login") String login);

    @Query(
        "select patient from Patient patient left join fetch patient.user left join fetch patient.doctor where patient.doctor.user.login = :login"
    )
    List<Patient> findByDoctorUserLogin(@Param("login") String login);
}
