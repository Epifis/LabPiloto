package edu.upc.labpilot.dto;

import java.util.List;

public class ReservaRequest {
    private String fechaInicio;
    private String fechaFin;
    private String tipoReserva;
    private Integer cantidadEstudiantes;
    private UsuarioReference usuario;
    private LaboratorioReference laboratorio;
    private List<InvitadoRequest> invitados;

    // Getters y Setters
    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getTipoReserva() {
        return tipoReserva;
    }

    public void setTipoReserva(String tipoReserva) {
        this.tipoReserva = tipoReserva;
    }

    public Integer getCantidadEstudiantes() {
        return cantidadEstudiantes;
    }

    public void setCantidadEstudiantes(Integer cantidadEstudiantes) {
        this.cantidadEstudiantes = cantidadEstudiantes;
    }

    public UsuarioReference getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioReference usuario) {
        this.usuario = usuario;
    }

    public LaboratorioReference getLaboratorio() {
        return laboratorio;
    }

    public void setLaboratorio(LaboratorioReference laboratorio) {
        this.laboratorio = laboratorio;
    }

    public List<InvitadoRequest> getInvitados() {
        return invitados;
    }

    public void setInvitados(List<InvitadoRequest> invitados) {
        this.invitados = invitados;
    }

    // Clases internas para referencias
    public static class UsuarioReference {
        private Integer id;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }

    public static class LaboratorioReference {
        private Integer id;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }

    public static class InvitadoRequest {
        private String nombre;
        private String apellido;
        private String documento;

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

        public String getDocumento() {
            return documento;
        }

        public void setDocumento(String documento) {
            this.documento = documento;
        }
    }
}
