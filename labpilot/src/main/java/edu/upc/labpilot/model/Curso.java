package edu.upc.labpilot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "curso")
public class Curso {

    @Id
    @Column(name = "nrc", length = 20)
    private String nrc;  // Clave primaria

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    // ======= CONSTRUCTORES =======
    public Curso() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public Curso(String nrc, String nombre) {
        this();
        this.nrc = nrc;
        this.nombre = nombre;
    }

    // ======= GETTERS & SETTERS =======
    public String getNrc() {
        return nrc;
    }

    public void setNrc(String nrc) {
        this.nrc = nrc;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    // ======= MÉTODOS OVERRIDE =======
    @Override
    public String toString() {
        return "Curso{" +
                "nrc='" + nrc + '\'' +
                ", nombre='" + nombre + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Curso)) return false;
        Curso curso = (Curso) o;
        return nrc != null && nrc.equals(curso.nrc);
    }

    @Override
    public int hashCode() {
        return nrc != null ? nrc.hashCode() : 0;
    }
}
