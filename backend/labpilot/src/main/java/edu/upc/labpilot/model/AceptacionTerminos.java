package edu.upc.labpilot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "aceptacion_terminos")
public class AceptacionTerminos {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aceptacion")
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrato", nullable = false)
    private ContratoUso contrato;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "fecha_aceptacion")
    private LocalDateTime fechaAceptacion;
    
    @Column(name = "hash_firma", nullable = false, length = 255)
    private String hashFirma;
    
    // Constructores
    public AceptacionTerminos() {
        this.fechaAceptacion = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    
    public ContratoUso getContrato() { return contrato; }
    public void setContrato(ContratoUso contrato) { this.contrato = contrato; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public LocalDateTime getFechaAceptacion() { return fechaAceptacion; }
    public void setFechaAceptacion(LocalDateTime fechaAceptacion) { this.fechaAceptacion = fechaAceptacion; }
    
    public String getHashFirma() { return hashFirma; }
    public void setHashFirma(String hashFirma) { this.hashFirma = hashFirma; }
}