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
    
    //@Autowired
    private UsuarioService usuarioService;

     @Autowired
    private EmailService email;

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

    // ========== MÉTODO SOLICITAR RESERVA INDIVIDUAL (ARREGLADO) ==========
    @Transactional
    public Reserva solicitar(Reserva reserva) {
        System.out.println("=== INICIANDO SOLICITUD DE RESERVA ===");
        System.out.println("Datos recibidos: " + reserva);
        
        // 1️⃣ Validar y cargar usuario
        Usuario usuario = usuarioRepo.findById(reserva.getUsuario().getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + reserva.getUsuario().getId()));
        reserva.setUsuario(usuario);
        System.out.println("Usuario cargado: " + usuario.getId() + " - " + usuario.getNombre());

        // 2️⃣ Validar y cargar laboratorio
        Laboratorio lab = laboratorioRepo.findById(reserva.getLaboratorio().getId())
                .orElseThrow(() -> new RuntimeException("Laboratorio no encontrado con ID: " + reserva.getLaboratorio().getId()));
        reserva.setLaboratorio(lab);
        System.out.println("Laboratorio cargado: " + lab.getId() + " - " + lab.getNombre());

        // 3️⃣ Validar tipo de reserva
        if (!"clase".equals(reserva.getTipoReserva()) && !"practica_libre".equals(reserva.getTipoReserva())) {
            throw new RuntimeException("Tipo de reserva no válido. Debe ser 'clase' o 'practica_libre'");
        }

        // 4️⃣ Calcular cantidad de estudiantes
        int totalEstudiantes = 1; // el usuario que reserva
        if (reserva.getInvitados() != null && !reserva.getInvitados().isEmpty()) {
            totalEstudiantes += reserva.getInvitados().size();
            System.out.println("Total de invitados: " + reserva.getInvitados().size());
        }
        reserva.setCantidadEstudiantes(totalEstudiantes);
        System.out.println("Total estudiantes: " + totalEstudiantes);

        // 5️⃣ VERIFICAR PRIORIDAD MÁXIMA: RESERVAS RECURRENTES DE CURSO
        boolean existeRecurrenteCursoEnHorario = existeReservaRecurrenteCursoEnHorario(
            lab.getId(), 
            reserva.getFechaInicio(), 
            reserva.getFechaFin()
        );

        if (existeRecurrenteCursoEnHorario) {
            throw new RuntimeException("Reserva rechazada: Existe una reserva recurrente de curso con prioridad máxima en este horario");
        }

        // 6️⃣ VERIFICAR RESERVAS DE CLASE REGULARES
        boolean existeClaseEnHorario = existeReservaClaseEnHorario(
            lab.getId(), 
            reserva.getFechaInicio(), 
            reserva.getFechaFin()
        );

        if ("practica_libre".equals(reserva.getTipoReserva()) && existeClaseEnHorario) {
            throw new RuntimeException("Reserva rechazada: Existe una reserva de clase en este horario");
        }

        // 7️⃣ CALCULAR CAPACIDAD OCUPADA EN LA FRANJA HORARIA
        int capacidadOcupada = calcularCapacidadOcupadaEnHorario(
            lab.getId(),
            reserva.getFechaInicio(), 
            reserva.getFechaFin()
        );

        int capacidadDisponibleEnHorario = lab.getCapacidad() - capacidadOcupada;

        // 8️⃣ VALIDAR CAPACIDAD
        if (capacidadDisponibleEnHorario < totalEstudiantes) {
            throw new RuntimeException(String.format(
                "Capacidad insuficiente para el horario seleccionado. " +
                "Capacidad total: %d, Ocupado: %d, Disponible: %d, Solicitado: %d",
                lab.getCapacidad(), capacidadOcupada, capacidadDisponibleEnHorario, totalEstudiantes
            ));
        }

        // 9️⃣ PARA PRÁCTICA LIBRE: Verificar capacidad con otras prácticas libres
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

        // 🔟 GUARDAR RESERVA PRIMERO (sin invitados temporalmente)
        reserva.setEstado("Pendiente");
        reserva.setEsRecurrente(false);
        
        // ✅ IMPORTANTE: Guardar los invitados temporalmente
        List<ReservaInvitado> invitadosTemp = reserva.getInvitados();
        reserva.setInvitados(null); // Limpiar temporalmente
        
        // Guardar la reserva para obtener el ID
        Reserva nuevaReserva = reservaRepo.save(reserva);
        System.out.println("Reserva guardada con ID: " + nuevaReserva.getId());

        // 1️⃣1️⃣ GUARDAR INVITADOS DESPUÉS de que la reserva tenga ID
        if (invitadosTemp != null && !invitadosTemp.isEmpty()) {
            System.out.println("Guardando " + invitadosTemp.size() + " invitados...");
            List<ReservaInvitado> invitadosGuardados = new ArrayList<>();
            
            for (ReservaInvitado invitado : invitadosTemp) {
                invitado.setReserva(nuevaReserva); // Establecer la relación
                ReservaInvitado invitadoGuardado = invitadoRepo.save(invitado);
                invitadosGuardados.add(invitadoGuardado);
                System.out.println("Invitado guardado: " + invitadoGuardado.getNombre() + " " + invitadoGuardado.getApellido());
            }
            
            // Actualizar la reserva con los invitados guardados
            nuevaReserva.setInvitados(invitadosGuardados);
        }

        System.out.println("=== RESERVA CREADA EXITOSAMENTE ===");
        return nuevaReserva;
    }

    // ========== RESTO DE MÉTODOS SIN CAMBIOS ==========
    
    @Transactional
    public List<Reserva> crearReservasRecurrentes(RecurrenceRequest request) {
        if (!"clase".equals(request.getTipoReserva())) {
            throw new RuntimeException("Las reservas recurrentes solo están permitidas para tipo 'clase'");
        }

        Usuario profesor = usuarioRepo.findById(request.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));

        Laboratorio lab = laboratorioRepo.findById(request.getIdLaboratorio())
                .orElseThrow(() -> new RuntimeException("Laboratorio no encontrado"));

        if (request.getNrcCurso() == null || request.getNrcCurso().trim().isEmpty()) {
            throw new RuntimeException("El NRC del curso es obligatorio para reservas recurrentes");
        }

        LocalDate fechaFin = request.getFechaInicio().plusWeeks(request.getCantidadSemanas());
        List<LocalDate> fechas = generarFechasRecurrentes(
                request.getFechaInicio(),
                fechaFin,
                request.getDiasSemana()
        );

        UUID grupoRecurrencia = UUID.randomUUID();
        List<Reserva> reservasCreadas = new ArrayList<>();

        for (LocalDate fecha : fechas) {
            LocalDateTime inicioReserva = fecha.atTime(request.getHoraInicio());
            LocalDateTime finReserva = fecha.atTime(request.getHoraFin());

            Reserva reserva = new Reserva();
            reserva.setFechaInicio(inicioReserva);
            reserva.setFechaFin(finReserva);
            reserva.setEstado("Confirmada");
            reserva.setTipoReserva("clase");
            reserva.setUsuario(profesor);
            reserva.setLaboratorio(lab);
            reserva.setNrcCurso(request.getNrcCurso());
            reserva.setCantidadEstudiantes(request.getCantidadEstudiantes());
            reserva.setEsRecurrente(true);
            reserva.setGrupoRecurrencia(grupoRecurrencia);

            Reserva reservaCreada = reservaRepo.save(reserva);
            reservasCreadas.add(reservaCreada);

            rechazarReservasEnHorario(lab.getId(), inicioReserva, finReserva);
        }

        return reservasCreadas;
    }

    @Transactional
