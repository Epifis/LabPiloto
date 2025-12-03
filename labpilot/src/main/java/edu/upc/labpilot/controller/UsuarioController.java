package edu.upc.labpilot.controller;

import edu.upc.labpilot.dto.RegistroEstudianteRequest;
import edu.upc.labpilot.dto.VerificarCodigoRequest;
import edu.upc.labpilot.model.Usuario;
import edu.upc.labpilot.service.UsuarioService;
import edu.upc.labpilot.service.MfaService;
import edu.upc.labpilot.service.EmailService;
import edu.upc.labpilot.service.ContratoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 👤 CONTROLADOR DE USUARIOS
 * - Registro de estudiantes (PÚBLICO)
 * - Gestión de usuarios (ADMIN)
 */
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {
    
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private MfaService mfaService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ContratoService contratoService;

    // ==========================================================
    // 📝 REGISTRO PÚBLICO DE ESTUDIANTES
    // ==========================================================

    /**
     * ✅ REGISTRO DE ESTUDIANTE CON VERIFICACIÓN
     * Flujo completo: Registro → Código → Contrato → Activación
     */
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarEstudiante(
            @RequestBody RegistroEstudianteRequest request,
            HttpServletRequest httpRequest) {
        try {
            System.out.println("📝 Iniciando registro de estudiante: " + request.getCorreo());

            // ✅ VALIDACIONES BÁSICAS
            if (request.getCorreo() == null || request.getCorreo().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El correo es obligatorio"));
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "La contraseña es obligatoria"));
            }

            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El nombre es obligatorio"));
            }

            if (request.getApellido() == null || request.getApellido().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El apellido es obligatorio"));
            }

            if (!request.isAceptaTerminos()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Debe aceptar los términos y condiciones"));
            }

            // ✅ Verificar que el correo no exista
            if (usuarioService.existsByCorreo(request.getCorreo())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Ya existe un usuario con ese correo"));
            }

            // ✅ CREAR USUARIO
            Usuario usuario = new Usuario();
            usuario.setNombre(request.getNombre());
            usuario.setApellido(request.getApellido());
            usuario.setCorreo(request.getCorreo());
            usuario.setDocumento(request.getDocumento());
            usuario.setPrograma(request.getPrograma());
            usuario.setTelefono(request.getTelefono());
            usuario.setRol("estudiante");
            usuario.setActivo(false);
            usuario.setCorreoVerificado(false);
            usuario.setMfaHabilitado(true);

            System.out.println("✅ Guardando usuario en base de datos...");

            // ✅ GUARDAR CON CONTRASEÑA ENCRIPTADA
            Usuario usuarioGuardado = usuarioService.registrarEstudiante(usuario, request.getPassword());

            System.out.println("✅ Usuario guardado con ID: " + usuarioGuardado.getId());

            // ✅ ENVIAR CÓDIGO DE VERIFICACIÓN (PRIMERA ETAPA)
            String codigoVerificacion = mfaService.generarCodigoVerificacion(usuarioGuardado);
            emailService.enviarCodigoVerificacionCorreo(
                usuarioGuardado.getCorreo(),
                usuarioGuardado.getNombre(),
                codigoVerificacion
            );

            System.out.println("✅ Código de verificación enviado");

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Se ha enviado un código de verificación a su correo",
                "idUsuario", usuarioGuardado.getId(),
                "correo", usuarioGuardado.getCorreo(),
                "requiereVerificacion", true,
                "siguientePaso", "verificar-codigo"
            ));

        } catch (Exception e) {
            System.err.println("❌ Error en registro estudiante: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error en el registro: " + e.getMessage()));
        }
    }

    /**
     * ✅ VERIFICAR CÓDIGO DE CORREO (SEGUNDA ETAPA)
     */
    @PostMapping("/verificar-codigo")
    public ResponseEntity<?> verificarCodigoCorreo(@RequestBody VerificarCodigoRequest request) {
        try {
            System.out.println("🔍 Verificando código para: " + request.getCorreo());

            Optional<Usuario> usuarioOpt = usuarioService.findByCorreo(request.getCorreo());

            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
            }

            Usuario usuario = usuarioOpt.get();

            // ✅ Verificar código
            boolean codigoValido = mfaService.verificarCodigoVerificacion(
                usuario.getId(), request.getCodigo());

            if (!codigoValido) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Código de verificación inválido o expirado"));
            }

            // ✅ Marcar correo como verificado
            usuario.setCorreoVerificado(true);
            usuarioService.save(usuario);

            System.out.println("✅ Correo verificado para: " + usuario.getCorreo());

            // ✅ Enviar contrato para firma (TERCERA ETAPA)
            emailService.enviarContratoEstudiante(
                usuario.getCorreo(),
                usuario.getNombre(),
                usuario.getTokenVerificacion()
            );

            return ResponseEntity.ok(Map.of(
                "mensaje", "Correo verificado exitosamente. Se ha enviado el contrato para firma.",
                "correoVerificado", true,
                "siguientePaso", "firmar-contrato"
            ));

        } catch (Exception e) {
            System.err.println("❌ Error verificando código: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error verificando código: " + e.getMessage()));
        }
    }

    /**
     * 📋 FIRMAR CONTRATO ESTUDIANTE (CUARTA ETAPA - POST)
     */
    @PostMapping("/firmar-contrato/{token}")
    public ResponseEntity<String> firmarContratoEstudiante(
            @PathVariable String token,
            HttpServletRequest httpRequest) {
        try {
            System.out.println("📋 Procesando firma de contrato para token: " + token);

            Optional<Usuario> usuarioOpt = usuarioService.findByTokenVerificacion(token);

            if (usuarioOpt.isEmpty()) {
                return htmlError("Token inválido", "No se encontró ningún estudiante con este token.");
            }

            Usuario usuario = usuarioOpt.get();

            // ✅ Verificar que sea estudiante y tenga correo verificado
            if (!"estudiante".equals(usuario.getRol()) || !usuario.getCorreoVerificado()) {
                return htmlError("Usuario no válido", "Debe verificar su correo primero.");
            }

            // ✅ REGISTRAR FIRMA DEL CONTRATO
            String ipAddress = httpRequest.getRemoteAddr();
            String userAgent = httpRequest.getHeader("User-Agent");
            String firmaFinal = "firma_estudiante_" + System.currentTimeMillis();

            contratoService.registrarAceptacionEstudiante(
                usuario, ipAddress, userAgent, firmaFinal
            );

            System.out.println("✅ Contrato firmado por: " + usuario.getCorreo());

            // ✅ ACTIVAR CUENTA DEL ESTUDIANTE (ETAPA FINAL)
            usuario.setActivo(true);
            usuario.setTokenVerificacion(null);
            usuarioService.save(usuario);

            System.out.println("✅ Cuenta activada para: " + usuario.getCorreo());

            return htmlOk(
                "Contrato Firmado",
                "🎉 ¡Cuenta Activada Exitosamente!",
                "Ya puede iniciar sesión en el sistema con sus credenciales."
            );

        } catch (Exception e) {
            System.err.println("❌ Error firmando contrato: " + e.getMessage());
            e.printStackTrace();
            return htmlError("Error", "Error firmando contrato: " + e.getMessage());
        }
    }

    /**
     * 📋 MOSTRAR FORMULARIO DE CONTRATO (GET)
     */
    @GetMapping("/firmar-contrato/{token}")
    public ResponseEntity<String> mostrarFormularioContrato(@PathVariable String token) {
        try {
            Optional<Usuario> usuarioOpt = usuarioService.findByTokenVerificacion(token);

            if (usuarioOpt.isEmpty()) {
                return htmlError("Token inválido", "No se encontró ningún estudiante con este token.");
            }

            Usuario usuario = usuarioOpt.get();

            if (!"estudiante".equals(usuario.getRol()) || usuario.getActivo()) {
                return htmlError("Usuario no válido", "Este usuario no puede firmar el contrato.");
            }

            return htmlContratoEstudiante(
                usuario.getNombreCompleto(),
                usuario.getDocumento(),
                usuario.getPrograma(),
                token
            );

        } catch (Exception e) {
            return htmlError("Error", "Error mostrando formulario: " + e.getMessage());
        }
    }

    /**
     * 🔄 REENVIAR CÓDIGO DE VERIFICACIÓN
     */
    @PostMapping("/reenviar-codigo")
    public ResponseEntity<?> reenviarCodigo(@RequestBody Map<String, String> request) {
        try {
            String correo = request.get("correo");

            Optional<Usuario> usuarioOpt = usuarioService.findByCorreo(correo);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
            }

            Usuario usuario = usuarioOpt.get();

            if (usuario.getActivo()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "La cuenta ya está activa"));
            }

            if (!usuario.getCorreoVerificado()) {
                // Reenviar código de verificación
                String codigoVerificacion = mfaService.generarCodigoVerificacion(usuario);
                emailService.reenviarCodigoVerificacion(
                    usuario.getCorreo(),
                    usuario.getNombre(),
                    codigoVerificacion
                );
                return ResponseEntity.ok(Map.of(
                    "mensaje", "Se ha reenviado el código de verificación"
                ));
            } else {
                // Reenviar contrato
                emailService.enviarContratoEstudiante(
                    usuario.getCorreo(),
                    usuario.getNombre(),
                    usuario.getTokenVerificacion()
                );
                return ResponseEntity.ok(Map.of(
                    "mensaje", "Se ha reenviado el contrato para firma"
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error reenviando código: " + e.getMessage()));
        }
    }

    // ==========================================================
    // 🔐 ENDPOINTS DE GESTIÓN (SOLO ADMINS)
    // ==========================================================

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<Usuario>> getAll() {
        return ResponseEntity.ok(usuarioService.getAll());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Usuario> getById(@PathVariable Integer id) {
        return usuarioService.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Usuario> update(@PathVariable Integer id, @RequestBody Usuario usuario) {
        return usuarioService.getById(id)
            .map(existing -> {
                usuario.setId(id);
                return ResponseEntity.ok(usuarioService.save(usuario));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Usuario> cambiarEstado(
            @PathVariable Integer id,
            @RequestBody Map<String, Boolean> body) {
        boolean nuevoEstado = body.get("activo");
        Usuario usuario = usuarioService.cambiarEstado(id, nuevoEstado);
        return ResponseEntity.ok(usuario);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (usuarioService.getById(id).isPresent()) {
            usuarioService.delete(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
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
            + "<form method='POST' action='/api/usuarios/firmar-contrato/" + token + "'>"
            + "<button type='submit'>✅ Firmar Contrato y Activar Cuenta</button>"
            + "</form>"
            + "</div>"
            + "</div></body></html>"
        );
    }
}