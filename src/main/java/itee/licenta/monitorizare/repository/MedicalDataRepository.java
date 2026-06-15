package itee.licenta.monitorizare.repository;

import itee.licenta.monitorizare.domain.MedicalData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the MedicalData entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MedicalDataRepository extends JpaRepository<MedicalData, Long>, JpaSpecificationExecutor<MedicalData> {
    @Query(
        value = "select md from MedicalData md where md.patient.id = :patientId order by md.timestamp desc",
        countQuery = "select count(md) from MedicalData md where md.patient.id = :patientId"
    )
    Page<MedicalData> findByPatientId(@Param("patientId") Long patientId, Pageable pageable);

    @Query(
        value = "select md from MedicalData md where md.patient.doctor.id = :doctorId order by md.timestamp desc",
        countQuery = "select count(md) from MedicalData md where md.patient.doctor.id = :doctorId"
    )
    Page<MedicalData> findByDoctorId(@Param("doctorId") Long doctorId, Pageable pageable);

    @Query(
        value = "select md from MedicalData md where md.patient.user.login = :login order by md.timestamp desc",
        countQuery = "select count(md) from MedicalData md where md.patient.user.login = :login"
    )
    Page<MedicalData> findByPatientUserLogin(@Param("login") String login, Pageable pageable);

    java.util.List<MedicalData> findByPatientIdAndTimestampAfterOrderByTimestampAsc(Long patientId, java.time.Instant timestamp);

    // Delete all medical data for a patient
    @Modifying
    @Query("delete from MedicalData md where md.patient.id = :patientId")
    void deleteByPatientId(@Param("patientId") Long patientId);
}
