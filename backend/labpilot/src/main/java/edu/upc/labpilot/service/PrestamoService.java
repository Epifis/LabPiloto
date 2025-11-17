package edu.upc.labpilot.service;

import edu.upc.labpilot.model.Prestamo;
import edu.upc.labpilot.model.Usuario;
import edu.upc.labpilot.model.Elemento;
import edu.upc.labpilot.repository.PrestamoRepository;
import edu.upc.labpilot.repository.UsuarioRepository;
import edu.upc.labpilot.repository.ElementoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PrestamoService {

    @Autowired
    private PrestamoRepository repo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private ElementoRepository elementoRepo;

    public List<Prestamo> getAll() {
        return repo.findAll();
    }

    public Optional<Prestamo> getById(Integer id) {
        return repo.findById(id);
    }

    public List<Prestamo> getByUsuario(Integer usuarioId) {
        return repo.findAll().stream()
                .filter(p -> p.getUsuario() != null && p.getUsuario().getId().equals(usuarioId))
                .toList();
    }

    @Transactional
    public Prestamo solicitar(Prestamo prestamo) {
        // Cargar las entidades completas
        if (prestamo.getUsuario() != null && prestamo.getUsuario().getId() != null) {
            Usuario usuario = usuarioRepo.findById(prestamo.getUsuario().getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            prestamo.setUsuario(usuario);
        }

        if (prestamo.getElemento() != null && prestamo.getElemento().getId() != null) {
            Elemento elemento = elementoRepo.findById(prestamo.getElemento().getId())
                    .orElseThrow(() -> new RuntimeException("Elemento no encontrado"));
            prestamo.setElemento(elemento);
        }

        prestamo.setEstado("Pendiente");
        prestamo.setFechaPrestamo(LocalDateTime.now());
        return repo.save(prestamo);
    }

    @Transactional
    public Optional<Prestamo> aprobar(Integer id) {
        return repo.findById(id).map(p -> {
            p.setEstado("Aprobado");

            Elemento elemento = p.getElemento();
            if (elemento != null && elemento.getCantidadDisponible() > 0) {
                elemento.setCantidadDisponible(elemento.getCantidadDisponible() - 1);
                elementoRepo.save(elemento);
            }

            return repo.save(p);
        });
    }

    @Transactional
    public Optional<Prestamo> rechazar(Integer id) {
        return repo.findById(id).map(p -> {
            p.setEstado("Rechazado");
            return repo.save(p);
        });
    }

    @Transactional
    public Optional<Prestamo> devolver(Integer id) {
        return repo.findById(id).map(p -> {
            p.setEstado("Devuelto");
            p.setFechaDevolucion(LocalDateTime.now());

            Elemento elemento = p.getElemento();
            if (elemento != null) {
                elemento.setCantidadDisponible(elemento.getCantidadDisponible() + 1);
                elementoRepo.save(elemento);
            }

            return repo.save(p);
        });
    }

    public List<Prestamo> getPendientes() {
        return repo.findAll().stream()
                .filter(p -> "Pendiente".equalsIgnoreCase(p.getEstado()))
                .toList();
    }
}
