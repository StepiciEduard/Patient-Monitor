package itee.licenta.monitorizare.web.rest;

import itee.licenta.monitorizare.security.SecurityUtils;
import itee.licenta.monitorizare.service.ChatbotService;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotResource {

    private static final Logger LOG = LoggerFactory.getLogger(ChatbotResource.class);

    private final ChatbotService chatbotService;

    public ChatbotResource(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/message")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody Map<String, Object> request) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("User not logged in"));

        String message = (String) request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mesajul nu poate fi gol"));
        }

        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) request.getOrDefault("history", new ArrayList<>());

        // Determine role
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isDoctor = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));
        boolean isPatient = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));

        String response;
        if (isPatient) {
            response = chatbotService.chatAsPatient(login, message, history);
        } else if (isDoctor) {
            response = chatbotService.chatAsDoctor(login, message, history);
        } else {
            response = "Chatbot-ul este disponibil doar pentru pacienti si doctori.";
        }

        LOG.debug("Chatbot message from {} ({}): {}", login, isDoctor ? "DOCTOR" : "PATIENT", message);

        return ResponseEntity.ok(Map.of("response", response));
    }
}
