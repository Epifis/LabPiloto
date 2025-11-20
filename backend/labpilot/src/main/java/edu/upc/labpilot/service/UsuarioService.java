package edu.upc.labpilot.service;

import edu.upc.labpilot.model.Usuario;
import edu.upc.labpilot.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    public List<Usuario> getAll() {
        return usuarioRepository.findAll();
    }
    
    public Optional<Usuario> getById(Integer id) {
        return usuarioRepository.findById(id);
    }
    
    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
    
    public void delete(Integer id) {
        usuarioRepository.deleteById(id);
    }
    
    // Método para buscar por correo
    public Optional<Usuario> findByCorreo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo);
        return Optional.ofNullable(usuario);
    }
    
    // ✅ NUEVO: Buscar por correo (retorna Usuario directamente)
    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }
    
    // Método para verificar si existe un correo
    public boolean existsByCorreo(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }

    public Usuario cambiarEstado(Integer id, boolean activo) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        u.setActivo(activo);
        return usuarioRepository.save(u);
    }
    
    // ✅ NUEVO: Crear usuario desde formularios públicos (sin contraseña)
    @Transactional
    public Usuario crearUsuario(Usuario usuario) {
        // Validar que el correo no exista
        if (existsByCorreo(usuario.getCorreo())) {
            throw new RuntimeException("Ya existe un usuario con ese correo");
        }
        
        // Establecer valores por defecto
        if (usuario.getFechaCreacion() == null) {
            usuario.setFechaCreacion(LocalDateTime.now());
        }
        
        if (usuario.getActivo() == null) {
            usuario.setActivo(true);
        }
        
        // Para usuarios de formularios públicos, no requieren contraseña
        if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
            usuario.setPassword(""); // Sin contraseña para usuarios públicos
        }
        
        return usuarioRepository.save(usuario);
    }
    
    // ✅ NUEVO: Buscar o crear usuario (usado por reservas y préstamos públicos)
    @Transactional
    public Usuario buscarOCrearUsuario(String nombreCompleto, String correo, String documento, String programa) {
        // Intentar buscar por correo
        Usuario usuario = buscarPorCorreo(correo);
        
        if (usuario != null) {
            return usuario;
        }
        
        // Si no existe, crear nuevo
        String[] partes = nombreCompleto.trim().split("\\s+", 2);
        String nombre = partes[0];
        String apellido = partes.length > 1 ? partes[1] : "";
        
        usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setCorreo(correo);
        usuario.setDocumento(documento);
        usuario.setPrograma(programa);
        usuario.setRol("estudiante");
        usuario.setPassword(""); // Sin contraseña para usuarios públicos
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());
        
        return crearUsuario(usuario);
    }
}
