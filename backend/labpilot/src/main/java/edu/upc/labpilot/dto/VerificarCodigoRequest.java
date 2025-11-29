package edu.upc.labpilot.dto;

public class VerificarCodigoRequest {
    private String correo;
    private String codigo;
    
    // getters y setters
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
}