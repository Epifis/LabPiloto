package edu.upc.labpilot.service;

import edu.upc.labpilot.model.Reserva;
import edu.upc.labpilot.model.Laboratorio;
import edu.upc.labpilot.repository.ReservaRepository;
import edu.upc.labpilot.repository.LaboratorioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservaAutoCompletarService {
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private LaboratorioRepository laboratorioRepository;
    
    @Scheduled(fixedRate = 300000) // Ejecutar cada 5 minutos (300,000 ms)
    @Transactional
    public void completarReservasVencidas() {
        LocalDateTime ahora = LocalDateTime.now();
        
        List<Reserva> reservasVencidas = reservaRepository.findAll().stream()
                .filter(r -> "Activa".equals(r.getEstado()))
                .filter(r -> r.getFechaFin().isBefore(ahora))
                .toList();
        
        for (Reserva reserva : reservasVencidas) {
            try {
                Laboratorio lab = reserva.getLaboratorio();
                
                // Devolver capacidad al laboratorio
                int nuevaCapacidad = lab.getCapacidadDisponible() + reserva.getCantidadEstudiantes();
                lab.setCapacidadDisponible(Math.min(nuevaCapacidad, lab.getCapacidad()));
                
                laboratorioRepository.save(lab);
                reserva.setEstado("Completada");
                reservaRepository.save(reserva);
                
                System.out.println("✅ Reserva " + reserva.getId() + " completada automáticamente");
            } catch (Exception e) {
                System.err.println("❌ Error completando reserva " + reserva.getId() + ": " + e.getMessage());
            }
        }
    }
}
