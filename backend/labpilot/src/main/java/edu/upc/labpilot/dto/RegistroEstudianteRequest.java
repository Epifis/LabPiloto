package edu.upc.labpilot.dto;

public class RegistroEstudianteRequest {
    private String nombre;
    private String apellido;
    private String correo;
    private String documento;
    private String programa;
    private String telefono;
    private String password;
    private boolean aceptaTerminos;
    private String firmaDigital;

    // Constructores
    public RegistroEstudianteRequest() {}

    public RegistroEstudianteRequest(String nombre, String apellido, String correo, 
                                    String password, String documento, String programa,
                                    String telefono, boolean aceptaTerminos, String firmaDigital) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.password = password;
        this.documento = documento;
        this.programa = programa;
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
    
    public String getTelefono() { 
        return telefono; 
    }
    
    public void setTelefono(String telefono) { 
        this.telefono = telefono; 
    }
    
    public String getPassword() { 
        return password; 
    }
    
    public void setPassword(String password) { 
        this.password = password; 
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
        return "RegistroEstudianteRequest{" +
                "nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", correo='" + correo + '\'' +
                ", documento='" + documento + '\'' +
                ", programa='" + programa + '\'' +
                ", telefono='" + telefono + '\'' +
                ", aceptaTerminos=" + aceptaTerminos +
                '}';
    }
}