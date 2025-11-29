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
            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El nombre es obligatorio");
            }
            if (request.getApellido() == null || request.getApellido().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El apellido es obligatorio");
            }
            if (request.getCorreo() == null || request.getCorreo().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El correo es obligatorio");
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La contraseña es obligatoria");
            }

            // Verificar duplicados
            if (solicitudRepository.existsByCorreo(request.getCorreo())) {
                return ResponseEntity.badRequest().body("Ya existe una solicitud pendiente con este correo");
            }
            if (usuarioRepository.existsByCorreo(request.getCorreo())) {
                return ResponseEntity.badRequest().body("Ya existe un usuario registrado con este correo");
            }

            SolicitudAdmin solicitud = new SolicitudAdmin();
            solicitud.setNombre(request.getNombre().trim());
            solicitud.setApellido(request.getApellido().trim());
            solicitud.setCorreo(request.getCorreo().trim());
            solicitud.setPassword(passwordEncoder.encode(request.getPassword()));
            solicitud.setEstado("Pendiente");
            solicitud.setFechaSolicitud(LocalDateTime.now());
            solicitud.setTokenValidacion(UUID.randomUUID().toString());

            solicitudRepository.save(solicitud);

            // ✅ CORREGIDO: Notificar al SuperAdmin (NO enviar contrato todavía)
            emailService.notificarSolicitudAdmin(
                    solicitud.getNombre() + " " + solicitud.getApellido(),
                    solicitud.getCorreo(),
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

            // NO crear usuario todavía, solo pre-aprobar
            solicitud.setEstado("PreAprobada");
            solicitudRepository.save(solicitud);

            //Enviar contrato al SOLICITANTE para que firme
            String nombreCompleto = solicitud.getNombre() + " " + solicitud.getApellido();
            emailService.enviarContratoSolicitante(
                    solicitud.getCorreo(),
                    nombreCompleto,
                    solicitud.getTokenValidacion()
            );

            return htmlOk(
                    "Solicitud Pre-Aprobada",
                    "¡Solicitud Pre-Aprobada!",
                    "Se ha enviado el contrato de responsabilidad al solicitante para su firma."
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
            emailService.notificarRechazoAdmin(solicitud.getCorreo(), nombreCompleto);

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
                "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" + title + "</title>"
                + "<style>body{font-family:Arial,sans-serif;background:#f5f5f5;margin:0;padding:40px;}"
                + ".container{background:#fff;padding:30px;border-radius:10px;max-width:600px;margin:auto;"
                + "box-shadow:0 2px 8px rgba(0,0,0,0.1);}h1{text-align:center;color:#333;}p{text-align:center;color:#555;}"
                + ".ok{color:#28a745;font-size:60px;text-align:center;}</style></head><body>"
                + "<div class='container'><div class='ok'>✔</div><h1>" + heading + "</h1>"
                + "<p>" + message + "</p></div></body></html>"
        );
    }

    private ResponseEntity<String> htmlError(String heading, String message) {
        return ResponseEntity.badRequest().body(
                "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Error</title>"
                + "<style>body{font-family:Arial,sans-serif;background:#f8d7da;padding:40px;}"
                + ".container{background:#fff;padding:30px;border-radius:10px;max-width:600px;margin:auto;"
                + "box-shadow:0 2px 8px rgba(0,0,0,0.1);}h1{text-align:center;color:#721c24;}"
                + "p{text-align:center;color:#721c24;}</style></head><body>"
                + "<div class='container'><h1>" + heading + "</h1><p>" + message + "</p></div></body></html>"
        );
    }

    @PostMapping("/firmar-contrato/{token}")
    public ResponseEntity<String> firmarContratoAdmin(@PathVariable String token,
            @RequestParam String firmaDigital) {
        try {
            Optional<SolicitudAdmin> solicitudOpt = solicitudRepository.findByTokenValidacion(token);

            if (solicitudOpt.isEmpty()) {
                return htmlError("Token inválido", "No se encontró ninguna solicitud con este token.");
            }

            SolicitudAdmin solicitud = solicitudOpt.get();

            if (!solicitud.getEstado().equals("PreAprobada")) {
                return htmlError("Solicitud no pre-aprobada",
                        "Esta solicitud no está en estado de pre-aprobación.");
            }

            Usuario nuevoAdmin = new Usuario();
            nuevoAdmin.setNombre(solicitud.getNombre());
            nuevoAdmin.setApellido(solicitud.getApellido());
            nuevoAdmin.setCorreo(solicitud.getCorreo());
            nuevoAdmin.setPassword(solicitud.getPassword());
            nuevoAdmin.setRol("administrador");
            nuevoAdmin.setActivo(true);
            nuevoAdmin.setCorreoVerificado(true);
            nuevoAdmin.setMfaHabilitado(true);

            usuarioRepository.save(nuevoAdmin);

            // Actualizar estado final
            solicitud.setEstado("Aprobada");
            solicitudRepository.save(solicitud);

            // Notificar activación
            String nombreCompleto = solicitud.getNombre() + " " + solicitud.getApellido();
            emailService.notificarAdminActivado(solicitud.getCorreo(), nombreCompleto);

            return htmlOk(
                    "Contrato Firmado",
                    "¡Cuenta Activada Exitosamente!",
                    "El usuario ha sido activado como administrador. Ya puede iniciar sesión."
            );

        } catch (Exception e) {
            e.printStackTrace();
            return htmlError("Error al procesar la firma", e.getMessage());
        }
    }
    // ✅ NUEVO: Agregar ANTES del método POST
