
package edu.upc.labpilot.controller.auth;

import edu.upc.labpilot.model.ContratoUso;
import edu.upc.labpilot.model.Usuario;
import edu.upc.labpilot.service.ContratoService;
import edu.upc.labpilot.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class ContratoController {

    @Autowired
    private ContratoService contratoService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * ✅ Obtener el contrato vigente (público)
     */
    @GetMapping("/contrato")
    public ResponseEntity<?> obtenerContratoVigente() {
        try {
            Optional<ContratoUso> contratoOpt = contratoService.obtenerContratoVigente();
            if (contratoOpt.isPresent()) {
                return ResponseEntity.ok(contratoOpt.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No hay contrato vigente"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error obteniendo contrato: " + e.getMessage()));
        }
    }

    /**
     * ✅ Verificar si el usuario actual ha aceptado los términos
     */
    @GetMapping("/contrato/verificar-aceptacion")
    public ResponseEntity<?> verificarAceptacion(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no autenticado"));
            }

            String correo = authentication.getName();
            Optional<Usuario> usuarioOpt = usuarioService.findByCorreo(correo);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
            }

            boolean haAceptado = contratoService.verificarAceptacion(usuarioOpt.get().getId());
            
            return ResponseEntity.ok(Map.of(
                "haAceptado", haAceptado,
                "usuario", usuarioOpt.get().getCorreo()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error verificando aceptación: " + e.getMessage()));
        }
    }

    /**
     * ✅ Aceptar términos y condiciones (para estudiantes)
     */
    @PostMapping("/contrato/aceptar")
    public ResponseEntity<?> aceptarContrato(
            @RequestBody Map<String, String> request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no autenticado"));
            }

            String correo = authentication.getName();
            Optional<Usuario> usuarioOpt = usuarioService.findByCorreo(correo);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
            }

            Usuario usuario = usuarioOpt.get();
            String firmaDigital = request.get("firmaDigital");
            
            if (firmaDigital == null || firmaDigital.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Se requiere firma digital"));
            }

            // Obtener información de la solicitud
            String ipAddress = httpRequest.getRemoteAddr();
            String userAgent = httpRequest.getHeader("User-Agent");

            // Registrar aceptación
            boolean exito = contratoService.registrarAceptacion(
                usuario, ipAddress, userAgent, firmaDigital
            );

            if (exito) {
                return ResponseEntity.ok(Map.of(
                    "mensaje", "Términos y condiciones aceptados exitosamente",
                    "fechaAceptacion", java.time.LocalDateTime.now().toString(),
                    "usuario", usuario.getCorreo()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al registrar la aceptación"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error aceptando contrato: " + e.getMessage()));
        }
    }

    /**
     * ✅ Obtener historial de aceptaciones (para admins)
     */
    @GetMapping("/admin/contrato/aceptaciones")
    public ResponseEntity<?> obtenerAceptaciones(Authentication authentication) {
        try {
            // Verificar que sea admin
            if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                             a.getAuthority().equals("ROLE_SUPERADMIN"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No tiene permisos para ver las aceptaciones"));
            }

            // Este método deberías implementarlo en ContratoService
            // List<AceptacionTerminos> aceptaciones = contratoService.obtenerTodasAceptaciones();
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Endpoint para obtener aceptaciones - implementar en servicio"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error obteniendo aceptaciones: " + e.getMessage()));
        }
    }

    /**
     * ✅ MOSTRAR FORMULARIO DE FIRMA DE CONTRATO (GET)
     * Para la URL: /api/auth/firmar-contrato-estudiante/{token}
     */
    @GetMapping("/firmar-contrato-estudiante/{token}")
    public ResponseEntity<String> mostrarFormularioFirmaContrato(@PathVariable String token) {
        try {
            System.out.println("📋 Mostrando formulario de contrato para token: " + token);
            
            Optional<Usuario> usuarioOpt = usuarioService.findByTokenVerificacion(token);
            
            if (usuarioOpt.isEmpty()) {
                return htmlError("Enlace inválido", "No se encontró ningún estudiante con este enlace de verificación.");
            }

            Usuario usuario = usuarioOpt.get();

            // Verificar que sea estudiante
            if (!"estudiante".equals(usuario.getRol())) {
                return htmlError("Acceso denegado", "Solo los estudiantes pueden firmar este contrato.");
            }

            // Verificar que no esté ya activo
            if (usuario.getActivo()) {
                return htmlError("Cuenta ya activa", "Esta cuenta ya ha sido activada anteriormente.");
            }

            return htmlContratoEstudiante(
                usuario.getNombreCompleto(),
                usuario.getDocumento(),
                usuario.getPrograma(),
                token
            );

        } catch (Exception e) {
            System.err.println("❌ Error mostrando formulario: " + e.getMessage());
            return htmlError("Error", "Error mostrando formulario: " + e.getMessage());
        }
    }

    /**
     * ✅ PROCESAR FIRMA DE CONTRATO (POST)
     * Para la URL: /api/auth/firmar-contrato-estudiante/{token}
     */
    @PostMapping("/firmar-contrato-estudiante/{token}")
    public ResponseEntity<String> procesarFirmaContrato(
            @PathVariable String token,
            HttpServletRequest httpRequest) {
        try {
            System.out.println("📋 Procesando firma de contrato para token: " + token);

            Optional<Usuario> usuarioOpt = usuarioService.findByTokenVerificacion(token);
            
            if (usuarioOpt.isEmpty()) {
                return htmlError("Enlace inválido", "No se encontró ningún estudiante con este enlace de verificación.");
            }

            Usuario usuario = usuarioOpt.get();

            // Verificar que sea estudiante
            if (!"estudiante".equals(usuario.getRol())) {
                return htmlError("Acceso denegado", "Solo los estudiantes pueden firmar este contrato.");
            }

            // Verificar que no esté ya activo
            if (usuario.getActivo()) {
                return htmlError("Cuenta ya activa", "Esta cuenta ya ha sido activada anteriormente.");
            }

            // Registrar firma del contrato
            String ipAddress = httpRequest.getRemoteAddr();
            String userAgent = httpRequest.getHeader("User-Agent");
            String firmaFinal = "firma_estudiante_" + System.currentTimeMillis();

            contratoService.registrarAceptacionEstudiante(
                usuario, ipAddress, userAgent, firmaFinal
            );

            System.out.println("✅ Contrato firmado por: " + usuario.getCorreo());

            // Activar cuenta del estudiante
            usuario.setActivo(true);
            usuario.setTokenVerificacion(null); // Limpiar el token después de usar
            usuarioService.save(usuario);

            System.out.println("✅ Cuenta activada para: " + usuario.getCorreo());

            return htmlOk(
                "Contrato Firmado",
                "🎉 ¡Contrato Firmado Exitosamente!",
                "Su cuenta ha sido activada. Ya puede iniciar sesión en el sistema."
            );

        } catch (Exception e) {
            System.err.println("❌ Error firmando contrato: " + e.getMessage());
            return htmlError("Error", "Error firmando contrato: " + e.getMessage());
        }
    }

    /**
     * ✅ VERIFICAR ESTUDIANTE (público)
     */
    @GetMapping("/verificar-estudiante/{token}")
    public ResponseEntity<?> verificarEstudiante(@PathVariable String token) {
        try {
            Optional<Usuario> usuarioOpt = usuarioService.findByTokenVerificacion(token);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Estudiante no encontrado"));
            }

            Usuario usuario = usuarioOpt.get();

            if (!"estudiante".equals(usuario.getRol())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El usuario no es un estudiante"));
            }

            return ResponseEntity.ok(Map.of(
                "estudiante", usuario.getNombreCompleto(),
                "documento", usuario.getDocumento(),
                "programa", usuario.getPrograma(),
                "activo", usuario.getActivo(),
                "correoVerificado", usuario.getCorreoVerificado()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error verificando estudiante: " + e.getMessage()));
        }
    }

    // ==========================================================
    // MÉTODOS AUXILIARES HTML
    // ==========================================================

    private ResponseEntity<String> htmlError(String titulo, String mensaje) {
        return ResponseEntity.badRequest().body(
            "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Error - LabPilot</title>"
            + "<style>body{font-family:Arial,sans-serif;background:#f8d7da;padding:40px;}"
            + ".container{background:#fff;padding:30px;border-radius:10px;max-width:600px;margin:auto;"
            + "box-shadow:0 2px 8px rgba(0,0,0,0.1);}h1{text-align:center;color:#721c24;}"
            + "p{text-align:center;color:#721c24;}</style></head><body>"
            + "<div class='container'><h1>" + titulo + "</h1><p>" + mensaje + "</p></div></body></html>"
        );
    }

    private ResponseEntity<String> htmlOk(String titulo, String encabezado, String mensaje) {
        return ResponseEntity.ok(
            "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" + titulo + "</title>"
            + "<style>body{font-family:Arial,sans-serif;background:#f5f5f5;margin:0;padding:40px;}"
            + ".container{background:#fff;padding:30px;border-radius:10px;max-width:600px;margin:auto;"
            + "box-shadow:0 2px 8px rgba(0,0,0,0.1);}h1{text-align:center;color:#333;}p{text-align:center;color:#555;}"
            + ".ok{color:#28a745;font-size:60px;text-align:center;}</style></head><body>"
            + "<div class='container'><div class='ok'>✅</div><h1>" + encabezado + "</h1>"
            + "<p>" + mensaje + "</p></div></body></html>"
        );
    }

    private ResponseEntity<String> htmlContratoEstudiante(String nombre, String documento, 
                                                          String programa, String token) {
        return ResponseEntity.ok(
            "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Contrato Estudiante - LabPilot</title>"
            + "<style>body{font-family:Arial,sans-serif;background:#f5f5f5;margin:0;padding:20px;}"
            + ".container{background:#fff;padding:30px;border-radius:10px;max-width:800px;margin:auto;"
            + "box-shadow:0 2px 8px rgba(0,0,0,0.1);}h1{text-align:center;color:#333;}h2{color:#555;}"
            + ".contrato{border:1px solid #ccc;padding:20px;margin:20px 0;max-height:400px;overflow-y:auto;}"
            + ".firma-form{margin-top:20px;padding:20px;background:#f9f9f9;border-radius:5px;}"
            + ".info-usuario{background:#e8f4fd;padding:15px;border-radius:5px;margin-bottom:20px;}"
            + "button{background:#28a745;color:white;padding:12px 24px;border:none;border-radius:5px;cursor:pointer;}"
            + "button:hover{background:#218838;}</style></head><body>"
            + "<div class='container'>"
            + "<h1>🎓 Contrato de Responsabilidad - Estudiante</h1>"
            + "<div class='info-usuario'>"
            + "<p><strong>Estudiante:</strong> " + nombre + "</p>"
            + "<p><strong>Documento:</strong> " + documento + "</p>"
            + "<p><strong>Programa:</strong> " + programa + "</p>"
            + "</div>"
            + "<div class='contrato'>"
            + "<h2>DECLARACIÓN JURADA DE USO RESPONSABLE</h2>"
            + "<p>Yo, <strong>" + nombre + "</strong>, identificado con documento <strong>" + documento + "</strong>, "
            + "estudiante de <strong>" + programa + "</strong> de la Universidad Piloto de Colombia, declaro bajo juramento:</p>"
            + "<h3>1. USO ACADÉMICO EXCLUSIVO</h3>"
            + "<p>Me comprometo a utilizar el Sistema de Laboratorios LabPilot ÚNICAMENTE para fines académicos.</p>"
            + "<h3>2. RESPONSABILIDAD SOBRE EQUIPOS</h3>"
            + "<ul>"
            + "<li>Utilizaré los equipos con cuidado y responsabilidad</li>"
            + "<li>Reportaré inmediatamente cualquier daño</li>"
            + "<li>Asumiré responsabilidad económica por daños causados</li>"
            + "</ul>"
            + "<h3>3. CONSECUENCIAS POR INCUMPLIMIENTO</h3>"
            + "<p>Reconozco que el incumplimiento puede acarrear suspensión, sanciones disciplinarias y responsabilidad económica.</p>"
            + "</div>"
            + "<div class='firma-form'>"
            + "<form method='POST' action='/api/auth/firmar-contrato-estudiante/" + token + "'>"
            + "<button type='submit'>✅ Firmar Contrato y Activar Cuenta</button>"
            + "</form>"
            + "</div>"
            + "</div></body></html>"
        );
    }
}
