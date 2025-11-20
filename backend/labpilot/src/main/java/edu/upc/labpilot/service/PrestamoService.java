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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PrestamoService {

    @Autowired
    private PrestamoRepository repo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private ElementoRepository elementoRepo;

    @Autowired
    private EmailService email;

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
        System.out.println("=== INICIANDO SOLICITUD DE PRÉSTAMO ===");
        
        // Cargar las entidades completas
        if (prestamo.getUsuario() != null && prestamo.getUsuario().getId() != null) {
            Usuario usuario = usuarioRepo.findById(prestamo.getUsuario().getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            prestamo.setUsuario(usuario);
            System.out.println("Usuario: " + usuario.getNombre() + " " + usuario.getApellido());
        }

        if (prestamo.getElemento() != null && prestamo.getElemento().getId() != null) {
            Elemento elemento = elementoRepo.findById(prestamo.getElemento().getId())
                    .orElseThrow(() -> new RuntimeException("Elemento no encontrado"));
            prestamo.setElemento(elemento);
            System.out.println("Elemento solicitado: " + elemento.getNombre());
            System.out.println("Disponibilidad actual: " + elemento.getCantidadDisponible() + "/" + elemento.getCantidadTotal());
        }

        prestamo.setEstado("Pendiente");
        prestamo.setFechaPrestamo(LocalDateTime.now());
        
        Prestamo prestamoGuardado = repo.save(prestamo);
        System.out.println("Préstamo guardado con ID: " + prestamoGuardado.getId());
        System.out.println("=== SOLICITUD COMPLETADA ===");
        
        return prestamoGuardado;
    }

    @Transactional
    public Optional<Prestamo> aprobar(Integer id) {
        return repo.findById(id).map(p -> {
            System.out.println("=== APROBANDO PRÉSTAMO #" + id + " ===");
            
            if (!"Pendiente".equals(p.getEstado())) {
                throw new RuntimeException("Solo se pueden aprobar préstamos en estado Pendiente. Estado actual: " + p.getEstado());
            }
            
            Elemento elemento = p.getElemento();
            
            if (elemento == null) {
                throw new RuntimeException("Elemento no encontrado en el préstamo");
            }
            
            System.out.println("Elemento: " + elemento.getNombre());
            System.out.println("Disponibilidad actual: " + elemento.getCantidadDisponible() + "/" + elemento.getCantidadTotal());
            
            // ✅ VALIDAR que haya disponibilidad ANTES de aprobar
            if (elemento.getCantidadDisponible() <= 0) {
                throw new RuntimeException(
                    "No hay disponibilidad para el elemento: " + elemento.getNombre() + 
                    ". Disponible: " + elemento.getCantidadDisponible()
                );
            }
            
            // ⚠️ NO reducir inventario aquí - solo cuando se marque como "Prestado"
            // Solo cambiar el estado a Aprobado (autorizado pero no entregado)
            
            p.setEstado("Aprobado");
            Prestamo prestamoActualizado = repo.save(p);
            
            System.out.println("=== PRÉSTAMO APROBADO (Autorizado pero no entregado) ===");
            return prestamoActualizado;
        });
    }
    
    /**
     * ✅ NUEVO: Marcar préstamo como Prestado (entrega física del elemento)
     * AQUÍ es donde se reduce el inventario
     */
    @Transactional
    public Optional<Prestamo> marcarComoPrestado(Integer id) {
        return repo.findById(id).map(p -> {
            System.out.println("=== MARCANDO PRÉSTAMO #" + id + " COMO PRESTADO ===");
            
            if (!"Aprobado".equals(p.getEstado())) {
                throw new RuntimeException("Solo se pueden marcar como prestados los préstamos Aprobados. Estado actual: " + p.getEstado());
            }
            
            Elemento elemento = p.getElemento();
            
            if (elemento == null) {
                throw new RuntimeException("Elemento no encontrado en el préstamo");
            }
            
            System.out.println("Elemento: " + elemento.getNombre());
            System.out.println("Disponibilidad antes de prestar: " + elemento.getCantidadDisponible() + "/" + elemento.getCantidadTotal());
            
            // ✅ VALIDAR disponibilidad al momento de entregar
            if (elemento.getCantidadDisponible() <= 0) {
                throw new RuntimeException(
                    "No se puede entregar el elemento: " + elemento.getNombre() + 
                    ". Disponible: " + elemento.getCantidadDisponible()
                );
            }
            
            // 🔥 AQUÍ reducimos el inventario (entrega física)
            elemento.setCantidadDisponible(elemento.getCantidadDisponible() - 1);
            elementoRepo.save(elemento);
            
            System.out.println("Inventario reducido. Nueva disponibilidad: " + elemento.getCantidadDisponible() + "/" + elemento.getCantidadTotal());
            
            p.setEstado("Prestado");
            p.setFechaPrestamo(LocalDateTime.now()); // Actualizar fecha de préstamo real
            Prestamo prestamoActualizado = repo.save(p);
            
            System.out.println("=== ELEMENTO ENTREGADO - PRÉSTAMO ACTIVO ===");
            return prestamoActualizado;
        });
    }

    @Transactional
    public Optional<Prestamo> rechazar(Integer id) {
        return repo.findById(id).map(p -> {
            System.out.println("Rechazando préstamo #" + id);
            p.setEstado("Rechazado");
            return repo.save(p);
        });
    }

    @Transactional
    public Optional<Prestamo> devolver(Integer id) {
        return repo.findById(id).map(p -> {
            System.out.println("=== DEVOLVIENDO PRÉSTAMO #" + id + " ===");
            
            Elemento elemento = p.getElemento();
            
            if (elemento == null) {
                throw new RuntimeException("Elemento no encontrado en el préstamo");
            }
            
            System.out.println("Elemento: " + elemento.getNombre());
            System.out.println("Estado actual del préstamo: " + p.getEstado());
            System.out.println("Disponibilidad antes de devolver: " + elemento.getCantidadDisponible() + "/" + elemento.getCantidadTotal());
            
            // ✅ Solo se puede devolver si está en estado "Prestado"
            if (!"Prestado".equals(p.getEstado())) {
                throw new RuntimeException(
                    "Solo se pueden marcar como devueltos los préstamos en estado 'Prestado'. " +
                    "Estado actual: " + p.getEstado()
                );
            }
            
            // Incrementar disponibilidad (devolver al inventario)
            elemento.setCantidadDisponible(elemento.getCantidadDisponible() + 1);
            
            // ✅ VALIDAR que no se exceda la cantidad total
            if (elemento.getCantidadDisponible() > elemento.getCantidadTotal()) {
                System.err.println("⚠️ ADVERTENCIA: Se detectó inconsistencia en el inventario");
                System.err.println("Disponible calculado: " + elemento.getCantidadDisponible());
                System.err.println("Total en sistema: " + elemento.getCantidadTotal());
                
                // Corregir la inconsistencia
                elemento.setCantidadDisponible(elemento.getCantidadTotal());
                System.err.println("Cantidad disponible corregida a: " + elemento.getCantidadTotal());
            }
            
            elementoRepo.save(elemento);
            
            System.out.println("Nueva disponibilidad: " + elemento.getCantidadDisponible() + "/" + elemento.getCantidadTotal());
            
            p.setEstado("Devuelto");
            p.setFechaDevolucion(LocalDateTime.now());
            Prestamo prestamoActualizado = repo.save(p);
            
            System.out.println("=== DEVOLUCIÓN COMPLETADA ===");
            return prestamoActualizado;
        });
    }

    public List<Prestamo> getPendientes() {
        return repo.findAll().stream()
                .filter(p -> "Pendiente".equalsIgnoreCase(p.getEstado()))
                .toList();
    }
    
    /**
     * ✅ NUEVO: Método para validar disponibilidad antes de aprobar múltiples préstamos
     */
    @Transactional
    public void validarDisponibilidadParaAprobar(List<Integer> ids) {
        System.out.println("=== VALIDANDO DISPONIBILIDAD PARA " + ids.size() + " PRÉSTAMOS ===");
        
        for (Integer id : ids) {
            Prestamo prestamo = repo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Préstamo #" + id + " no encontrado"));
            
            Elemento elemento = prestamo.getElemento();
            
            if (elemento.getCantidadDisponible() <= 0) {
                throw new RuntimeException(
                    "No se puede aprobar el préstamo #" + id + ". " +
                    "Elemento: " + elemento.getNombre() + " - Sin disponibilidad"
                );
            }
        }
        
        System.out.println("✅ Validación exitosa - Todos los elementos tienen disponibilidad");
    }

    public void aprobarPrestamos(List<Integer> ids) {

        // Agrupa por usuario
        Map<Usuario, List<Prestamo>> grupo = new HashMap<>();

        for (Integer id : ids) {
            Prestamo p = repo.findById(id).orElseThrow();
            p.setEstado("Aprobado");
            repo.save(p);

            grupo.computeIfAbsent(p.getUsuario(), k -> new ArrayList<>()).add(p);
        }

        // Mandar correo por cada usuario
        for (var entry : grupo.entrySet()) {
            Usuario u = entry.getKey();
            List<Prestamo> lista = entry.getValue();

            List<String> elementos = lista.stream()
                    .map(p -> p.getElemento().getNombre())
                    .toList();

            email.notificarPrestamoAprobado(
                    u.getCorreo(),
                    u.getNombre(),
                    lista.get(0).getFechaPrestamo().toString(),
                    elementos
            );
        }
    }

    public void rechazarPrestamos(List<Integer> ids) {

        for (Integer id : ids) {
            Prestamo p = repo.findById(id).orElseThrow();
            p.setEstado("Rechazado");
            repo.save(p);

            Usuario u = p.getUsuario();
            email.notificarPrestamoRechazado(u.getCorreo(), u.getNombre());
        }
    }
}
