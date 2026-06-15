package itee.licenta.monitorizare.repository;

import itee.licenta.monitorizare.domain.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Notification entity.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
    @Query("select notification from Notification notification where notification.recipientUser.login = ?#{authentication.name}")
    List<Notification> findByRecipientUserIsCurrentUser();

    @Query("select notification from Notification notification where notification.senderUser.login = ?#{authentication.name}")
    List<Notification> findBySenderUserIsCurrentUser();

    default Optional<Notification> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Notification> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Notification> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select notification from Notification notification left join fetch notification.recipientUser left join fetch notification.senderUser",
        countQuery = "select count(notification) from Notification notification"
    )
    Page<Notification> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select notification from Notification notification left join fetch notification.recipientUser left join fetch notification.senderUser"
    )
    List<Notification> findAllWithToOneRelationships();

    @Query(
        "select notification from Notification notification left join fetch notification.recipientUser left join fetch notification.senderUser where notification.id =:id"
    )
    Optional<Notification> findOneWithToOneRelationships(@Param("id") Long id);

    @Query(
        value = "select notification from Notification notification left join fetch notification.recipientUser left join fetch notification.senderUser where notification.recipientUser.login = ?#{authentication.name} order by notification.createdAt desc",
        countQuery = "select count(notification) from Notification notification where notification.recipientUser.login = ?#{authentication.name}"
    )
    Page<Notification> findByRecipientUserIsCurrentUserPaged(Pageable pageable);

    // Delete all notifications where user is recipient OR sender
    @Modifying
    @Query("delete from Notification n where n.recipientUser.id = :userId")
    void deleteByRecipientUserId(@Param("userId") Long userId);

    @Modifying
    @Query("delete from Notification n where n.senderUser.id = :userId")
    void deleteBySenderUserId(@Param("userId") Long userId);

    // Delete all notifications for a specific patient (by patient's user id)
    @Modifying
    @Query("delete from Notification n where n.relatedPatient.id = :patientId")
    void deleteByPatientId(@Param("patientId") Long patientId);
}
