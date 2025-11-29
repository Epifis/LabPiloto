package edu.upc.labpilot.controller.auth;

import edu.upc.labpilot.config.JwtUtil;
import edu.upc.labpilot.model.Usuario;
import edu.upc.labpilot.service.EmailService;
import edu.upc.labpilot.service.MfaService;
import edu.upc.labpilot.service.UsuarioService;
import edu.upc.labpilot.service.CustomUserDetailsService;
import edu.upc.labpilot.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth/estudiante")
@CrossOrigin(origins = "*")
public class AuthEstudianteController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MfaService mfaService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Registro de estudiante:
     * - crea usuario con rol 'estudiante' (inactivo)
     * - genera código de verificación (15min) y lo envía por email
     * Request body: { "nombre": "...", "apellido":"...", "correo":"...", "password":"...", "documento":"...", "programa":"..." }
     */
    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody Map<String, String> body) {
        try {
            Usuario u = new Usuario();
            u.setNombre(body.get("nombre"));
            u.setApellido(body.get("apellido"));
            u.setCorreo(body.get("correo"));
            u.setDocumento(body.get("documento"));
            u.setPrograma(body.get("programa"));
            // Password will be encoded in usuarioService.save if needed
            u.setPassword(body.get("password"));

            Usuario creado = usuarioService.registrarEstudiante(u, body.get("password"));

            // Generar código de verificación y enviarlo
            String codigo = mfaService.generarCodigoVerificacion(creado);
            emailService.enviarCodigoVerificacionCorreo(creado.getCorreo(), creado.getNombre(), codigo);

            return ResponseEntity.ok(Map.of(
                    "message", "Estudiante registrado. Código de verificación enviado por correo.",
                    "idUsuario", creado.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Validar código de verificación (registro). Si válido -> marcar correo verificado y enviar enlace contrato.
     * Request: { "idUsuario": 123, "codigo": "123456" }
     */
    @PostMapping("/validar-codigo-verificacion")
    public ResponseEntity<?> validarCodigoVerificacion(@RequestBody Map<String, Object> body) {
        Integer idUsuario = (body.get("idUsuario") instanceof Integer) ? (Integer) body.get("idUsuario") : Integer.parseInt(body.get("idUsuario").toString());
        String codigo = body.get("codigo").toString();

        boolean valido = mfaService.verificarCodigoVerificacion(idUsuario, codigo);
        if (!valido) return ResponseEntity.status(400).body(Map.of("error", "Código inválido o expirado"));

        Optional<Usuario> opt = usuarioService.getById(idUsuario);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));

        Usuario usuario = opt.get();
        // Marcar correo verificado y guardar
        usuario.marcarCorreoVerificado();
        usuarioService.save(usuario);

        // Enviar link/contrato para firmar (si en tu flujo lo quieres)
        emailService.enviarContratoEstudiante(usuario.getCorreo(), usuario.getNombre(), usuario.getTokenVerificacion());

        return ResponseEntity.ok(Map.of("message", "Correo verificado. Se ha enviado el contrato para firmar."));
    }

    /**
     * Login (password) -> envía código MFA (login)
     * Request: { "correo":"..", "password":".." }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String correo = body.get("correo");
        String password = body.get("password");
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(correo, password));

            Usuario usuario = usuarioRepository.findByCorreo(correo);
            if (usuario == null) return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
            if (!"estudiante".equalsIgnoreCase(usuario.getRol())) {
                return ResponseEntity.status(403).body(Map.of("error", "No es una cuenta de estudiante"));
            }

            // Generar código login MFA
            mfaService.iniciarMfa(usuario, "email");
            return ResponseEntity.ok(Map.of("message", "Código MFA enviado", "idUsuario", usuario.getId()));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }
    }

    /**
     * Verificar código MFA de login -> devuelve JWT
     * Request: { "idUsuario":123, "codigo":"123456" }
     */
    @PostMapping("/verify-mfa")
    public ResponseEntity<?> verifyMfa(@RequestBody Map<String, Object> body) {
        Integer idUsuario = (body.get("idUsuario") instanceof Integer) ? (Integer) body.get("idUsuario") : Integer.parseInt(body.get("idUsuario").toString());
        String codigo = body.get("codigo").toString();

        boolean ok = mfaService.verificarCodigoMfa(idUsuario, codigo);
        if (!ok) return ResponseEntity.status(400).body(Map.of("error", "Código inválido o expirado"));

        Usuario usuario = usuarioService.getById(idUsuario).orElse(null);
        if (usuario == null) return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));

        UserDetails ud = userDetailsService.loadUserByUsername(usuario.getCorreo());
        String token = jwtUtil.generateToken(ud);

        Map<String, Object> resp = new HashMap<>();
        resp.put("jwt", token);
        resp.put("id", usuario.getId());
        resp.put("nombre", usuario.getNombre());
        resp.put("apellido", usuario.getApellido());
        resp.put("correo", usuario.getCorreo());
        resp.put("rol", usuario.getRol());
        return ResponseEntity.ok(resp);
    }

    /**
     * Reenviar código de verificación (registro) o login.
     * For registration_verification: we'll generate new verification code and email it.
     * Request: { "idUsuario": 123, "tipo": "verificacion_correo" }  // or "login_mfa"
     */
    @PostMapping("/reenviar-codigo")
    public ResponseEntity<?> reenviarCodigo(@RequestBody Map<String, Object> body) {
        Integer idUsuario = (body.get("idUsuario") instanceof Integer) ? (Integer) body.get("idUsuario") : Integer.parseInt(body.get("idUsuario").toString());
        String tipo = body.get("tipo") != null ? body.get("tipo").toString() : "verificacion_correo";

        Usuario usuario = usuarioService.getById(idUsuario).orElse(null);
        if (usuario == null) return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));

        if ("login_mfa".equals(tipo)) {
            boolean ok = mfaService.reenviarCodigoMfa(idUsuario);
            if (!ok) return ResponseEntity.status(400).body(Map.of("error", "No se pudo reenviar el código de login"));
            return ResponseEntity.ok(Map.of("message", "Código de login reenviado"));
        } else {
            // generar nuevo código de verificación y enviar
            String codigo = mfaService.generarCodigoVerificacion(usuario);
            emailService.reenviarCodigoVerificacion(usuario.getCorreo(), usuario.getNombre(), codigo);
            return ResponseEntity.ok(Map.of("message", "Código de verificación reenviado"));
        }
    }
}
