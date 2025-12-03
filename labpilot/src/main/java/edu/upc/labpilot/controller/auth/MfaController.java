package edu.upc.labpilot.controller.auth;

import edu.upc.labpilot.config.JwtUtil;
import edu.upc.labpilot.model.Usuario;
import edu.upc.labpilot.service.CustomUserDetailsService;
import edu.upc.labpilot.service.EmailService;
import edu.upc.labpilot.service.MfaService;
import edu.upc.labpilot.service.UsuarioService;
import edu.upc.labpilot.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/mfa")
@CrossOrigin(origins = "*")
public class MfaController {

    @Autowired
    private MfaService mfaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Iniciar MFA para login por correo (usa el servicio para crear y enviar)
     * Request: { "correo": "..." }  -- autenticación por password debe ocurrir antes en flows que lo requieran
     */
    @PostMapping("/start")
    public ResponseEntity<?> startMfa(@RequestBody Map<String, String> body) {
        String correo = body.get("correo");
        Usuario usuario = usuarioRepository.findByCorreo(correo);
        if (usuario == null) return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));

        String codigo = mfaService.iniciarMfa(usuario, "email");
        return ResponseEntity.ok(Map.of("message", "Código MFA enviado", "idUsuario", usuario.getId()));
    }

    /**
     * Verificar código MFA de login -> retorna JWT
     * Request: { "idUsuario": 123, "codigo":"123456" }
     */
    @PostMapping("/verify-login")
    public ResponseEntity<?> verifyLogin(@RequestBody Map<String, Object> body) {
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
        resp.put("correo", usuario.getCorreo());
        resp.put("rol", usuario.getRol());
        return ResponseEntity.ok(resp);
    }

    /**
     * Reenviar código de login MFA
     * Request: { "idUsuario": 123 }
     */
    @PostMapping("/reenviar")
    public ResponseEntity<?> reenviar(@RequestBody Map<String, Object> body) {
        Integer idUsuario = (body.get("idUsuario") instanceof Integer) ? (Integer) body.get("idUsuario") : Integer.parseInt(body.get("idUsuario").toString());
        boolean ok = mfaService.reenviarCodigoMfa(idUsuario);
        if (!ok) return ResponseEntity.status(400).body(Map.of("error", "No se pudo reenviar"));
        return ResponseEntity.ok(Map.of("message", "Código reenviado"));
    }
}
