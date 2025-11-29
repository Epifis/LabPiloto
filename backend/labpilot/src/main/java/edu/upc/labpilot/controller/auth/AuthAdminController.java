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
@RequestMapping("/api/auth/admin")
@CrossOrigin(origins = "*")
public class AuthAdminController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MfaService mfaService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Login admin / superadmin:
     *  - Authentica password
     *  - Genera y envía código MFA por email (no devuelve JWT todavía)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String correo = body.get("correo");
        String password = body.get("password");

        try {
            // Autenticación por password
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(correo, password));

            // Buscar usuario
            Usuario usuario = usuarioRepository.findByCorreo(correo);
            if (usuario == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
            }

            // Solo admin o superAdmin pueden entrar aquí
            if (!"administrador".equalsIgnoreCase(usuario.getRol()) && !"superadmin".equalsIgnoreCase(usuario.getRol())) {
                return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado - No es administrador"));
            }

            // Generar código MFA para LOGIN (email)
            String codigo = mfaService.iniciarMfa(usuario, "email");

            // Respuesta indicando que se envió el código
            return ResponseEntity.ok(Map.of(
                    "message", "Código MFA enviado al correo",
                    "idUsuario", usuario.getId()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }
    }

    /**
     * Verificar código MFA (login). Si válido -> generar JWT y devolver datos.
     * Request: { "idUsuario": 123, "codigo": "123456" }
     */
    @PostMapping("/verify-mfa")
    public ResponseEntity<?> verifyMfa(@RequestBody Map<String, Object> body) {
        Integer idUsuario = (body.get("idUsuario") instanceof Integer) ? (Integer) body.get("idUsuario") : Integer.parseInt(body.get("idUsuario").toString());
        String codigo = body.get("codigo").toString();

        boolean ok = mfaService.verificarCodigoMfa(idUsuario, codigo);
        if (!ok) {
            return ResponseEntity.status(400).body(Map.of("error", "Código inválido o expirado"));
        }

        Usuario usuario = usuarioService.getById(idUsuario).orElse(null);
        if (usuario == null) return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));

        // Generar JWT
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
     * Reenviar código MFA login
     * Request: { "idUsuario": 123 }
     */
    @PostMapping("/reenviar-codigo")
    public ResponseEntity<?> reenviarCodigo(@RequestBody Map<String, Object> body) {
        Integer idUsuario = (body.get("idUsuario") instanceof Integer) ? (Integer) body.get("idUsuario") : Integer.parseInt(body.get("idUsuario").toString());
        boolean reenvio = mfaService.reenviarCodigoMfa(idUsuario);
        if (!reenvio) return ResponseEntity.status(400).body(Map.of("error", "No se pudo reenviar código"));
        return ResponseEntity.ok(Map.of("message", "Código reenviado"));
    }
}
