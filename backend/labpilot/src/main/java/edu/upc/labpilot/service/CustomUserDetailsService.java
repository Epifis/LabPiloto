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
    Usuario usuario = usuarioRepository.findByCorreo(correo);

    if (usuario == null) {
        throw new UsernameNotFoundException("Usuario no encontrado: " + correo);
    }

    String rolBD = usuario.getRol();  // superAdmin, administrador, etc.

    // Convertir a ROLE_*** usable por Spring Security
    String authority;

    switch (rolBD.toLowerCase()) {

        case "superadmin":   // superAdmin en BD
            authority = "ROLE_SUPERADMIN";
            break;

        case "administrador": // admin normal
            authority = "ROLE_ADMIN";
            break;

        default:
            throw new UsernameNotFoundException("Acceso denegado");
    }

    return new User(
        usuario.getCorreo(),
        usuario.getPassword(),
        Collections.singletonList(new SimpleGrantedAuthority(authority))
    );
}
}
