package edu.upc.labpilot.dto;

public class MfaRequest {
    private String correo;
    private String codigoMfa;
    private String tipoMfa; // "app", "sms", "email"

    // Constructores
    public MfaRequest() {}

    public MfaRequest(String correo, String codigoMfa, String tipoMfa) {
        this.correo = correo;
        this.codigoMfa = codigoMfa;
        this.tipoMfa = tipoMfa;
    }

    // Getters y Setters
    public String getCorreo() { 
        return correo; 
    }
    
    public void setCorreo(String correo) { 
        this.correo = correo; 
    }
    
    public String getCodigoMfa() { 
        return codigoMfa; 
    }
    
    public void setCodigoMfa(String codigoMfa) { 
        this.codigoMfa = codigoMfa; 
    }
    
    public String getTipoMfa() { 
        return tipoMfa; 
    }
    
    public void setTipoMfa(String tipoMfa) { 
        this.tipoMfa = tipoMfa; 
    }

    @Override
    public String toString() {
        return "MfaRequest{" +
                "correo='" + correo + '\'' +
                ", tipoMfa='" + tipoMfa + '\'' +
                '}';
    }
}