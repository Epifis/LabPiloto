package edu.upc.labpilot.repository;

import edu.upc.labpilot.model.AceptacionTerminos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AceptacionTerminosRepository extends JpaRepository<AceptacionTerminos, Integer> {
    
    // Verificar si un usuario ha aceptado términos
    boolean existsByUsuarioId(Integer usuarioId);
    
    // Verificar si un usuario ha aceptado un contrato específico
    boolean existsByUsuarioIdAndContratoId(Integer usuarioId, Integer contratoId);
    
    // Obtener la última aceptación de un usuario
    Optional<AceptacionTerminos> findTopByUsuarioIdOrderByFechaAceptacionDesc(Integer usuarioId);
    
    // Obtener historial de aceptaciones de un usuario
    List<AceptacionTerminos> findByUsuarioIdOrderByFechaAceptacionDesc(Integer usuarioId);
    
    // Obtener aceptaciones por contrato
    List<AceptacionTerminos> findByContratoIdOrderByFechaAceptacionDesc(Integer contratoId);
    
    // Contar aceptaciones por contrato
    @Query("SELECT COUNT(a) FROM AceptacionTerminos a WHERE a.contrato.id = ?1")
    Long countByContratoId(Integer contratoId);
    
    // Contar usuarios únicos que han aceptado términos
    @Query("SELECT COUNT(DISTINCT a.usuario.id) FROM AceptacionTerminos a")
    Long countDistinctUsuarios();
}