package edu.upc.labpilot.service;

import edu.upc.labpilot.model.*;
import edu.upc.labpilot.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private LaboratorioRepository laboratorioRepo;

    @Autowired
    private ReservaInvitadoRepository invitadoRepo;

    @Autowired
    private CursoRepository cursoRepository;

    // ========== MÉTODOS DE CONSULTA ==========
    public List<Reserva> getAll() {
        return reservaRepo.findAll();
    }

    public Optional<Reserva> getById(Integer id) {
        return reservaRepo.findById(id);
    }

    public List<Reserva> getByUsuario(Integer usuarioId) {
        return reservaRepo.findAll().stream()
                .filter(r -> r.getUsuario() != null && r.getUsuario().getId().equals(usuarioId))
                .toList();
    }

    public List<Reserva> getPendientes() {
        return reservaRepo.findAll().stream()
                .filter(r -> "Pendiente".equalsIgnoreCase(r.getEstado()))
                .toList();
    }

    // ========== MÉTODO SOLICITAR RESERVA INDIVIDUAL ==========
    @Transactional
public Reserva solicitar(Reserva reserva) {
    // 1️⃣ Validaciones básicas
    Usuario usuario = usuarioRepo.findById(reserva.getUsuario().getId())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    reserva.setUsuario(usuario);

    Laboratorio lab = laboratorioRepo.findById(reserva.getLaboratorio().getId())
            .orElseThrow(() -> new RuntimeException("Laboratorio no encontrado"));
    reserva.setLaboratorio(lab);

    // Validar tipo de reserva
    if (!"clase".equals(reserva.getTipoReserva()) && !"practica_libre".equals(reserva.getTipoReserva())) {
        throw new RuntimeException("Tipo de reserva no válido. Debe ser 'clase' o 'practica_libre'");
    }

    // 2️⃣ Calcular cantidad de estudiantes
    int totalEstudiantes = 1; // el usuario que reserva
    if (reserva.getInvitados() != null) {
        totalEstudiantes += reserva.getInvitados().size();
    }
    reserva.setCantidadEstudiantes(totalEstudiantes);

    // 3️⃣ VERIFICAR PRIORIDAD MÁXIMA: RESERVAS RECURRENTES DE CURSO
    boolean existeRecurrenteCursoEnHorario = existeReservaRecurrenteCursoEnHorario(
        lab.getId(), 
        reserva.getFechaInicio(), 
        reserva.getFechaFin()
    );

    if (existeRecurrenteCursoEnHorario) {
        throw new RuntimeException("Reserva rechazada: Existe una reserva recurrente de curso con prioridad máxima en este horario");
    }

    // 4️⃣ VERIFICAR RESERVAS DE CLASE REGULARES
    boolean existeClaseEnHorario = existeReservaClaseEnHorario(
        lab.getId(), 
        reserva.getFechaInicio(), 
        reserva.getFechaFin()
    );

    if ("practica_libre".equals(reserva.getTipoReserva()) && existeClaseEnHorario) {
        throw new RuntimeException("Reserva rechazada: Existe una reserva de clase en este horario");
    }

    // 5️⃣ CALCULAR CAPACIDAD OCUPADA EN LA FRANJA HORARIA
    int capacidadOcupada = calcularCapacidadOcupadaEnHorario(
        lab.getId(),
        reserva.getFechaInicio(), 
        reserva.getFechaFin()
    );

    int capacidadDisponibleEnHorario = lab.getCapacidad() - capacidadOcupada;

    // 6️⃣ VALIDAR CAPACIDAD
    if (capacidadDisponibleEnHorario < totalEstudiantes) {
        throw new RuntimeException(String.format(
            "Capacidad insuficiente para el horario seleccionado. " +
            "Capacidad total: %d, Ocupado: %d, Disponible: %d, Solicitado: %d",
            lab.getCapacidad(), capacidadOcupada, capacidadDisponibleEnHorario, totalEstudiantes
        ));
    }

    // 7️⃣ PARA PRÁCTICA LIBRE: Verificar capacidad con otras prácticas libres
    if ("practica_libre".equals(reserva.getTipoReserva())) {
        int capacidadOcupadaPorPracticasLibres = calcularCapacidadPracticasLibresAprobadas(
            lab.getId(),
            reserva.getFechaInicio(),
            reserva.getFechaFin()
        );
        
        int capacidadRestanteParaPracticas = lab.getCapacidad() - capacidadOcupadaPorPracticasLibres;
        
        if (capacidadRestanteParaPracticas < totalEstudiantes) {
            throw new RuntimeException(String.format(
                "Capacidad insuficiente para práctica libre. " +
                "Disponible para prácticas libres: %d, Solicitado: %d",
                capacidadRestanteParaPracticas, totalEstudiantes
            ));
        }
    }

    // 8️⃣ GUARDAR RESERVA PRIMERO (sin invitados temporalmente)
    reserva.setEstado("Pendiente");
    reserva.setEsRecurrente(false);
    
    // Guardar la reserva sin los invitados primero para obtener el ID
    Reserva nuevaReserva = reservaRepo.save(reserva);

    // 9️⃣ GUARDAR INVITADOS DESPUÉS de que la reserva tenga ID
    if (reserva.getInvitados() != null && !reserva.getInvitados().isEmpty()) {
        for (ReservaInvitado invitado : reserva.getInvitados()) {
            invitado.setReserva(nuevaReserva); // Establecer la relación
            invitadoRepo.save(invitado);
        }
        // Actualizar la reserva con los invitados
        nuevaReserva.setInvitados(reserva.getInvitados());
    }

    return nuevaReserva;
}

    // ========== RESERVAS RECURRENTES (MÁXIMA PRIORIDAD) ==========
    @Transactional
    public List<Reserva> crearReservasRecurrentes(RecurrenceRequest request) {
        // 1. Validar que sea para curso (único tipo permitido para recurrentes)
        if (!"clase".equals(request.getTipoReserva())) {
            throw new RuntimeException("Las reservas recurrentes solo están permitidas para tipo 'clase'");
        }

        // 2. Cargar usuario (profesor) y laboratorio
        Usuario profesor = usuarioRepo.findById(request.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));

        Laboratorio lab = laboratorioRepo.findById(request.getIdLaboratorio())
                .orElseThrow(() -> new RuntimeException("Laboratorio no encontrado"));

        // 3. Validar curso
        if (request.getNrcCurso() == null || request.getNrcCurso().trim().isEmpty()) {
            throw new RuntimeException("El NRC del curso es obligatorio para reservas recurrentes");
        }

        // 4. Calcular fecha fin y generar fechas
        LocalDate fechaFin = request.getFechaInicio().plusWeeks(request.getCantidadSemanas());
        List<LocalDate> fechas = generarFechasRecurrentes(
                request.getFechaInicio(),
                fechaFin,
                request.getDiasSemana()
        );

        // 5. Grupo único para todas las reservas recurrentes
        UUID grupoRecurrencia = UUID.randomUUID();

        // 6. Crear reservas recurrentes
        List<Reserva> reservasCreadas = new ArrayList<>();

        for (LocalDate fecha : fechas) {
            LocalDateTime inicioReserva = fecha.atTime(request.getHoraInicio());
            LocalDateTime finReserva = fecha.atTime(request.getHoraFin());

            // 🔥 LAS RESERVAS RECURRENTES TIENEN PRIORIDAD MÁXIMA
            // No necesitan validar capacidad ni conflictos - SON LAS DUEÑAS DEL HORARIO
            // 7. Crear reserva recurrente
            Reserva reserva = new Reserva();
            reserva.setFechaInicio(inicioReserva);
            reserva.setFechaFin(finReserva);
            reserva.setEstado("Confirmada"); // Las recurrentes se aprueban automáticamente
            reserva.setTipoReserva("clase");
            reserva.setUsuario(profesor);
            reserva.setLaboratorio(lab);
            reserva.setNrcCurso(request.getNrcCurso());
            reserva.setCantidadEstudiantes(request.getCantidadEstudiantes());
            reserva.setEsRecurrente(true);
            reserva.setGrupoRecurrencia(grupoRecurrencia);

            Reserva reservaCreada = reservaRepo.save(reserva);
            reservasCreadas.add(reservaCreada);

            // 8. 🔥 RECHAZAR AUTOMÁTICAMENTE CUALQUIER RESERVA EN ESTE HORARIO
            rechazarReservasEnHorario(lab.getId(), inicioReserva, finReserva);
        }

        return reservasCreadas;
    }

    // ========== MÉTODOS DE APROBACIÓN/RECHAZO ==========
    @Transactional
    public Optional<Reserva> aprobar(Integer id) {
        return reservaRepo.findById(id).map(reserva -> {
            // 🔥 Validar que no haya reservas recurrentes de curso en el horario
            boolean existeRecurrente = existeReservaRecurrenteCursoEnHorario(
                    reserva.getLaboratorio().getId(),
                    reserva.getFechaInicio(),
                    reserva.getFechaFin()
            );

            if (existeRecurrente) {
                throw new RuntimeException("No se puede aprobar: Existe una reserva recurrente de curso con prioridad máxima en este horario");
            }

            // Si se aprueba una reserva de CLASE, rechazar prácticas libres en el horario
            if ("clase".equals(reserva.getTipoReserva())) {
                rechazarPracticasLibresEnHorario(
                        reserva.getLaboratorio().getId(),
                        reserva.getFechaInicio(),
                        reserva.getFechaFin()
                );
            }

            // Si se aprueba una práctica libre, verificar que no haya clases
            if ("practica_libre".equals(reserva.getTipoReserva())) {
                boolean existeClase = existeReservaClaseEnHorario(
                        reserva.getLaboratorio().getId(),
                        reserva.getFechaInicio(),
                        reserva.getFechaFin()
                );

                if (existeClase) {
                    throw new RuntimeException("No se puede aprobar: Existe una reserva de clase en este horario");
                }
            }

            reserva.setEstado("Confirmada");
            return reservaRepo.save(reserva);
        });
    }

    @Transactional
    public Optional<Reserva> rechazar(Integer id) {
        return reservaRepo.findById(id).map(reserva -> {
            reserva.setEstado("Rechazada");
            return reservaRepo.save(reserva);
        });
    }

    @Transactional
    public Optional<Reserva> cancelar(Integer id) {
        return reservaRepo.findById(id).map(reserva -> {
            reserva.setEstado("Cancelada");
            return reservaRepo.save(reserva);
        });
    }

    // ========== ACTIVAR Y COMPLETAR ==========
    @Transactional
    public Optional<Reserva> activar(Integer id) {
        return reservaRepo.findById(id).map(reserva -> {
            if (!"Confirmada".equals(reserva.getEstado())) {
                throw new RuntimeException("Solo se pueden activar reservas confirmadas");
            }

            LocalDateTime ahora = LocalDateTime.now();
            if (ahora.isBefore(reserva.getFechaInicio()) || ahora.isAfter(reserva.getFechaFin())) {
                throw new RuntimeException("Solo se puede activar la reserva durante su horario establecido");
            }

            Laboratorio lab = reserva.getLaboratorio();
            if (lab.getCapacidadDisponible() < reserva.getCantidadEstudiantes()) {
                throw new RuntimeException(String.format(
                        "Capacidad insuficiente para activar la reserva. Disponible: %d, Requerido: %d",
                        lab.getCapacidadDisponible(), reserva.getCantidadEstudiantes()
                ));
            }

            // Actualizar capacidad disponible
            lab.setCapacidadDisponible(lab.getCapacidadDisponible() - reserva.getCantidadEstudiantes());
            laboratorioRepo.save(lab);

            reserva.setEstado("Activa");
            return reservaRepo.save(reserva);
        });
    }

    @Transactional
    public Optional<Reserva> completar(Integer id) {
        return reservaRepo.findById(id).map(reserva -> {
            if (!"Activa".equals(reserva.getEstado())) {
                throw new RuntimeException("Solo se pueden completar reservas activas");
            }

            Laboratorio lab = reserva.getLaboratorio();
            lab.setCapacidadDisponible(lab.getCapacidadDisponible() + reserva.getCantidadEstudiantes());

            // Validar que no se exceda la capacidad máxima
            if (lab.getCapacidadDisponible() > lab.getCapacidad()) {
                lab.setCapacidadDisponible(lab.getCapacidad());
            }

            laboratorioRepo.save(lab);
            reserva.setEstado("Completada");
            return reservaRepo.save(reserva);
        });
    }

    // ========== MÉTODOS AUXILIARES PRIVADOS ==========
    /**
     * Verifica si existe reserva RECURRENTE de CURSO en el horario (MÁXIMA
     * PRIORIDAD)
     */
    private boolean existeReservaRecurrenteCursoEnHorario(Integer idLaboratorio, LocalDateTime inicio, LocalDateTime fin) {
        List<Reserva> reservasRecurrentes = reservaRepo.findAll().stream()
                .filter(r -> r.getLaboratorio().getId().equals(idLaboratorio))
                .filter(r -> Boolean.TRUE.equals(r.getEsRecurrente()))
                .filter(r -> "clase".equals(r.getTipoReserva()))
                .filter(r -> !"Cancelada".equals(r.getEstado()) && !"Rechazada".equals(r.getEstado()))
                .filter(r -> haySolapamiento(inicio, fin, r.getFechaInicio(), r.getFechaFin()))
                .toList();

        return !reservasRecurrentes.isEmpty();
    }

    /**
     * Verifica si existe reserva de CLASE regular (no recurrente) en el horario
     */
    private boolean existeReservaClaseEnHorario(Integer idLaboratorio, LocalDateTime inicio, LocalDateTime fin) {
        List<Reserva> reservasClase = reservaRepo.findAll().stream()
                .filter(r -> r.getLaboratorio().getId().equals(idLaboratorio))
                .filter(r -> "clase".equals(r.getTipoReserva()))
                .filter(r -> !Boolean.TRUE.equals(r.getEsRecurrente())) // Excluir recurrentes
                .filter(r -> !"Cancelada".equals(r.getEstado()) && !"Rechazada".equals(r.getEstado()))
                .filter(r -> haySolapamiento(inicio, fin, r.getFechaInicio(), r.getFechaFin()))
                .toList();

        return !reservasClase.isEmpty();
    }

    /**
     * Rechaza AUTOMÁTICAMENTE cualquier reserva en el horario (para reservas
     * recurrentes)
     */
    private void rechazarReservasEnHorario(Integer idLaboratorio, LocalDateTime inicio, LocalDateTime fin) {
        List<Reserva> reservasEnHorario = reservaRepo.findAll().stream()
                .filter(r -> r.getLaboratorio().getId().equals(idLaboratorio))
                .filter(r -> !Boolean.TRUE.equals(r.getEsRecurrente())) // No rechazar otras recurrentes
                .filter(r -> "Pendiente".equals(r.getEstado()) || "Confirmada".equals(r.getEstado()))
                .filter(r -> haySolapamiento(inicio, fin, r.getFechaInicio(), r.getFechaFin()))
                .toList();

        for (Reserva reserva : reservasEnHorario) {
            reserva.setEstado("Rechazada");
            reservaRepo.save(reserva);
        }
    }

    /**
     * Rechaza prácticas libres cuando se aprueba una clase
     */
    private void rechazarPracticasLibresEnHorario(Integer idLaboratorio, LocalDateTime inicio, LocalDateTime fin) {
        List<Reserva> practicasLibres = reservaRepo.findAll().stream()
                .filter(r -> r.getLaboratorio().getId().equals(idLaboratorio))
                .filter(r -> "practica_libre".equals(r.getTipoReserva()))
                .filter(r -> "Pendiente".equals(r.getEstado()) || "Confirmada".equals(r.getEstado()))
                .filter(r -> haySolapamiento(inicio, fin, r.getFechaInicio(), r.getFechaFin()))
                .toList();

        for (Reserva practica : practicasLibres) {
            practica.setEstado("Rechazada");
            reservaRepo.save(practica);
        }
    }

    /**
     * Calcula capacidad ocupada por TODAS las reservas en un horario
     */
    private int calcularCapacidadOcupadaEnHorario(Integer idLaboratorio, LocalDateTime inicio, LocalDateTime fin) {
        List<Reserva> reservasEnHorario = reservaRepo.findAll().stream()
                .filter(r -> r.getLaboratorio().getId().equals(idLaboratorio))
                .filter(r -> !"Cancelada".equals(r.getEstado()) && !"Rechazada".equals(r.getEstado()))
                .filter(r -> haySolapamiento(inicio, fin, r.getFechaInicio(), r.getFechaFin()))
                .toList();

        return reservasEnHorario.stream()
                .mapToInt(Reserva::getCantidadEstudiantes)
                .sum();
    }

    /**
     * Calcula capacidad ocupada solo por prácticas libres APROBADAS
     */
    private int calcularCapacidadPracticasLibresAprobadas(Integer idLaboratorio, LocalDateTime inicio, LocalDateTime fin) {
        List<Reserva> practicasAprobadas = reservaRepo.findAll().stream()
                .filter(r -> r.getLaboratorio().getId().equals(idLaboratorio))
                .filter(r -> "practica_libre".equals(r.getTipoReserva()))
                .filter(r -> "Confirmada".equals(r.getEstado()))
                .filter(r -> haySolapamiento(inicio, fin, r.getFechaInicio(), r.getFechaFin()))
                .toList();

        return practicasAprobadas.stream()
                .mapToInt(Reserva::getCantidadEstudiantes)
                .sum();
    }

    /**
     * Genera lista de fechas para reservas recurrentes
     */
    private List<LocalDate> generarFechasRecurrentes(LocalDate inicio, LocalDate fin, List<Integer> diasSemana) {
        List<LocalDate> fechas = new ArrayList<>();
        LocalDate fechaActual = inicio;

        while (!fechaActual.isAfter(fin)) {
            if (diasSemana.contains(fechaActual.getDayOfWeek().getValue())) {
                fechas.add(fechaActual);
            }
            fechaActual = fechaActual.plusDays(1);
        }

        return fechas;
    }

    /**
     * Verifica solapamiento de horarios
     */
    private boolean haySolapamiento(LocalDateTime inicio1, LocalDateTime fin1,
            LocalDateTime inicio2, LocalDateTime fin2) {
        return (inicio1.isBefore(fin2) && fin1.isAfter(inicio2));
    }

    /**
     * Guarda invitados de una reserva
     */
    private void guardarInvitados(Reserva reserva, List<ReservaInvitado> invitados) {
        if (invitados != null) {
            invitados.forEach(inv -> {
                inv.setReserva(reserva);
                invitadoRepo.save(inv);
            });
        }
    }
}
