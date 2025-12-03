package edu.upc.labpilot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contrato_uso")
public class ContratoUso {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato")
    private Integer id;
    
    @Column(name = "version", nullable = false, length = 20)
    private String version;
    
    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "activo")
    private Boolean activo = true;
    
    @Column(name = "tipo_contrato", length = 50)
    private String tipoContrato = "ESTUDIANTE"; // ESTUDIANTE, ADMIN, GENERAL
    
    // Constructores
    public ContratoUso() {
        this.fechaCreacion = LocalDateTime.now();
    }
    
    public ContratoUso(String version, String contenido, String tipoContrato) {
        this();
        this.version = version;
        this.contenido = contenido;
        this.tipoContrato = tipoContrato;
    }
    
    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    
    public String getTipoContrato() { return tipoContrato; }
    public void setTipoContrato(String tipoContrato) { this.tipoContrato = tipoContrato; }
}