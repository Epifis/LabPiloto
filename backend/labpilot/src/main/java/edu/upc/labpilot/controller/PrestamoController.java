package edu.upc.labpilot.controller;

import edu.upc.labpilot.model.Prestamo;
import edu.upc.labpilot.service.PrestamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
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

    @PostMapping("/solicitar")
    public Prestamo solicitar(@RequestBody Prestamo prestamo) {
        return service.solicitar(prestamo);
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<Prestamo> aprobar(@PathVariable Integer id) { 
        return service.aprobar(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
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

    @GetMapping("/pendientes")
    public List<Prestamo> pendientes() { 
        return service.getPendientes(); 
    }
}