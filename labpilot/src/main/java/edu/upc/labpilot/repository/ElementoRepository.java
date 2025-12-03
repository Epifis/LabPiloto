package edu.upc.labpilot.repository;

import edu.upc.labpilot.model.Elemento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElementoRepository extends JpaRepository<Elemento, Integer> {
}