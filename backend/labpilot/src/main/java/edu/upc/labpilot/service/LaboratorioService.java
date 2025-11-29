package edu.upc.labpilot.service;

import edu.upc.labpilot.model.Laboratorio;
import edu.upc.labpilot.repository.LaboratorioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LaboratorioService {

    @Autowired
    private LaboratorioRepository laboratorioRepository;

    public List<Laboratorio> getAll() {
        return laboratorioRepository.findAll();
    }

    public Optional<Laboratorio> getById(Integer id) {
        return laboratorioRepository.findById(id);
    }

    public void delete(Integer id) {
        laboratorioRepository.deleteById(id);
    }

    // Laboratorios disponibles (donde estado = 'Activo')
    public List<Laboratorio> getDisponibles() {
        return laboratorioRepository.findAll().stream()
            .filter(lab -> "Activo".equalsIgnoreCase(lab.getEstado()))
            .toList();
    }

    public Laboratorio save(Laboratorio lab) {

    if (lab.getCapacidadDisponible() > lab.getCapacidad()) {
        lab.setCapacidadDisponible(lab.getCapacidad());
    }
    if (lab.getCapacidadDisponible() < 0) {
        lab.setCapacidadDisponible(0);
    }
    if (lab.getCapacidad() < 0) {
        lab.setCapacidad(0);
    }

    return laboratorioRepository.save(lab);
}

}
