package edu.upc.labpilot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mfa_codes")
public class MfaCode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mfa")
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    
    @Column(name = "codigo", nullable = false, length = 10)
    private String codigo;
    
    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo; // 'sms', 'email', 'app'
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;
    
    @Column(name = "utilizado")
    private Boolean utilizado = false;

    // Constructores
    public MfaCode() {
        this.fechaCreacion = LocalDateTime.now();
        this.utilizado = false;
    }
    
    public MfaCode(Usuario usuario, String codigo, String tipo, LocalDateTime fechaExpiracion) {
        this();
        this.usuario = usuario;
        this.codigo = codigo;
        this.tipo = tipo;
        this.fechaExpiracion = fechaExpiracion;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
    
    public Boolean getUtilizado() { return utilizado; }
    public void setUtilizado(Boolean utilizado) { this.utilizado = utilizado; }

    /**
     * Verifica si el código ha expirado
     */
    public boolean haExpirado() {
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }

    /**
     * Marca el código como utilizado
     */
    public void marcarComoUtilizado() {
        this.utilizado = true;
    }

    /**
     * Verifica si el código es válido (no expirado y no utilizado)
     */
    public boolean esValido() {
        return !utilizado && fechaExpiracion.isAfter(LocalDateTime.now());
    }
}