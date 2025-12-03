package edu.upc.labpilot.service;

import edu.upc.labpilot.model.Usuario;
import edu.upc.labpilot.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ======= MÉTODOS BÁSICOS CRUD =======
    public List<Usuario> getAll() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> getById(Integer id) {
        return usuarioRepository.findById(id);
    }

    public Usuario save(Usuario usuario) {
        if (usuario.getPassword() != null
                && !usuario.getPassword().isEmpty()
                && !usuario.getPassword().startsWith("$2a$")) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    public void delete(Integer id) {
        usuarioRepository.deleteById(id);
    }

    // ======= MÉTODOS DE BÚSQUEDA =======
    public Optional<Usuario> findByCorreo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo);
        return Optional.ofNullable(usuario);
    }

    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    public boolean existsByCorreo(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }

    // ======= MÉTODOS DE GESTIÓN DE ESTADO =======
    public Usuario cambiarEstado(Integer id, boolean activo) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        u.setActivo(activo);
        return usuarioRepository.save(u);
    }

    public Usuario activarUsuario(Integer id) {
        return cambiarEstado(id, true);
    }

    public Usuario desactivarUsuario(Integer id) {
        return cambiarEstado(id, false);
    }

    // ======= MÉTODOS DE REGISTRO Y CREACIÓN =======
    @Transactional
    public Usuario crearUsuario(Usuario usuario) {
        // Validar que el correo no exista
        if (existsByCorreo(usuario.getCorreo())) {
            throw new RuntimeException("Ya existe un usuario con ese correo");
        }

        // Establecer valores por defecto
        if (usuario.getFechaCreacion() == null) {
            usuario.setFechaCreacion(LocalDateTime.now());
        }

        if (usuario.getActivo() == null) {
            usuario.setActivo(true);
        }

        // Para usuarios de formularios públicos, no requieren contraseña
        if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
            usuario.setPassword(""); // Sin contraseña para usuarios públicos
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario buscarOCrearUsuario(String nombreCompleto, String correo, String documento, String programa) {
        // Intentar buscar por correo
        Usuario usuario = buscarPorCorreo(correo);

        if (usuario != null) {
            return usuario;
        }

        // Si no existe, crear nuevo
        String[] partes = nombreCompleto.trim().split("\\s+", 2);
        String nombre = partes[0];
        String apellido = partes.length > 1 ? partes[1] : "";

        usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setCorreo(correo);
        usuario.setDocumento(documento);
        usuario.setPrograma(programa);
        usuario.setRol("estudiante");
        usuario.setPassword(""); // Sin contraseña para usuarios públicos
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());

        return crearUsuario(usuario);
    }

    // ======= MÉTODOS DE VERIFICACIÓN Y MFA =======
    /**
     * Registrar un nuevo estudiante con verificación por correo
     */
    @Transactional
    public Usuario registrarEstudiante(Usuario usuario, String password) {
        if (existsByCorreo(usuario.getCorreo())) {
            throw new RuntimeException("Ya existe un usuario con ese correo");
        }

        // Configurar usuario estudiante
        usuario.setRol("estudiante");
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setActivo(false); // No activo hasta verificación
        usuario.setCorreoVerificado(false);
        usuario.setTokenVerificacion(generarTokenVerificacion());
        usuario.setFechaCreacion(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    /**
     * Verificar correo de usuario usando token
     */
    @Transactional
    public Usuario verificarCorreo(String token) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByTokenVerificacion(token);

        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Token de verificación inválido");
        }

        Usuario usuario = usuarioOpt.get();
        usuario.marcarCorreoVerificado();
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    /**
     * Buscar usuario por token de verificación
     */
    public Optional<Usuario> findByTokenVerificacion(String token) {
        return usuarioRepository.findByTokenVerificacion(token);
    }

    /**
     * Generar token de verificación único
     */
    private String generarTokenVerificacion() {
        return UUID.randomUUID().toString();
    }

    // ======= MÉTODOS DE MFA =======
    /**
     * Configurar MFA para un usuario
     */
    @Transactional
    public Usuario configurarMFA(Integer idUsuario, String secret) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.habilitarMFA(secret);
        return usuarioRepository.save(usuario);
    }

    /**
     * Deshabilitar MFA para un usuario
     */
    @Transactional
    public Usuario deshabilitarMFA(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.deshabilitarMFA();
        return usuarioRepository.save(usuario);
    }

    /**
     * Verificar si un usuario tiene MFA habilitado
     */
    public boolean tieneMFAHabilitado(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .map(Usuario::getMfaHabilitado)
                .orElse(false);
    }

    // ======= MÉTODOS DE ACTUALIZACIÓN =======
    /**
     * Actualizar perfil de usuario
     */
    @Transactional
    public Usuario actualizarPerfil(Integer id, Usuario datosActualizados) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Actualizar campos permitidos
        if (datosActualizados.getNombre() != null) {
            usuarioExistente.setNombre(datosActualizados.getNombre());
        }
        if (datosActualizados.getApellido() != null) {
            usuarioExistente.setApellido(datosActualizados.getApellido());
        }
        if (datosActualizados.getTelefono() != null) {
            usuarioExistente.setTelefono(datosActualizados.getTelefono());
        }
        if (datosActualizados.getPrograma() != null) {
            usuarioExistente.setPrograma(datosActualizados.getPrograma());
        }
        if (datosActualizados.getDocumento() != null) {
            usuarioExistente.setDocumento(datosActualizados.getDocumento());
        }

        return usuarioRepository.save(usuarioExistente);
    }

    /**
     * Cambiar contraseña de usuario
     */
    @Transactional
    public Usuario cambiarPassword(Integer id, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        return usuarioRepository.save(usuario);
    }

    /**
     * Verificar contraseña actual
     */
    public boolean verificarPassword(Integer id, String passwordActual) {
        return usuarioRepository.findById(id)
                .map(usuario -> passwordEncoder.matches(passwordActual, usuario.getPassword()))
                .orElse(false);
    }

    // ======= MÉTODOS DE CONSULTA ESPECÍFICOS =======
    /**
     * Buscar usuarios por rol
     */
    public List<Usuario> findByRol(String rol) {
        return usuarioRepository.findByRol(rol);
    }

    /**
     * Buscar usuarios activos
     */
    public List<Usuario> findActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    /**
     * Buscar usuarios inactivos
     */
    public List<Usuario> findInactivos() {
        return usuarioRepository.findByActivoFalse();
    }

    /**
     * Contar usuarios por rol
     */
    public long countByRol(String rol) {
        return usuarioRepository.countByRol(rol);
    }

    /**
     * Verificar si un usuario puede iniciar sesión
     */
    public boolean puedeIniciarSesion(String correo) {
        return findByCorreo(correo)
                .map(Usuario::puedeIniciarSesion)
                .orElse(false);
    }

    // ======= MÉTODOS DE ADMINISTRACIÓN =======
    /**
     * Crear usuario administrador
     */
    @Transactional
    public Usuario crearAdministrador(Usuario usuario, String password) {
        if (existsByCorreo(usuario.getCorreo())) {
            throw new RuntimeException("Ya existe un usuario con ese correo");
        }

        usuario.setRol("administrador");
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setActivo(true);
        usuario.setCorreoVerificado(true);
        usuario.setFechaCreacion(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    /**
     * Promover usuario a administrador
     */
    @Transactional
    public Usuario promoverAAdministrador(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setRol("administrador");
        return usuarioRepository.save(usuario);
    }

    /**
     * Degradar administrador a estudiante
     */
    @Transactional
    public Usuario degradarAEstudiante(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar que no sea el último superadmin
        if (usuario.esSuperAdmin()) {
            long countSuperAdmins = usuarioRepository.countByRol("superadmin");
            if (countSuperAdmins <= 1) {
                throw new RuntimeException("No se puede degradar al último superadmin");
            }
        }

        usuario.setRol("estudiante");
        return usuarioRepository.save(usuario);
    }
}
