package edu.upc.labpilot.repository;

import edu.upc.labpilot.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    boolean existsByCorreo(String correo);
    Usuario findByCorreo(String correo);
}
