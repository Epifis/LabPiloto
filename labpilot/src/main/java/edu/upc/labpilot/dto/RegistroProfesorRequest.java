package edu.upc.labpilot.dto;

public class RegistroProfesorRequest {
    private String nombre;
    private String apellido;
    private String correo;
    private String password;
    private String documento;
    private String departamento;
    private String especialidad;
    private String telefono;
    private boolean aceptaTerminos;
    private String firmaDigital;

    // Constructores
    public RegistroProfesorRequest() {}

    public RegistroProfesorRequest(String nombre, String apellido, String correo, 
                                  String password, String documento, 
                                  String departamento, String especialidad,
                                  String telefono, boolean aceptaTerminos, String firmaDigital) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.password = password;
        this.documento = documento;
        this.departamento = departamento;
        this.especialidad = especialidad;
        this.telefono = telefono;
        this.aceptaTerminos = aceptaTerminos;
        this.firmaDigital = firmaDigital;
    }

    // Getters y Setters
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

    public String getDocumento() { 
        return documento; 
    }
    
    public void setDocumento(String documento) { 
        this.documento = documento; 
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

    public String getTelefono() { 
        return telefono; 
    }
    
    public void setTelefono(String telefono) { 
        this.telefono = telefono; 
    }

    public boolean isAceptaTerminos() { 
        return aceptaTerminos; 
    }
    
    public void setAceptaTerminos(boolean aceptaTerminos) { 
        this.aceptaTerminos = aceptaTerminos; 
    }

    public String getFirmaDigital() { 
        return firmaDigital; 
    }
    
    public void setFirmaDigital(String firmaDigital) { 
        this.firmaDigital = firmaDigital; 
    }

    @Override
    public String toString() {
        return "RegistroProfesorRequest{" +
                "nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", correo='" + correo + '\'' +
                ", documento='" + documento + '\'' +
                ", departamento='" + departamento + '\'' +
                ", especialidad='" + especialidad + '\'' +
                ", telefono='" + telefono + '\'' +
                ", aceptaTerminos=" + aceptaTerminos +
                '}';
    }
}