@GetMapping("/firmar-contrato/{token}")
public ResponseEntity<String> mostrarFormularioContrato(@PathVariable String token) {
    try {
        Optional<SolicitudAdmin> solicitudOpt = solicitudRepository.findByTokenValidacion(token);

        if (solicitudOpt.isEmpty()) {
            return htmlError("Token inválido", "No se encontró ninguna solicitud con este token.");
        }

        SolicitudAdmin solicitud = solicitudOpt.get();

        if (!solicitud.getEstado().equals("PreAprobada")) {
            return htmlError("Solicitud no disponible",
                    "Esta solicitud no está en estado de pre-aprobación. Estado actual: " + solicitud.getEstado());
        }

        String nombreCompleto = solicitud.getNombre() + " " + solicitud.getApellido();
        return htmlContratoForm(nombreCompleto, solicitud.getCorreo(), token);

    } catch (Exception e) {
        e.printStackTrace();
        return htmlError("Error al cargar el contrato", e.getMessage());
    }
}

// Método auxiliar para mostrar formulario de contrato
    private ResponseEntity<String> htmlContratoForm(String nombre, String correo, String token) {
        return ResponseEntity.ok(
                "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Contrato de Responsabilidad</title>"
                + "<style>body{font-family:Arial,sans-serif;background:#f5f5f5;margin:0;padding:20px;}"
                + ".container{background:#fff;padding:30px;border-radius:10px;max-width:800px;margin:auto;"
                + "box-shadow:0 2px 8px rgba(0,0,0,0.1);}h1{text-align:center;color:#333;}h2{color:#555;}"
                + ".contrato{border:1px solid #ccc;padding:20px;margin:20px 0;max-height:400px;overflow-y:auto;}"
                + ".firma-form{margin-top:20px;padding:20px;background:#f9f9f9;border-radius:5px;}"
                + "button{background:#28a745;color:white;padding:12px 24px;border:none;border-radius:5px;cursor:pointer;}"
                + "button:hover{background:#218838;}</style></head><body>"
                + "<div class='container'>"
                + "<h1>📋 Contrato de Responsabilidad</h1>"
                + "<p><strong>Nombre:</strong> " + nombre + "</p>"
                + "<p><strong>Correo:</strong> " + correo + "</p>"
                + "<div class='contrato'>"
                + "<h2>CONTRATO DE RESPONSABILIDAD PARA ADMINISTRADORES</h2>"
                + "<p>Como administrador del Sistema de Laboratorios LabPilot, usted será responsable de:</p>"
                + "<ul>"
                + "<li>Gestionar usuarios, laboratorios y equipos del sistema</li>"
                + "<li>Aprobar/rechazar reservas y préstamos</li>"
                + "<li>Mantener la integridad y seguridad del sistema</li>"
                + "<li>Proteger la información confidencial de usuarios</li>"
                + "<li>Cumplir con las políticas de seguridad institucionales</li>"
                + "</ul>"
                + "<p><strong>Al firmar este contrato, acepta utilizar sus privilegios de manera ética y responsable.</strong></p>"
                + "</div>"
                + "<div class='firma-form'>"
                + "<form method='POST' action='/api/admins/firmar-contrato/" + token + "'>"
                + "<input type='hidden' name='firmaDigital' value='firma_aceptada_" + System.currentTimeMillis() + "'>"
                + "<button type='submit'>✅ Firmar Contrato y Activar Cuenta</button>"
                + "</form>"
                + "</div>"
                + "</div></body></html>"
        );
    }
}
