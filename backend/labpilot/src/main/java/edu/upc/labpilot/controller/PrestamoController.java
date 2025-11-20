package edu.upc.labpilot.controller;

import edu.upc.labpilot.model.Prestamo;
import edu.upc.labpilot.service.PrestamoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.hibernate.property.access.internal.PropertyAccessStrategyCompositeUserTypeImpl;

@RestController
@RequestMapping("/api/prestamos")
@CrossOrigin(origins = "*") // ✅ Permitir CORS
public class PrestamoController {

    @Autowired
    private PrestamoService service;

    @GetMapping
    public List<Prestamo> getAll() { 
        return service.getAll(); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prestamo> getById(@PathVariable Integer id) { 
        return service.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Prestamo> getByUsuario(@PathVariable Integer usuarioId) {
        return service.getByUsuario(usuarioId);
    }

    /**
     * ✅ ENDPOINT PÚBLICO para solicitar préstamos
     * No requiere autenticación
     */
    @PostMapping("/solicitar")
    public ResponseEntity<?> solicitar(@RequestBody Prestamo prestamo) {
        try {
            Prestamo nuevoPrestamo = service.solicitar(prestamo);
            return ResponseEntity.ok(nuevoPrestamo);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobar(@PathVariable Integer id) {
        try {
            return service.aprobar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * ✅ NUEVO: Marcar préstamo como "Prestado" (entrega física)
     */
    @PutMapping("/{id}/prestar")
    public ResponseEntity<?> marcarComoPrestado(@PathVariable Integer id) {
        try {
            return service.marcarComoPrestado(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<Prestamo> rechazar(@PathVariable Integer id) { 
        return service.rechazar(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/devolver")
    public ResponseEntity<Prestamo> devolver(@PathVariable Integer id) { 
        return service.devolver(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/aprobar-lote")
    public ResponseEntity<?> aprobarLote(@RequestBody List<Integer> ids) {
        service.aprobarPrestamos(ids);
        return ResponseEntity.ok("Aprobados y notificados");
    }

    @PutMapping("/rechazar-lote")
    public ResponseEntity<?> rechazarLote(@RequestBody List<Integer> ids) {
        service.rechazarPrestamos(ids);
        return ResponseEntity.ok("Rechazados y notificados");
    }

    @GetMapping("/pendientes")
    public List<Prestamo> pendientes() { 
        return service.getPendientes(); 
    }
}
