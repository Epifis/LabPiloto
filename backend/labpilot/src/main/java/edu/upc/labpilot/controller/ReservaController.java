package edu.upc.labpilot.controller;

import edu.upc.labpilot.dto.ReservaRequest;
import edu.upc.labpilot.model.*;
import edu.upc.labpilot.service.ReservaService;
import edu.upc.labpilot.service.UsuarioService;
import edu.upc.labpilot.service.LaboratorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = "*")
public class ReservaController {

    @Autowired
    private ReservaService service;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private LaboratorioService laboratorioService;

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

    /**
     * ✅ ENDPOINT PÚBLICO para solicitar reservas CON INVITADOS
     * Acepta ReservaRequest DTO y convierte a entidad Reserva
     */
    @PostMapping(value = "/solicitar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> solicitar(@RequestBody ReservaRequest request) {
        try {
            System.out.println("=== SOLICITUD DE RESERVA RECIBIDA ===");
            System.out.println("Request completo: " + request);
            System.out.println("Fecha inicio: " + request.getFechaInicio());
            System.out.println("Fecha fin: " + request.getFechaFin());
            System.out.println("Usuario ID: " + (request.getUsuario() != null ? request.getUsuario().getId() : "NULL"));
            System.out.println("Laboratorio ID: " + (request.getLaboratorio() != null ? request.getLaboratorio().getId() : "NULL"));
            System.out.println("Cantidad estudiantes: " + request.getCantidadEstudiantes());
            System.out.println("Invitados: " + (request.getInvitados() != null ? request.getInvitados().size() : 0));

            // Validaciones
            if (request.getUsuario() == null || request.getUsuario().getId() == null) {
                throw new RuntimeException("Usuario es requerido");
            }
            if (request.getLaboratorio() == null || request.getLaboratorio().getId() == null) {
                throw new RuntimeException("Laboratorio es requerido");
            }

            // Validar usuario
            Usuario usuario = usuarioService.getById(request.getUsuario().getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + request.getUsuario().getId()));

            // Validar laboratorio
            Laboratorio laboratorio = laboratorioService.getById(request.getLaboratorio().getId())
                    .orElseThrow(() -> new RuntimeException("Laboratorio no encontrado con ID: " + request.getLaboratorio().getId()));

            // Convertir fechas
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime fechaInicio = LocalDateTime.parse(request.getFechaInicio(), formatter);
            LocalDateTime fechaFin = LocalDateTime.parse(request.getFechaFin(), formatter);

            // Crear entidad Reserva
            Reserva reserva = new Reserva();
            reserva.setUsuario(usuario);
            reserva.setLaboratorio(laboratorio);
            reserva.setFechaInicio(fechaInicio);
            reserva.setFechaFin(fechaFin);
            reserva.setTipoReserva(request.getTipoReserva());
            reserva.setCantidadEstudiantes(request.getCantidadEstudiantes());
            reserva.setEstado("pendiente");
            reserva.setFechaCreacion(LocalDateTime.now());

            // Procesar invitados si existen
            if (request.getInvitados() != null && !request.getInvitados().isEmpty()) {
                List<ReservaInvitado> invitados = request.getInvitados().stream()
                        .map(inv -> {
                            ReservaInvitado invitado = new ReservaInvitado();
                            invitado.setNombre(inv.getNombre());
                            invitado.setApellido(inv.getApellido() != null && !inv.getApellido().isEmpty() ? inv.getApellido() : "");
                            invitado.setDocumento(inv.getDocumento() != null ? inv.getDocumento() : "");
                            invitado.setReserva(reserva);
                            return invitado;
                        })
                        .collect(Collectors.toList());

                reserva.setInvitados(invitados);
                System.out.println("✅ " + invitados.size() + " invitados procesados");
            }

            // Guardar reserva
            Reserva nuevaReserva = service.solicitar(reserva);
            System.out.println("✅ Reserva creada con ID: " + nuevaReserva.getId());
            System.out.println("====================================");

            return ResponseEntity.ok(nuevaReserva);

        } catch (Exception e) {
            System.err.println("❌ Error al procesar reserva:");
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
            List<Reserva> reservas = service.crearReservasRecurrentes(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Reservas recurrentes creadas exitosamente",
                    "totalReservas", reservas.size(),
                    "reservas", reservas
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
