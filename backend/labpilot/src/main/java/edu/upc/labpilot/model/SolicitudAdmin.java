package edu.upc.labpilot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_admin")
public class SolicitudAdmin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Integer id;
    
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;
    
    @Column(name = "apellido", nullable = false, length = 50)
    private String apellido;
    
    @Column(name = "correo", nullable = false, length = 100, unique = true)
    private String correo;
    
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "Pendiente"; // "Aprobada", "Rechazada", "Pendiente"
    
    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;
    
    @Column(name = "token_validacion", length = 255)
    private String tokenValidacion;

    // Constructores
    public SolicitudAdmin() {
        this.fechaSolicitud = LocalDateTime.now();
        this.estado = "Pendiente";
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
    
    public String getTokenValidacion() { return tokenValidacion; }
    public void setTokenValidacion(String tokenValidacion) { this.tokenValidacion = tokenValidacion; }

    /**
     * Obtiene el nombre completo
     */
    public String getNombreCompleto() {
        return this.nombre + " " + this.apellido;
    }

    /**
     * Verifica si la solicitud está pendiente
     */
    public boolean estaPendiente() {
        return "Pendiente".equals(this.estado);
    }

    /**
     * Verifica si la solicitud está aprobada
     */
    public boolean estaAprobada() {
        return "Aprobada".equals(this.estado);
    }

    /**
     * Verifica si la solicitud está rechazada
     */
    public boolean estaRechazada() {
        return "Rechazada".equals(this.estado);
    }
}