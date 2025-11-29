package edu.upc.labpilot.repository;

import edu.upc.labpilot.model.ContratoUso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContratoUsoRepository extends JpaRepository<ContratoUso, Integer> {
    
    // Obtener SOLO el contrato activo
    @Query("SELECT c FROM ContratoUso c WHERE c.activo = true ORDER BY c.fechaCreacion DESC LIMIT 1")
    Optional<ContratoUso> findTopByActivoTrueOrderByFechaCreacionDesc();
    
    // Obtener contrato por tipo (solo activo)
    @Query("SELECT c FROM ContratoUso c WHERE c.tipoContrato = ?1 AND c.activo = true ORDER BY c.fechaCreacion DESC LIMIT 1")
    Optional<ContratoUso> findByTipoContratoAndActivoTrue(String tipoContrato);
    
    // Obtener todos los contratos de un tipo
    List<ContratoUso> findByTipoContrato(String tipoContrato);
    
    // Obtener todos los contratos ordenados
    List<ContratoUso> findAllByOrderByActivoDescFechaCreacionDesc();
    
    // Obtener contratos por tipo ordenados
    List<ContratoUso> findByTipoContratoOrderByActivoDescFechaCreacionDesc(String tipoContrato);
    
    // Alias para contrato vigente
    default Optional<ContratoUso> findByActivoTrue() {
        return findTopByActivoTrueOrderByFechaCreacionDesc();
    }
}