package edu.upc.labpilot.controller.auth;

import edu.upc.labpilot.config.JwtUtil;
import edu.upc.labpilot.dto.LoginRequest;
import edu.upc.labpilot.dto.LoginResponse;
import edu.upc.labpilot.model.Usuario;
import edu.upc.labpilot.repository.UsuarioRepository;
import edu.upc.labpilot.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            System.out.println("═══════════════════════════════════════");
            System.out.println("🔑 INICIO LOGIN");
            System.out.println("═══════════════════════════════════════");
            System.out.println("📧 Correo: " + request.getCorreo());
            System.out.println("🔐 Password (length): " + request.getPassword().length());
            
            // PASO 1: Verificar que el usuario existe
            Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo());
            if (usuario == null) {
                System.out.println("❌ Usuario no encontrado en BD");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
            }
            
            System.out.println("✅ Usuario encontrado en BD:");
            System.out.println("   - ID: " + usuario.getId());
            System.out.println("   - Nombre: " + usuario.getNombre() + " " + usuario.getApellido());
            System.out.println("   - Rol: " + usuario.getRol());
            System.out.println("   - Activo: " + usuario.isActivo());
            System.out.println("   - Correo verificado: " + usuario.isCorreoVerificado());

            // PASO 2: Verificar estado del usuario
            if (!usuario.isActivo()) {
                System.out.println("❌ Usuario inactivo");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Usuario inactivo. Contacta al administrador.");
            }

            if (!usuario.isCorreoVerificado() && 
                !usuario.getRol().equalsIgnoreCase("administrador") && 
                !usuario.getRol().equalsIgnoreCase("superAdmin")) {
                System.out.println("❌ Correo no verificado");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Debes verificar tu correo antes de iniciar sesión.");
            }

            // PASO 3: Autenticar con Spring Security
            System.out.println("🔐 Intentando autenticación con AuthenticationManager...");
            
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword())
            );
            
            System.out.println("✅ Autenticación exitosa!");

            // PASO 4: Cargar UserDetails y generar JWT
            System.out.println("📝 Cargando UserDetails...");
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getCorreo());
            
            System.out.println("🎫 Generando JWT...");
            String jwt = jwtUtil.generateToken(userDetails);
            System.out.println("✅ JWT generado (primeros 50 chars): " + jwt.substring(0, Math.min(50, jwt.length())));

            // PASO 5: Construir respuesta completa
            LoginResponse response = new LoginResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),
                usuario.getRol(),
                jwt,
                usuario.getDocumento(),
                usuario.getPrograma(),
                usuario.getDepartamento(),
                usuario.getEspecialidad()
            );

            System.out.println("═══════════════════════════════════════");
            System.out.println("✅ LOGIN EXITOSO");
            System.out.println("   - Token JWT: " + jwt.substring(0, Math.min(30, jwt.length())) + "...");
            System.out.println("   - Usuario: " + usuario.getNombreCompleto());
            System.out.println("   - Rol: " + usuario.getRol());
            System.out.println("═══════════════════════════════════════");
            
            return ResponseEntity.ok(response);

        } catch (UsernameNotFoundException e) {
            System.out.println("❌ Usuario no encontrado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
            
        } catch (BadCredentialsException e) {
            System.out.println("❌ Credenciales incorrectas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
            
        } catch (Exception e) {
            System.out.println("❌ ERROR INESPERADO EN LOGIN");
            System.out.println("Tipo: " + e.getClass().getName());
            System.out.println("Mensaje: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("Token inválido");
        }

        String correo = auth.getName();
        Usuario usuario = usuarioRepository.findByCorreo(correo);

        Map<String, Object> data = new HashMap<>();
        data.put("id", usuario.getId());
        data.put("nombre", usuario.getNombre());
        data.put("apellido", usuario.getApellido());
        data.put("correo", usuario.getCorreo());
        data.put("rol", usuario.getRol());

        return ResponseEntity.ok(data);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada"));
    }
}