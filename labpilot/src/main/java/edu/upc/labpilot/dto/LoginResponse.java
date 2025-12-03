package edu.upc.labpilot.dto;

public class LoginResponse {
    private Integer id;
    private String nombre;
    private String apellido;
    private String correo;
    private String rol;
    private String token;
    private String jwt;  // Alias para token
    private String documento;
    private String programa;
    private String departamento;  // ⭐ NUEVO
    private String especialidad;  // ⭐ NUEVO

    // Constructor vacío
    public LoginResponse() {}

    // Constructor principal con JWT
    public LoginResponse(Integer id, String nombre, String apellido, String correo, String rol, String jwt) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.rol = rol;
        this.token = jwt;
        this.jwt = jwt;
    }

    // Constructor completo
    public LoginResponse(Integer id, String nombre, String apellido, String correo, String rol, 
                        String jwt, String documento, String programa, String departamento, String especialidad) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.rol = rol;
        this.token = jwt;
        this.jwt = jwt;
        this.documento = documento;
        this.programa = programa;
        this.departamento = departamento;
        this.especialidad = especialidad;
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

    public String getRol() { 
        return rol; 
    }
    
    public void setRol(String rol) { 
        this.rol = rol; 
    }

    public String getToken() { 
        return token; 
    }
    
    public void setToken(String token) { 
        this.token = token;
        this.jwt = token;  // Mantener sincronizado
    }

    public String getJwt() { 
        return jwt; 
    }
    
    public void setJwt(String jwt) { 
        this.jwt = jwt;
        this.token = jwt;  // Mantener sincronizado
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
}