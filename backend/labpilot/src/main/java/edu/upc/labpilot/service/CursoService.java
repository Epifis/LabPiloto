package edu.upc.labpilot.service;

import edu.upc.labpilot.model.Curso;
import edu.upc.labpilot.repository.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CursoService {

    @Autowired
    private CursoRepository cursoRepository;

    public List<Curso> getAllCursos() {
        return cursoRepository.findAll();
    }

    public Optional<Curso> getCursoByNrc(String nrc) {
        return cursoRepository.findById(nrc);
    }

    public Curso createCurso(Curso curso) {
        // Validar que el NRC no exista
        if (cursoRepository.existsById(curso.getNrc())) {
            throw new RuntimeException("Ya existe un curso con el NRC: " + curso.getNrc());
        }
        return cursoRepository.save(curso);
    }

    public Curso updateCurso(String nrc, Curso curso) {
        // Verificar que el curso existe
        if (!cursoRepository.existsById(nrc)) {
            throw new RuntimeException("No existe el curso con NRC: " + nrc);
        }
        curso.setNrc(nrc); // Asegurar que el NRC no cambie
        return cursoRepository.save(curso);
    }

    public void deleteCurso(String nrc) {
        if (!cursoRepository.existsById(nrc)) {
            throw new RuntimeException("No existe el curso con NRC: " + nrc);
        }
        cursoRepository.deleteById(nrc);
    }
}
