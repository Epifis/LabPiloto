package edu.upc.labpilot.controller;

import edu.upc.labpilot.dto.AdminSolicitudRequest;
import edu.upc.labpilot.model.SolicitudAdmin;
import edu.upc.labpilot.model.Usuario;
import edu.upc.labpilot.repository.SolicitudAdminRepository;
import edu.upc.labpilot.repository.UsuarioRepository;
import edu.upc.labpilot.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private SolicitudAdminRepository solicitudRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/solicitar")
    public ResponseEntity<String> solicitarAdmin(@RequestBody AdminSolicitudRequest request) {
        try {
            // Validaciones básicas
            if (request.getNombre() == null || request.getNombre().trim().isEmpty())
                return ResponseEntity.badRequest().body("El nombre es obligatorio");
            if (request.getApellido() == null || request.getApellido().trim().isEmpty())
                return ResponseEntity.badRequest().body("El apellido es obligatorio");
            if (request.getCorreo() == null || request.getCorreo().trim().isEmpty())
                return ResponseEntity.badRequest().body("El correo es obligatorio");
            if (request.getPassword() == null || request.getPassword().trim().isEmpty())
                return ResponseEntity.badRequest().body("La contraseña es obligatoria");

            // Verificar duplicados
            if (solicitudRepository.existsByCorreo(request.getCorreo()))
                return ResponseEntity.badRequest().body("Ya existe una solicitud pendiente con este correo");
            if (usuarioRepository.existsByCorreo(request.getCorreo()))
                return ResponseEntity.badRequest().body("Ya existe un usuario registrado con este correo");

            // Crear solicitud
            SolicitudAdmin solicitud = new SolicitudAdmin();
            solicitud.setNombre(request.getNombre().trim());
            solicitud.setApellido(request.getApellido().trim());
            solicitud.setCorreo(request.getCorreo().trim());
            solicitud.setPassword(passwordEncoder.encode(request.getPassword()));
            solicitud.setEstado("Pendiente");
            solicitud.setFechaSolicitud(LocalDateTime.now());
            solicitud.setTokenValidacion(UUID.randomUUID().toString());

            solicitudRepository.save(solicitud);

            // Enviar correo de validación
            String nombreCompleto = solicitud.getNombre() + " " + solicitud.getApellido();
            emailService.enviarSolicitudRegistro(
                nombreCompleto,
                solicitud.getCorreo(),
                "ADMIN",
                solicitud.getTokenValidacion()
            );

            return ResponseEntity.ok("Solicitud enviada correctamente. El administrador recibirá un correo para validarla.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al procesar la solicitud: " + e.getMessage());
        }
    }

    @GetMapping("/aprobar/{token}")
    public ResponseEntity<String> aprobarSolicitud(@PathVariable String token) {
        try {
            Optional<SolicitudAdmin> solicitudOpt = solicitudRepository.findByTokenValidacion(token);

            if (solicitudOpt.isEmpty()) {
                return htmlError("Token inválido", "No se encontró ninguna solicitud con este token.");
            }

            SolicitudAdmin solicitud = solicitudOpt.get();

            if (!solicitud.getEstado().equals("Pendiente")) {
                return htmlError("Solicitud ya procesada",
                        "Esta solicitud ya fue " + solicitud.getEstado().toLowerCase() + ".");
            }

            // Crear usuario administrador
            Usuario nuevoAdmin = new Usuario();
            nuevoAdmin.setNombre(solicitud.getNombre());
            nuevoAdmin.setApellido(solicitud.getApellido());
            nuevoAdmin.setCorreo(solicitud.getCorreo());
            nuevoAdmin.setPassword(solicitud.getPassword()); // ya está hasheada
            nuevoAdmin.setRol("administrador");
            usuarioRepository.save(nuevoAdmin);

            // Actualizar estado y fecha
            solicitud.setEstado("Aprobada");
            solicitud.setFechaSolicitud(LocalDateTime.now());
            solicitudRepository.save(solicitud);

            // Notificar por correo
            String nombreCompleto = solicitud.getNombre() + " " + solicitud.getApellido();
            emailService.notificarAprobacion(solicitud.getCorreo(), nombreCompleto);

            return htmlOk(
                "Solicitud Aprobada",
                "¡Solicitud Aprobada!",
                "El usuario ha sido creado exitosamente como administrador."
            );

        } catch (Exception e) {
            e.printStackTrace();
            return htmlError("Error al procesar la solicitud", e.getMessage());
        }
    }

    @GetMapping("/rechazar/{token}")
    public ResponseEntity<String> rechazarSolicitud(@PathVariable String token) {
        try {
            Optional<SolicitudAdmin> solicitudOpt = solicitudRepository.findByTokenValidacion(token);

            if (solicitudOpt.isEmpty()) {
                return htmlError("Token inválido", "No se encontró ninguna solicitud con este token.");
            }

            SolicitudAdmin solicitud = solicitudOpt.get();

            if (!solicitud.getEstado().equals("Pendiente")) {
                return htmlError("Solicitud ya procesada",
                        "Esta solicitud ya fue " + solicitud.getEstado().toLowerCase() + ".");
            }

            solicitud.setEstado("Rechazada");
            solicitud.setFechaSolicitud(LocalDateTime.now());
            solicitudRepository.save(solicitud);

            // Notificar rechazo
            String nombreCompleto = solicitud.getNombre() + " " + solicitud.getApellido();
            emailService.notificarRechazo(solicitud.getCorreo(), nombreCompleto);

            return htmlOk(
                "Solicitud Rechazada",
                "Solicitud Rechazada",
                "El solicitante ha sido notificado del rechazo de su solicitud."
            );

        } catch (Exception e) {
            e.printStackTrace();
            return htmlError("Error al procesar la solicitud", e.getMessage());
        }
    }

    // Métodos auxiliares para HTML coherente y limpio
    private ResponseEntity<String> htmlOk(String title, String heading, String message) {
        return ResponseEntity.ok(
            "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" + title + "</title>" +
            "<style>body{font-family:Arial,sans-serif;background:#f5f5f5;margin:0;padding:40px;}" +
            ".container{background:#fff;padding:30px;border-radius:10px;max-width:600px;margin:auto;" +
            "box-shadow:0 2px 8px rgba(0,0,0,0.1);}h1{text-align:center;color:#333;}p{text-align:center;color:#555;}" +
            ".ok{color:#28a745;font-size:60px;text-align:center;}</style></head><body>" +
            "<div class='container'><div class='ok'>✔</div><h1>" + heading + "</h1>" +
            "<p>" + message + "</p></div></body></html>"
        );
    }

    private ResponseEntity<String> htmlError(String heading, String message) {
        return ResponseEntity.badRequest().body(
            "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Error</title>" +
            "<style>body{font-family:Arial,sans-serif;background:#f8d7da;padding:40px;}" +
            ".container{background:#fff;padding:30px;border-radius:10px;max-width:600px;margin:auto;" +
            "box-shadow:0 2px 8px rgba(0,0,0,0.1);}h1{text-align:center;color:#721c24;}" +
            "p{text-align:center;color:#721c24;}</style></head><body>" +
            "<div class='container'><h1>" + heading + "</h1><p>" + message + "</p></div></body></html>"
        );
    }
}
