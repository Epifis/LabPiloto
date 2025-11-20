package edu.upc.labpilot.controller;

import edu.upc.labpilot.model.Curso;
import edu.upc.labpilot.service.CursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    @Autowired
    private CursoService cursoService;

    @GetMapping
    public List<Curso> getAllCursos() {
        return cursoService.getAllCursos();
    }

    @GetMapping("/{nrc}")
    public ResponseEntity<Curso> getCursoByNrc(@PathVariable String nrc) {
        return cursoService.getCursoByNrc(nrc)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Curso> createCurso(@RequestBody Curso curso) {
        try {
            Curso nuevoCurso = cursoService.createCurso(curso);
            return ResponseEntity.ok(nuevoCurso);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{nrc}")
    public ResponseEntity<Curso> updateCurso(@PathVariable String nrc, @RequestBody Curso curso) {
        try {
            Curso cursoActualizado = cursoService.updateCurso(nrc, curso);
            return ResponseEntity.ok(cursoActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{nrc}")
    public ResponseEntity<?> deleteCurso(@PathVariable String nrc) {
        try {
            cursoService.deleteCurso(nrc);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
