package edu.upc.labpilot.controller;

import edu.upc.labpilot.config.JwtUtil;
import edu.upc.labpilot.dto.LoginRequest;
import edu.upc.labpilot.dto.LoginResponse;
import edu.upc.labpilot.model.Usuario;
import edu.upc.labpilot.repository.UsuarioRepository;
import edu.upc.labpilot.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Login - Genera JWT
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Autenticar usando Spring Security
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword())
            );

            // Cargar usuario y generar token
            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getCorreo());
            final String jwt = jwtUtil.generateToken(userDetails);

            // Obtener información del usuario
            Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo());
            
            // Validar que sea admin o superAdmin
            if (!usuario.getRol().equalsIgnoreCase("superAdmin") && 
                !usuario.getRol().equalsIgnoreCase("administrador")) {
                return ResponseEntity.status(403).body("Acceso denegado - Solo administradores");
            }
            
            LoginResponse response = new LoginResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),
                usuario.getRol(),
                jwt
            );

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error en login: " + e.getMessage());
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }
    }

    /**
     * Verify - Valida el token JWT y retorna datos del usuario
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(401).body("Token inválido");
            }

            String correo = auth.getName();
            Usuario usuario = usuarioRepository.findByCorreo(correo);

            if (usuario == null) {
                return ResponseEntity.status(404).body("Usuario no encontrado");
            }

            // Verificar que sea admin o superAdmin
            if (!usuario.getRol().equalsIgnoreCase("superAdmin") && 
                !usuario.getRol().equalsIgnoreCase("administrador")) {
                return ResponseEntity.status(403).body("Acceso denegado");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", usuario.getId());
            response.put("nombre", usuario.getNombre());
            response.put("apellido", usuario.getApellido());
            response.put("correo", usuario.getCorreo());
            response.put("rol", usuario.getRol());

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error verificando token: " + e.getMessage());
            return ResponseEntity.status(401).body("Token inválido");
        }
    }

    /**
     * Logout - Invalida el token (en producción usar blacklist en Redis/DB)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            // Limpiar el contexto de seguridad
            SecurityContextHolder.clearContext();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Sesión cerrada exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error en logout: " + e.getMessage());
            return ResponseEntity.status(500).body("Error al cerrar sesión");
        }
    }
}
