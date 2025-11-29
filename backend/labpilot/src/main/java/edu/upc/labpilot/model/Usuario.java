package edu.upc.labpilot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios") // ✅ Coincide con la tabla en PostgreSQL
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;
    
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;
    
    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;
    
    @Column(name = "correo", nullable = false, length = 150, unique = true)
    private String correo;
    
    @Column(name = "password", length = 255)
    private String password;
    
    @Column(name = "rol", length = 20, nullable = false)
    private String rol;
    
    @Column(name = "documento", length = 20)
    private String documento;
    
    @Column(name = "programa", length = 100)
    private String programa;
    
    // ⭐ NUEVOS CAMPOS PARA PROFESOR
    @Column(name = "departamento", length = 100)
    private String departamento;
    
    @Column(name = "especialidad", length = 100)
    private String especialidad;
    
    @Column(name = "activo")
    private Boolean activo = true;
    
    @Column(name = "fecha_creacion", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaCreacion;

    // ======= CAMPOS MFA Y VERIFICACIÓN =======
    
    @Column(name = "mfa_secret", length = 32)
    private String mfaSecret;
    
    @Column(name = "mfa_habilitado")
    private Boolean mfaHabilitado = false;
    
    @Column(name = "telefono", length = 20)
    private String telefono;
    
    @Column(name = "correo_verificado")
    private Boolean correoVerificado = false;
    
    @Column(name = "token_verificacion", length = 100)
    private String tokenVerificacion;
    
    @Column(name = "fecha_verificacion")
    private LocalDateTime fechaVerificacion;

    // ======= CONSTRUCTORES =======
    
    public Usuario() {
        this.fechaCreacion = LocalDateTime.now();
        this.activo = true;
        this.correoVerificado = false;
        this.mfaHabilitado = false;
    }
    
    public Usuario(String nombre, String apellido, String correo, String rol) {
        this();
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.rol = rol;
    }

    // ======= GETTERS Y SETTERS =======
    
    public Integer getId() { 
        return id; 
    }
    
    public void setId(Integer id) { 
        this.id = id; 
    }

    public String getNombre() { 
        return nombre; 
    }
    
    public void setNombre(String nombre) { 
        this.nombre = nombre; 
    }

    public String getApellido() { 
        return apellido; 
    }
    
    public void setApellido(String apellido) { 
        this.apellido = apellido; 
    }

    public String getCorreo() { 
        return correo; 
    }
    
    public void setCorreo(String correo) { 
        this.correo = correo; 
    }

    public String getPassword() { 
        return password; 
    }
    
    public void setPassword(String password) { 
        this.password = password; 
    }

    public String getRol() { 
        return rol; 
    }
    
    public void setRol(String rol) { 
        this.rol = rol; 
    }

    public String getDocumento() { 
        return documento; 
    }
    
    public void setDocumento(String documento) { 
        this.documento = documento; 
    }

    public String getPrograma() { 
        return programa; 
    }
    
    public void setPrograma(String programa) { 
        this.programa = programa; 
    }

    // ⭐ NUEVOS GETTERS/SETTERS
    public String getDepartamento() { 
        return departamento; 
    }
    
    public void setDepartamento(String departamento) { 
        this.departamento = departamento; 
    }

    public String getEspecialidad() { 
        return especialidad; 
    }
    
    public void setEspecialidad(String especialidad) { 
        this.especialidad = especialidad; 
    }

    public Boolean getActivo() { 
        return activo; 
    }
    
    public void setActivo(Boolean activo) { 
        this.activo = activo; 
    }

    public LocalDateTime getFechaCreacion() { 
        return fechaCreacion; 
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) { 
        this.fechaCreacion = fechaCreacion; 
    }

    // ======= GETTERS Y SETTERS MFA =======
    
    public String getMfaSecret() { 
        return mfaSecret; 
    }
    
    public void setMfaSecret(String mfaSecret) { 
        this.mfaSecret = mfaSecret; 
    }

    public Boolean getMfaHabilitado() { 
        return mfaHabilitado; 
    }
    
    public void setMfaHabilitado(Boolean mfaHabilitado) { 
        this.mfaHabilitado = mfaHabilitado; 
    }

    public String getTelefono() { 
        return telefono; 
    }
    
    public void setTelefono(String telefono) { 
        this.telefono = telefono; 
    }

    public Boolean getCorreoVerificado() { 
        return correoVerificado; 
    }
    
    public void setCorreoVerificado(Boolean correoVerificado) { 
        this.correoVerificado = correoVerificado; 
    }

    public String getTokenVerificacion() { 
        return tokenVerificacion; 
    }
    
    public void setTokenVerificacion(String tokenVerificacion) { 
        this.tokenVerificacion = tokenVerificacion; 
    }

    public LocalDateTime getFechaVerificacion() { 
        return fechaVerificacion; 
    }
    
    public void setFechaVerificacion(LocalDateTime fechaVerificacion) { 
        this.fechaVerificacion = fechaVerificacion; 
    }

    // ======= ⭐ MÉTODOS is*() PARA BOOLEANOS =======
    // Estos son CRÍTICOS para evitar NullPointerException
    
    public boolean isActivo() {
        return Boolean.TRUE.equals(this.activo);
    }
    
    public boolean isCorreoVerificado() {
        return Boolean.TRUE.equals(this.correoVerificado);
    }
    
    public boolean isMfaHabilitado() {
        return Boolean.TRUE.equals(this.mfaHabilitado);
    }

    // ======= MÉTODOS DE CONVENIENCIA =======
    
    /**
     * Verifica si el usuario es administrador
     */
    public boolean esAdministrador() {
        return "administrador".equalsIgnoreCase(this.rol) || 
               "superadmin".equalsIgnoreCase(this.rol) ||
               "admin".equalsIgnoreCase(this.rol);
    }
    
    /**
     * Verifica si el usuario es estudiante
     */
    public boolean esEstudiante() {
        return "estudiante".equalsIgnoreCase(this.rol);
    }
    
    /**
     * Verifica si el usuario es profesor
     */
    public boolean esProfesor() {
        return "profesor".equalsIgnoreCase(this.rol);
    }
    
    /**
     * Verifica si el usuario es superadmin
     */
    public boolean esSuperAdmin() {
        return "superadmin".equalsIgnoreCase(this.rol);
    }
    
    /**
     * Verifica si el usuario puede iniciar sesión
     * (activo y, si es estudiante/profesor, con correo verificado)
     */
    public boolean puedeIniciarSesion() {
        // Usuario debe estar activo
        if (!isActivo()) {
            return false;
        }
        
        // Estudiantes y profesores deben tener correo verificado
        if ((esEstudiante() || esProfesor()) && !isCorreoVerificado()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Obtiene el nombre completo
     */
    public String getNombreCompleto() {
        return this.nombre + " " + this.apellido;
    }

    // ======= MÉTODOS DE SEGURIDAD =======
    
    /**
     * Marca el correo como verificado
     */
    public void marcarCorreoVerificado() {
        this.correoVerificado = true;
        this.fechaVerificacion = LocalDateTime.now();
        this.tokenVerificacion = null;
    }
    
    /**
     * Habilita MFA con un nuevo secret
     */
    public void habilitarMFA(String secret) {
        this.mfaSecret = secret;
        this.mfaHabilitado = true;
    }
    
    /**
     * Deshabilita MFA
     */
    public void deshabilitarMFA() {
        this.mfaSecret = null;
        this.mfaHabilitado = false;
    }

    // ======= EQUALS Y HASHCODE =======
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario usuario = (Usuario) o;
        return id != null && id.equals(usuario.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // ======= TO STRING =======
    
    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", correo='" + correo + '\'' +
                ", rol='" + rol + '\'' +
                ", activo=" + activo +
                ", correoVerificado=" + correoVerificado +
                ", mfaHabilitado=" + mfaHabilitado +
                '}';
    }
}