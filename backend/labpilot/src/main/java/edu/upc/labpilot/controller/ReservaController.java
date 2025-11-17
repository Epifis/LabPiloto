package edu.upc.labpilot.controller;

import edu.upc.labpilot.model.Reserva;
import edu.upc.labpilot.model.RecurrenceRequest;
import edu.upc.labpilot.service.ReservaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    @Autowired
    private ReservaService service;

    @GetMapping
    public List<Reserva> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reserva> getById(@PathVariable Integer id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Reserva> getByUsuario(@PathVariable Integer usuarioId) {
        return service.getByUsuario(usuarioId);
    }

    @PostMapping("/solicitar")
    public ResponseEntity<?> solicitar(@RequestBody Reserva reserva) {
        try {
            Reserva nuevaReserva = service.solicitar(reserva);
            return ResponseEntity.ok(nuevaReserva);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<Reserva> aprobar(@PathVariable Integer id) {
        return service.aprobar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<Reserva> rechazar(@PathVariable Integer id) {
        return service.rechazar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Reserva> cancelar(@PathVariable Integer id) {
        return service.cancelar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pendientes")
    public List<Reserva> pendientes() {
        return service.getPendientes();
    }

    @GetMapping("/disponibilidad")
    public String disponibilidad() {
        return "Comprobando disponibilidad de laboratorios...";
    }

    @PostMapping("/recurrentes")
    public ResponseEntity<?> crearRecurrentes(@RequestBody RecurrenceRequest request) {
        try {
            // Validar que el usuario actual es superAdmin
            // authenticationService.validarSuperAdmin();

            List<Reserva> reservas = service.crearReservasRecurrentes(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Reservas recurrentes creadas exitosamente",
                    "totalReservas", reservas.size(),
                    "reservas", reservas
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/activar")
    public ResponseEntity<Reserva> activar(@PathVariable Integer id) {
        return service.activar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/completar")
    public ResponseEntity<Reserva> completar(@PathVariable Integer id) {
        return service.completar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
