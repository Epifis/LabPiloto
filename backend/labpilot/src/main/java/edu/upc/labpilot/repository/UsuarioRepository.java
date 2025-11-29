package edu.upc.labpilot.repository;

import edu.upc.labpilot.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    // Métodos básicos
    Usuario findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    
    // Métodos para verificación
    Optional<Usuario> findByTokenVerificacion(String tokenVerificacion);
    
    // Métodos de búsqueda por estado y rol
    List<Usuario> findByRol(String rol);
    List<Usuario> findByActivoTrue();
    List<Usuario> findByActivoFalse();
    List<Usuario> findByCorreoVerificadoTrue();
    List<Usuario> findByCorreoVerificadoFalse();
    
    // Métodos de conteo
    long countByRol(String rol);
    long countByActivoTrue();
    long countByActivoFalse();
    
    // Búsqueda combinada
    List<Usuario> findByRolAndActivoTrue(String rol);
    List<Usuario> findByRolAndCorreoVerificadoTrue(String rol);
    
    // Búsqueda por MFA
    List<Usuario> findByMfaHabilitadoTrue();
    long countByMfaHabilitadoTrue();
    
    // Búsqueda por programa
    List<Usuario> findByPrograma(String programa);
    List<Usuario> findByProgramaContainingIgnoreCase(String programa);
    
    // Búsqueda por nombre/apellido
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Usuario> findByNombreOrApellidoContainingIgnoreCase(String nombre);
    
    // Usuarios que necesitan verificación
    @Query("SELECT u FROM Usuario u WHERE u.activo = false AND u.correoVerificado = false")
    List<Usuario> findPendientesVerificacion();
}