public Optional<Reserva> aprobar(Integer id) {
    return reservaRepo.findById(id).map(reserva -> {

        // Validaciones existentes
        boolean existeRecurrente = existeReservaRecurrenteCursoEnHorario(
                reserva.getLaboratorio().getId(),
                reserva.getFechaInicio(),
                reserva.getFechaFin()
        );

        if (existeRecurrente) {
            throw new RuntimeException("No se puede aprobar: Existe una reserva recurrente de curso con prioridad máxima en este horario");
        }

        if ("clase".equals(reserva.getTipoReserva())) {
            rechazarPracticasLibresEnHorario(
                    reserva.getLaboratorio().getId(),
                    reserva.getFechaInicio(),
                    reserva.getFechaFin()
            );
        }

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
        Reserva guardada = reservaRepo.save(reserva);

        // 🔥 ENVIAR CORREO AQUÍ
        email.notificarReservaAprobada(
                reserva.getUsuario().getCorreo(),
                reserva.getUsuario().getNombre(),
                reserva.getLaboratorio().getNombre(),
                reserva.getFechaInicio().toString()
        );

        return guardada;
    });
}


    @Transactional
public Optional<Reserva> rechazar(Integer id) {
    return reservaRepo.findById(id).map(reserva -> {

        reserva.setEstado("Rechazada");
        Reserva guardada = reservaRepo.save(reserva);

        // 🔥 ENVIAR CORREO AQUÍ
        email.notificarReservaRechazada(
                reserva.getUsuario().getCorreo(),
                reserva.getUsuario().getNombre()
        );

        return guardada;
    });
}


    @Transactional
    public Optional<Reserva> cancelar(Integer id) {
        return reservaRepo.findById(id).map(reserva -> {
            reserva.setEstado("Cancelada");
            return reservaRepo.save(reserva);
        });
    }

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

            if (lab.getCapacidadDisponible() > lab.getCapacidad()) {
                lab.setCapacidadDisponible(lab.getCapacidad());
            }

            laboratorioRepo.save(lab);
            reserva.setEstado("Completada");
            return reservaRepo.save(reserva);
        });
    }

    // ========== MÉTODOS AUXILIARES ==========
    
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

    private boolean existeReservaClaseEnHorario(Integer idLaboratorio, LocalDateTime inicio, LocalDateTime fin) {
        List<Reserva> reservasClase = reservaRepo.findAll().stream()
                .filter(r -> r.getLaboratorio().getId().equals(idLaboratorio))
                .filter(r -> "clase".equals(r.getTipoReserva()))
                .filter(r -> !Boolean.TRUE.equals(r.getEsRecurrente()))
                .filter(r -> !"Cancelada".equals(r.getEstado()) && !"Rechazada".equals(r.getEstado()))
                .filter(r -> haySolapamiento(inicio, fin, r.getFechaInicio(), r.getFechaFin()))
                .toList();

        return !reservasClase.isEmpty();
    }

    private void rechazarReservasEnHorario(Integer idLaboratorio, LocalDateTime inicio, LocalDateTime fin) {
        List<Reserva> reservasEnHorario = reservaRepo.findAll().stream()
                .filter(r -> r.getLaboratorio().getId().equals(idLaboratorio))
                .filter(r -> !Boolean.TRUE.equals(r.getEsRecurrente()))
                .filter(r -> "Pendiente".equals(r.getEstado()) || "Confirmada".equals(r.getEstado()))
                .filter(r -> haySolapamiento(inicio, fin, r.getFechaInicio(), r.getFechaFin()))
                .toList();

        for (Reserva reserva : reservasEnHorario) {
            reserva.setEstado("Rechazada");
            reservaRepo.save(reserva);
        }
    }

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

    private boolean haySolapamiento(LocalDateTime inicio1, LocalDateTime fin1,
            LocalDateTime inicio2, LocalDateTime fin2) {
        return (inicio1.isBefore(fin2) && fin1.isAfter(inicio2));
    }
}
