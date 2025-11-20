package edu.upc.labpilot.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class RecurrenceRequest {
    private Integer idUsuario;
    private Integer idLaboratorio;
    private String nrcCurso;
    private LocalDate fechaInicio;
    private Integer cantidadSemanas;
    private List<Integer> diasSemana; // 1=Lunes, 2=Martes, ..., 6=Sábado
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer cantidadEstudiantes;
    private String tipoReserva = "clase"; // Siempre "clase" para recurrentes

    // Getters y Setters
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    
    public Integer getIdLaboratorio() { return idLaboratorio; }
    public void setIdLaboratorio(Integer idLaboratorio) { this.idLaboratorio = idLaboratorio; }
    
    public String getNrcCurso() { return nrcCurso; }
    public void setNrcCurso(String nrcCurso) { this.nrcCurso = nrcCurso; }
    
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    
    public Integer getCantidadSemanas() { return cantidadSemanas; }
    public void setCantidadSemanas(Integer cantidadSemanas) { this.cantidadSemanas = cantidadSemanas; }
    
    public List<Integer> getDiasSemana() { return diasSemana; }
    public void setDiasSemana(List<Integer> diasSemana) { this.diasSemana = diasSemana; }
    
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    
    public Integer getCantidadEstudiantes() { return cantidadEstudiantes; }
    public void setCantidadEstudiantes(Integer cantidadEstudiantes) { this.cantidadEstudiantes = cantidadEstudiantes; }
    
    public String getTipoReserva() { return tipoReserva; }
    public void setTipoReserva(String tipoReserva) { this.tipoReserva = tipoReserva; }
}
