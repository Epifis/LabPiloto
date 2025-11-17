package edu.upc.labpilot.service;

import edu.upc.labpilot.model.Usuario;
import edu.upc.labpilot.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    
    // Método para verificar si existe un correo
    public boolean existsByCorreo(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }
}