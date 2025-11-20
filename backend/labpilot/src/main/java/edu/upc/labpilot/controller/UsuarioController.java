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
@CrossOrigin(origins = "*") // ✅ Asegurar CORS
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
    
    /**
     * ✅ ENDPOINT PÚBLICO para registrar usuarios (estudiantes desde formularios)
     * Permite crear usuarios SIN contraseña (para reservas/préstamos públicos)
     */
    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        try {
            System.out.println("=== REGISTRO DE USUARIO ===");
            System.out.println("Datos recibidos: " + usuario);
            System.out.println("Nombre: " + usuario.getNombre());
            System.out.println("Apellido: " + usuario.getApellido());
            System.out.println("Correo: " + usuario.getCorreo());
            System.out.println("Documento: " + usuario.getDocumento());
            System.out.println("Programa: " + usuario.getPrograma());
            System.out.println("Rol: " + usuario.getRol());
            
            // Validaciones básicas
            if (usuario.getCorreo() == null || usuario.getCorreo().trim().isEmpty()) {
                System.err.println("❌ Error: Correo vacío");
                return ResponseEntity.badRequest().body(Map.of("error", "El correo es obligatorio"));
            }
            
            if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
                System.err.println("❌ Error: Nombre vacío");
                return ResponseEntity.badRequest().body(Map.of("error", "El nombre es obligatorio"));
            }
            
            if (usuario.getApellido() == null || usuario.getApellido().trim().isEmpty()) {
                System.err.println("❌ Error: Apellido vacío");
                return ResponseEntity.badRequest().body(Map.of("error", "El apellido es obligatorio"));
            }
            
            // ✅ Validar que el correo no exista ya
            if (usuarioService.existsByCorreo(usuario.getCorreo())) {
                System.err.println("❌ Error: Correo ya existe - " + usuario.getCorreo());
                return ResponseEntity.badRequest().body(Map.of("error", "Ya existe un usuario con ese correo"));
            }
            
            // ✅ NO permitir registro directo de ADMIN desde este endpoint
            if ("ADMIN".equalsIgnoreCase(usuario.getRol()) || 
                "administrador".equalsIgnoreCase(usuario.getRol()) ||
                "superAdmin".equalsIgnoreCase(usuario.getRol()) ||
                "superadmin".equalsIgnoreCase(usuario.getRol())) {
                System.err.println("❌ Error: Intento de crear admin desde endpoint público");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "No se puede registrar administradores directamente. " +
                              "Por favor usa el formulario de solicitud de cuenta de administrador."));
            }
            
            // ✅ Establecer rol por defecto si no viene
            if (usuario.getRol() == null || usuario.getRol().trim().isEmpty()) {
                usuario.setRol("estudiante");
            }
            
            // ✅ Permitir password vacío o null para usuarios públicos (estudiantes sin cuenta)
            if (usuario.getPassword() == null) {
                usuario.setPassword("");
            }
            
            // ✅ Establecer activo por defecto
            if (usuario.getActivo() == null) {
                usuario.setActivo(true);
            }
            
            System.out.println("✅ Validaciones pasadas, guardando usuario...");
            
            // Guardar usuario
            Usuario saved = usuarioService.save(usuario);
            
            System.out.println("✅ Usuario guardado exitosamente con ID: " + saved.getId());
            System.out.println("=========================");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
            
        } catch (Exception e) {
            System.err.println("❌ Error inesperado al crear usuario:");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al crear usuario: " + e.getMessage()));
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
