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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
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
            // Autenticar usando Spring Security
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword())
            );

            // Cargar usuario y generar token
            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getCorreo());
            final String jwt = jwtUtil.generateToken(userDetails);

            // Obtener información del usuario
            Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo());
            
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
}