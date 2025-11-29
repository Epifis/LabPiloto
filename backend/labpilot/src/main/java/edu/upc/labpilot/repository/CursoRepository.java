package edu.upc.labpilot.repository;

import edu.upc.labpilot.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CursoRepository extends JpaRepository<Curso, String> {
    // Métodos básicos proporcionados por JpaRepository
}