package edu.upc.labpilot.service;

import edu.upc.labpilot.model.AceptacionTerminos;
import edu.upc.labpilot.model.ContratoUso;
import edu.upc.labpilot.model.Usuario;
import edu.upc.labpilot.repository.AceptacionTerminosRepository;
import edu.upc.labpilot.repository.ContratoUsoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ContratoService {

    @Autowired
    private ContratoUsoRepository contratoUsoRepository;

    @Autowired
    private AceptacionTerminosRepository aceptacionTerminosRepository;

    /**
     * Obtener el contrato vigente (SOLO uno activo)
     */
    public Optional<ContratoUso> obtenerContratoVigente() {
        Optional<ContratoUso> contrato = contratoUsoRepository.findByActivoTrue();
        if (contrato.isPresent()) {
            System.out.println("✅ Contrato vigente encontrado: " + contrato.get().getVersion());
        } else {
            System.err.println("❌ No hay contrato activo en el sistema");
        }
        return contrato;
    }

    /**
     * Obtener contrato por tipo
     */
    public Optional<ContratoUso> obtenerContratoPorTipo(String tipoContrato) {
        return contratoUsoRepository.findByTipoContratoAndActivoTrue(tipoContrato);
    }

    /**
     * Obtener contrato para estudiantes
     */
    public Optional<ContratoUso> obtenerContratoEstudiante() {
        // Los estudiantes usan el contrato general (no hay específico para estudiantes)
        return obtenerContratoVigente();
    }

    /**
     * Obtener contrato para administradores
     */
    public Optional<ContratoUso> obtenerContratoAdmin() {
        Optional<ContratoUso> contratoAdmin = obtenerContratoPorTipo("ADMIN");
        if (contratoAdmin.isPresent()) {
            return contratoAdmin;
        }
        // Fallback al contrato general si no hay específico para admin
        return obtenerContratoVigente();
    }

    /**
     * Registrar aceptación de términos
     */
    @Transactional
    public boolean registrarAceptacion(Usuario usuario, String ipAddress, 
                                     String userAgent, String firmaDigital) {
        try {
            System.out.println("🔍 Iniciando registro de aceptación para: " + usuario.getCorreo());
            
            Optional<ContratoUso> contratoOpt = obtenerContratoVigente();
            if (contratoOpt.isEmpty()) {
                System.err.println("❌ No hay contrato vigente");
                throw new RuntimeException("No hay contrato vigente");
            }

            ContratoUso contrato = contratoOpt.get();
            System.out.println("✅ Contrato encontrado: " + contrato.getVersion() + " - " + contrato.getTipoContrato());

            // Generar hash único de la firma
            String hashFirma = generarHashFirma(usuario, contrato, firmaDigital);
            System.out.println("✅ Hash de firma generado");

            // Verificar si ya existe una aceptación reciente
            Optional<AceptacionTerminos> aceptacionExistente = 
                aceptacionTerminosRepository.findTopByUsuarioIdOrderByFechaAceptacionDesc(usuario.getId());
            
            if (aceptacionExistente.isPresent()) {
                AceptacionTerminos existente = aceptacionExistente.get();
                // Si ya aceptó el mismo contrato recientemente, no crear duplicado
                if (existente.getContrato().getId().equals(contrato.getId()) &&
                    existente.getFechaAceptacion().isAfter(LocalDateTime.now().minusDays(1))) {
                    System.out.println("✅ Ya existe aceptación reciente, no se crea duplicado");
                    return true;
                }
            }

            AceptacionTerminos aceptacion = new AceptacionTerminos();
            aceptacion.setUsuario(usuario);
            aceptacion.setContrato(contrato);
            aceptacion.setIpAddress(ipAddress);
            aceptacion.setUserAgent(userAgent);
            aceptacion.setFechaAceptacion(LocalDateTime.now());
            aceptacion.setHashFirma(hashFirma);

            System.out.println("✅ Guardando aceptación en base de datos...");
            aceptacionTerminosRepository.save(aceptacion);
            System.out.println("✅ Aceptación guardada exitosamente para usuario: " + usuario.getCorreo());

            return true;
        } catch (Exception e) {
            System.err.println("❌ Error en registrarAceptacion: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error registrando aceptación de términos: " + e.getMessage(), e);
        }
    }

    /**
     * Registrar aceptación específica para estudiantes
     */
    @Transactional
    public boolean registrarAceptacionEstudiante(Usuario usuario, String ipAddress, 
                                               String userAgent, String firmaDigital) {
        try {
            System.out.println("🔍 Iniciando registro de aceptación ESTUDIANTE para: " + usuario.getCorreo());
            
            Optional<ContratoUso> contratoOpt = obtenerContratoEstudiante();
            if (contratoOpt.isEmpty()) {
                System.err.println("❌ No hay contrato para estudiantes");
                throw new RuntimeException("No hay contrato vigente para estudiantes");
            }

            ContratoUso contrato = contratoOpt.get();
            System.out.println("✅ Contrato estudiante encontrado: " + contrato.getVersion());

            String hashFirma = generarHashFirma(usuario, contrato, firmaDigital);
            System.out.println("✅ Hash de firma estudiante generado");

            AceptacionTerminos aceptacion = new AceptacionTerminos();
            aceptacion.setUsuario(usuario);
            aceptacion.setContrato(contrato);
            aceptacion.setIpAddress(ipAddress);
            aceptacion.setUserAgent(userAgent);
            aceptacion.setFechaAceptacion(LocalDateTime.now());
            aceptacion.setHashFirma(hashFirma);

            System.out.println("✅ Guardando aceptación estudiante...");
            aceptacionTerminosRepository.save(aceptacion);
            System.out.println("✅ Aceptación estudiante guardada exitosamente");

            return true;
        } catch (Exception e) {
            System.err.println("❌ Error en registrarAceptacionEstudiante: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error registrando aceptación de contrato estudiante: " + e.getMessage(), e);
        }
    }

    /**
     * Registrar aceptación específica para administradores
     */
    @Transactional
    public boolean registrarAceptacionAdmin(Usuario usuario, String ipAddress, 
                                          String userAgent, String firmaDigital) {
        try {
            System.out.println("🔍 Iniciando registro de aceptación ADMIN para: " + usuario.getCorreo());
            
            Optional<ContratoUso> contratoOpt = obtenerContratoAdmin();
            if (contratoOpt.isEmpty()) {
                System.err.println("❌ No hay contrato para administradores");
                throw new RuntimeException("No hay contrato vigente para administradores");
            }

            ContratoUso contrato = contratoOpt.get();
            System.out.println("✅ Contrato admin encontrado: " + contrato.getVersion());

            String hashFirma = generarHashFirma(usuario, contrato, firmaDigital);
            System.out.println("✅ Hash de firma admin generado");

            AceptacionTerminos aceptacion = new AceptacionTerminos();
            aceptacion.setUsuario(usuario);
            aceptacion.setContrato(contrato);
            aceptacion.setIpAddress(ipAddress);
            aceptacion.setUserAgent(userAgent);
            aceptacion.setFechaAceptacion(LocalDateTime.now());
            aceptacion.setHashFirma(hashFirma);

            System.out.println("✅ Guardando aceptación admin...");
            aceptacionTerminosRepository.save(aceptacion);
            System.out.println("✅ Aceptación admin guardada exitosamente");

            return true;
        } catch (Exception e) {
            System.err.println("❌ Error en registrarAceptacionAdmin: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error registrando aceptación de contrato admin: " + e.getMessage(), e);
        }
    }

    /**
     * Generar hash único para la firma
     */
    private String generarHashFirma(Usuario usuario, ContratoUso contrato, String firma) {
        try {
            String data = usuario.getCorreo() + 
                         usuario.getDocumento() + 
                         contrato.getVersion() + 
                         contrato.getId() + 
                         firma + 
                         LocalDateTime.now().toString() +
                         System.currentTimeMillis();
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            System.err.println("❌ Error generando hash de firma: " + e.getMessage());
            throw new RuntimeException("Error generando hash de firma", e);
        }
    }

    /**
     * Verificar si un usuario ha aceptado los términos
     */
    public boolean verificarAceptacion(Integer idUsuario) {
        boolean existe = aceptacionTerminosRepository.existsByUsuarioId(idUsuario);
        System.out.println("🔍 Verificando aceptación para usuario " + idUsuario + ": " + existe);
        return existe;
    }

    /**
     * Verificar si un usuario ha aceptado un contrato específico
     */
    public boolean verificarAceptacionContrato(Integer idUsuario, Integer idContrato) {
        return aceptacionTerminosRepository.existsByUsuarioIdAndContratoId(idUsuario, idContrato);
    }

    /**
     * Obtener la última aceptación de un usuario
     */
    public Optional<AceptacionTerminos> obtenerUltimaAceptacion(Integer idUsuario) {
        return aceptacionTerminosRepository.findTopByUsuarioIdOrderByFechaAceptacionDesc(idUsuario);
    }

    /**
     * Obtener historial de aceptaciones de un usuario
     */
    public List<AceptacionTerminos> obtenerHistorialAceptaciones(Integer idUsuario) {
        return aceptacionTerminosRepository.findByUsuarioIdOrderByFechaAceptacionDesc(idUsuario);
    }

    /**
     * Obtener todas las aceptaciones de un contrato específico
     */
    public List<AceptacionTerminos> obtenerAceptacionesPorContrato(Integer idContrato) {
        return aceptacionTerminosRepository.findByContratoIdOrderByFechaAceptacionDesc(idContrato);
    }

    /**
     * Crear nuevo contrato (para admins)
     */
    @Transactional
    public ContratoUso crearContrato(String version, String contenido, String tipoContrato) {
        try {
            System.out.println("🔧 Creando nuevo contrato: " + version + " - " + tipoContrato);
            
            // Desactivar contratos anteriores del mismo tipo
            Optional<ContratoUso> contratoAnteriorOpt = 
                contratoUsoRepository.findByTipoContratoAndActivoTrue(tipoContrato);
            
            if (contratoAnteriorOpt.isPresent()) {
                ContratoUso contratoAnterior = contratoAnteriorOpt.get();
                contratoAnterior.setActivo(false);
                contratoUsoRepository.save(contratoAnterior);
                System.out.println("✅ Contrato anterior desactivado: " + contratoAnterior.getVersion());
            }

            // Crear nuevo contrato
            ContratoUso nuevoContrato = new ContratoUso();
            nuevoContrato.setVersion(version);
            nuevoContrato.setContenido(contenido);
            nuevoContrato.setTipoContrato(tipoContrato);
            nuevoContrato.setActivo(true);
            nuevoContrato.setFechaCreacion(LocalDateTime.now());
            
            ContratoUso contratoGuardado = contratoUsoRepository.save(nuevoContrato);
            System.out.println("✅ Nuevo contrato creado: " + contratoGuardado.getId());
            
            return contratoGuardado;
        } catch (Exception e) {
            System.err.println("❌ Error creando contrato: " + e.getMessage());
            throw new RuntimeException("Error creando nuevo contrato", e);
        }
    }

    /**
     * Activar contrato específico y desactivar otros
     */
    @Transactional
    public boolean activarContrato(Integer idContrato) {
        try {
            Optional<ContratoUso> contratoOpt = contratoUsoRepository.findById(idContrato);
            if (contratoOpt.isEmpty()) {
                throw new RuntimeException("Contrato no encontrado");
            }

            ContratoUso contrato = contratoOpt.get();
            
            // Desactivar todos los contratos del mismo tipo
            List<ContratoUso> contratosMismoTipo = contratoUsoRepository.findByTipoContrato(contrato.getTipoContrato());
            for (ContratoUso c : contratosMismoTipo) {
                if (!c.getId().equals(idContrato)) {
                    c.setActivo(false);
                    contratoUsoRepository.save(c);
                }
            }

            // Activar el contrato seleccionado
            contrato.setActivo(true);
            contratoUsoRepository.save(contrato);
            
            System.out.println("✅ Contrato activado: " + contrato.getVersion());
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error activando contrato: " + e.getMessage());
            throw new RuntimeException("Error activando contrato", e);
        }
    }

    /**
     * Obtener todos los contratos (para administración)
     */
    public List<ContratoUso> obtenerTodosContratos() {
        return contratoUsoRepository.findAllByOrderByActivoDescFechaCreacionDesc();
    }

    /**
     * Obtener contratos por tipo
     */
    public List<ContratoUso> obtenerContratosPorTipo(String tipoContrato) {
        return contratoUsoRepository.findByTipoContratoOrderByActivoDescFechaCreacionDesc(tipoContrato);
    }

    /**
     * Obtener todas las aceptaciones (para reportes)
     */
    public List<AceptacionTerminos> obtenerTodasAceptaciones() {
        return aceptacionTerminosRepository.findAll();
    }

    /**
     * Obtener estadísticas de aceptaciones
     */
    public String obtenerEstadisticasAceptaciones() {
        long totalAceptaciones = aceptacionTerminosRepository.count();
        long totalUsuarios = aceptacionTerminosRepository.countDistinctUsuarios();
        
        return String.format("Total aceptaciones: %d, Usuarios únicos: %d", totalAceptaciones, totalUsuarios);
    }
}