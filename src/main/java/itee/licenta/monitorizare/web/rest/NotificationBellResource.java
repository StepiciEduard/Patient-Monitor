package itee.licenta.monitorizare.web.rest;

import itee.licenta.monitorizare.domain.Notification;
import itee.licenta.monitorizare.repository.NotificationRepository;
import itee.licenta.monitorizare.repository.UserRepository;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Transactional
public class NotificationBellResource {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationBellResource.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationBellResource(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/notifications/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        List<Notification> unread = notificationRepository
            .findByRecipientUserIsCurrentUser()
            .stream()
            .filter(n -> !Boolean.TRUE.equals(n.getIsRead()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("count", (long) unread.size()));
    }

    @GetMapping("/notifications/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentNotifications() {
        List<Notification> all = notificationRepository.findByRecipientUserIsCurrentUser();
        List<Notification> recent = all
            .stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .limit(10)
            .collect(Collectors.toList());
        return ResponseEntity.ok(buildNotificationList(recent));
    }

    @GetMapping("/notifications/all")
    public ResponseEntity<List<Map<String, Object>>> getAllNotifications() {
        List<Notification> all = notificationRepository.findByRecipientUserIsCurrentUser();
        List<Notification> sorted = all
            .stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .limit(100)
            .collect(Collectors.toList());
        return ResponseEntity.ok(buildNotificationList(sorted));
    }

    @PutMapping("/notifications/{id}/mark-read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Optional<Notification> notifOpt = notificationRepository.findById(id);
        if (notifOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Notification notification = notifOpt.get();
        notification.setIsRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/notifications/mark-all-read")
    public ResponseEntity<Void> markAllAsRead() {
        List<Notification> unread = notificationRepository
            .findByRecipientUserIsCurrentUser()
            .stream()
            .filter(n -> !Boolean.TRUE.equals(n.getIsRead()))
            .collect(Collectors.toList());
        for (Notification n : unread) {
            n.setIsRead(true);
            notificationRepository.save(n);
        }
        LOG.info("Marked {} notifications as read", unread.size());
        return ResponseEntity.ok().build();
    }

    private List<Map<String, Object>> buildNotificationList(List<Notification> notifications) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Notification n : notifications) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", n.getId());
            item.put("type", n.getType().name());
            item.put("title", n.getTitle());
            item.put("message", n.getMessage());
            item.put("isRead", n.getIsRead());
            item.put("createdAt", n.getCreatedAt().toString());
            if (n.getRelatedPatient() != null && n.getRelatedPatient().getUser() != null) {
                item.put(
                    "patientName",
                    n.getRelatedPatient().getUser().getFirstName() + " " + n.getRelatedPatient().getUser().getLastName()
                );
            }
            result.add(item);
        }
        return result;
    }
}
