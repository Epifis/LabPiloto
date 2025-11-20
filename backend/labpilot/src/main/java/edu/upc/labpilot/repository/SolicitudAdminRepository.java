package edu.upc.labpilot.repository;

import edu.upc.labpilot.model.SolicitudAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SolicitudAdminRepository extends JpaRepository<SolicitudAdmin, Integer> {
    boolean existsByCorreo(String correo);
    Optional<SolicitudAdmin> findByTokenValidacion(String token);
}
