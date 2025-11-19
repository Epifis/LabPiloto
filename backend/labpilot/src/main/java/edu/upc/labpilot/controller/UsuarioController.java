package edu.upc.labpilot.controller;

import edu.upc.labpilot.model.Usuario;
import edu.upc.labpilot.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @GetMapping
    public List<Usuario> getAll() {
        return usuarioService.getAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getById(@PathVariable Integer id) {
        return usuarioService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        try {
            // Validaciones básicas
            if (usuario.getCorreo() == null || usuario.getCorreo().isEmpty()) {
                return ResponseEntity.badRequest().body("El correo es obligatorio");
            }
            if (usuario.getNombre() == null || usuario.getNombre().isEmpty()) {
                return ResponseEntity.badRequest().body("El nombre es obligatorio");
            }
            if (usuario.getApellido() == null || usuario.getApellido().isEmpty()) {
                return ResponseEntity.badRequest().body("El apellido es obligatorio");
            }
            
            // ✅ NO permitir registro directo de ADMIN desde este endpoint
            if ("ADMIN".equalsIgnoreCase(usuario.getRol()) || "administrador".equalsIgnoreCase(usuario.getRol())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No se puede registrar administradores directamente. " +
                              "Por favor usa el formulario de solicitud de cuenta de administrador.");
            }
            
            // Guardar usuarios normales (estudiantes, profesores)
            Usuario saved = usuarioService.save(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear usuario: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> update(@PathVariable Integer id, @RequestBody Usuario usuario) {
        return usuarioService.getById(id)
                .map(existing -> {
                    usuario.setId(id);
                    return ResponseEntity.ok(usuarioService.save(usuario));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Usuario> cambiarEstado(
            @PathVariable Integer id,
            @RequestBody Map<String, Boolean> body) {

        boolean nuevoEstado = body.get("activo");
        Usuario usuario = usuarioService.cambiarEstado(id, nuevoEstado);
        return ResponseEntity.ok(usuario);
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (usuarioService.getById(id).isPresent()) {
            usuarioService.delete(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}