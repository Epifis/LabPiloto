package edu.upc.labpilot.controller.auth;

import edu.upc.labpilot.config.JwtUtil;
import edu.upc.labpilot.model.Usuario;
import edu.upc.labpilot.repository.UsuarioRepository;
import edu.upc.labpilot.service.CustomUserDetailsService;
import edu.upc.labpilot.service.EmailService;
import edu.upc.labpilot.service.MfaService;
import edu.upc.labpilot.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth/profesor")
@CrossOrigin(origins = "*")
public class AuthProfesorController {

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
     * Registro profesor (similar a lo que estabas usando)
     * Request body: { "nombre", "apellido", "correo", "password", "documento", "departamento", "telefono", ... }
     */
    @PostMapping("/registro")
    public ResponseEntity<?> registroProfesor(@RequestBody Map<String, String> body) {
        try {
            if (usuarioService.existsByCorreo(body.get("correo"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ya existe un usuario con ese correo"));
            }

            Usuario usuario = new Usuario();
            usuario.setNombre(body.get("nombre"));
            usuario.setApellido(body.get("apellido"));
            usuario.setCorreo(body.get("correo"));
            usuario.setDocumento(body.get("documento"));
            usuario.setPrograma(body.get("departamento"));
            usuario.setTelefono(body.get("telefono"));
            usuario.setRol("profesor");
            usuario.setPassword(body.get("password"));
            usuario.setActivo(false);

            Usuario saved = usuarioService.save(usuario);

            // generar código verificación
            String codigo = mfaService.generarCodigoVerificacion(saved);
            emailService.enviarCodigoVerificacionCorreo(saved.getCorreo(), saved.getNombre(), codigo);

            return ResponseEntity.ok(Map.of("message", "Profesor registrado. Verifica tu correo.", "id", saved.getId()));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Login profesor -> password auth then send login MFA code
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String correo = body.get("correo");
        String password = body.get("password");
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(correo, password));
            Usuario usuario = usuarioRepository.findByCorreo(correo);
            if (usuario == null) return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
            if (!"profesor".equalsIgnoreCase(usuario.getRol())) {
                return ResponseEntity.status(403).body(Map.of("error", "No es una cuenta de profesor"));
            }

            mfaService.iniciarMfa(usuario, "email");
            return ResponseEntity.ok(Map.of("message", "Código MFA enviado", "idUsuario", usuario.getId()));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }
    }

    /**
     * Verificar MFA login -> devuelve JWT
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
     * Reenviar código (verificación registro o login)
     * Request: { "idUsuario":123, "tipo":"verificacion_correo" | "login_mfa" }
     */
    @PostMapping("/reenviar-codigo")
    public ResponseEntity<?> reenviar(@RequestBody Map<String, Object> body) {
        Integer idUsuario = (body.get("idUsuario") instanceof Integer) ? (Integer) body.get("idUsuario") : Integer.parseInt(body.get("idUsuario").toString());
        String tipo = body.get("tipo") != null ? body.get("tipo").toString() : "verificacion_correo";

        Usuario usuario = usuarioService.getById(idUsuario).orElse(null);
        if (usuario == null) return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));

        if ("login_mfa".equals(tipo)) {
            boolean ok = mfaService.reenviarCodigoMfa(idUsuario);
            if (!ok) return ResponseEntity.status(400).body(Map.of("error", "No se pudo reenviar"));
            return ResponseEntity.ok(Map.of("message", "Código de login reenviado"));
        } else {
            String codigo = mfaService.generarCodigoVerificacion(usuario);
            emailService.reenviarCodigoVerificacion(usuario.getCorreo(), usuario.getNombre(), codigo);
            return ResponseEntity.ok(Map.of("message", "Código de verificación reenviado"));
        }
    }
}
