package edu.upc.labpilot.controller;

import edu.upc.labpilot.dto.ContactRequest;
import edu.upc.labpilot.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ContactController {

    private final EmailService emailService;

    public ContactController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/contacto")
    public ResponseEntity<?> enviarContacto(@RequestBody ContactRequest request) {
        if (request == null || request.getMensaje() == null || request.getMensaje().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Mensaje vacío");
        }

        try {
            emailService.enviarContactoSoporte(
                request.getNombre(),
                request.getCorreo(),
                request.getAsunto(),
                request.getMensaje()
            );
            return ResponseEntity.ok().body("Correo enviado");
        } catch (Exception e) {
            // Opcional: logger.error("...", e);
            return ResponseEntity.status(500).body("Error enviando correo");
        }
    }
}
