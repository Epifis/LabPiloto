package edu.upc.labpilot.service;

import edu.upc.labpilot.model.Usuario;
import edu.upc.labpilot.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        System.out.println("🔍 CustomUserDetailsService - Buscando usuario: " + correo);
        
        Usuario usuario = usuarioRepository.findByCorreo(correo);

        if (usuario == null) {
            System.out.println("❌ Usuario no encontrado: " + correo);
            throw new UsernameNotFoundException("Usuario no encontrado: " + correo);
        }

        String rolBD = usuario.getRol(); // superAdmin, administrador, estudiante, profesor
        System.out.println("✅ Usuario encontrado:");
        System.out.println("   - Nombre: " + usuario.getNombre() + " " + usuario.getApellido());
        System.out.println("   - Rol en BD: " + rolBD);
        System.out.println("   - Activo: " + usuario.isActivo());
        System.out.println("   - Correo verificado: " + usuario.isCorreoVerificado());

        // Convertir rol de BD a ROLE_*** para Spring Security
        String authority = convertirRolAAuthority(rolBD);
        System.out.println("   - Authority asignada: " + authority);

        return User.builder()
                .username(usuario.getCorreo())
                .password(usuario.getPassword()) // Ya hasheado en BD
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(authority)))
                .accountExpired(false)
                .accountLocked(!usuario.isActivo())
                .credentialsExpired(false)
                .disabled(!usuario.isActivo())
                .build();
    }

    /**
     * Convierte el rol de la BD al formato ROLE_* de Spring Security
     */
    private String convertirRolAAuthority(String rolBD) {
        if (rolBD == null) {
            return "ROLE_USER";
        }

        switch (rolBD.toLowerCase()) {
            case "superadmin":
                return "ROLE_SUPERADMIN";
            
            case "administrador":
            case "admin":
                return "ROLE_ADMIN";
            
            case "profesor":
                return "ROLE_PROFESOR";
            
            case "estudiante":
                return "ROLE_ESTUDIANTE";
            
            default:
                System.out.println("⚠️ Rol no reconocido: " + rolBD + ", usando ROLE_USER");
                return "ROLE_USER";
        }
    }
}