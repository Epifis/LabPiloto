package edu.upc.labpilot.controller;

import edu.upc.labpilot.model.Elemento;
import edu.upc.labpilot.service.ElementoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/elementos")
public class ElementoController {

    @Autowired
    private ElementoService elementoService;

    @GetMapping
    public List<Elemento> getAll() {
        return elementoService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Elemento> getById(@PathVariable Integer id) {
        return elementoService.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/disponibles")
    public List<Elemento> getDisponibles() {
        return elementoService.getDisponibles();
    }

    @PostMapping
    public Elemento create(@RequestBody Elemento elemento) {
        return elementoService.save(elemento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Elemento> update(@PathVariable Integer id, @RequestBody Elemento elemento) {
        return elementoService.getById(id)
            .map(existing -> {
                elemento.setId(id); 
                return ResponseEntity.ok(elementoService.save(elemento));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (elementoService.getById(id).isPresent()) {
            elementoService.delete(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/cantidad")
public ResponseEntity<Elemento> actualizarCantidad(
        @PathVariable Integer id, 
        @RequestParam Integer cantidadDelta) {
    try {
        return elementoService.actualizarCantidad(id, cantidadDelta)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().build();
    }
}

}