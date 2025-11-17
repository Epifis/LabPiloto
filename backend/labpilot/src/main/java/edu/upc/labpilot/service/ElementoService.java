package edu.upc.labpilot.service;

import edu.upc.labpilot.model.Elemento;
import edu.upc.labpilot.repository.ElementoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ElementoService {

    @Autowired
    private ElementoRepository elementoRepository;

    public List<Elemento> getAll() {
        return elementoRepository.findAll();
    }

    public Optional<Elemento> getById(Integer id) {
        return elementoRepository.findById(id);
    }

    public Elemento save(Elemento elemento) {
        return elementoRepository.save(elemento);
    }

    public void delete(Integer id) {
        elementoRepository.deleteById(id);
    }

    // Elementos disponibles (donde estado = 'Disponible')
    public List<Elemento> getDisponibles() {
        return elementoRepository.findAll().stream()
            .filter(e -> "Disponible".equalsIgnoreCase(e.getEstado()))
            .toList();
    }
}