package edu.upc.labpilot.controller;

import edu.upc.labpilot.model.Laboratorio;
import edu.upc.labpilot.service.LaboratorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/laboratorios")
public class LaboratorioController {

    @Autowired
    private LaboratorioService laboratorioService;

    @GetMapping
    public List<Laboratorio> getAll() {
        return laboratorioService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Laboratorio> getById(@PathVariable Integer id) {
        return laboratorioService.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/disponibles")
    public List<Laboratorio> getDisponibles() {
        return laboratorioService.getDisponibles();
    }

    @PostMapping
    public Laboratorio create(@RequestBody Laboratorio laboratorio) {
        return laboratorioService.save(laboratorio);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Laboratorio> update(@PathVariable Integer id, @RequestBody Laboratorio laboratorio) {
        return laboratorioService.getById(id)
            .map(existing -> {
                laboratorio.setId(id);
                return ResponseEntity.ok(laboratorioService.save(laboratorio));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (laboratorioService.getById(id).isPresent()) {
            laboratorioService.delete(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}