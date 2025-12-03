package edu.upc.labpilot.service;

import edu.upc.labpilot.config.MfaUtil;
import edu.upc.labpilot.model.MfaCode;
import edu.upc.labpilot.model.Usuario;
import edu.upc.labpilot.repository.MfaCodeRepository;
import edu.upc.labpilot.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class MfaService {

    @Autowired
    private MfaUtil mfaUtil;

    @Autowired
    private MfaCodeRepository mfaCodeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @Value("${mfa.code.expiration:600000}") // 10 minutos por defecto
    private Long mfaExpiration;

    /**
     * ✅ NUEVO: Generar código para verificación de correo (durante registro)
     */
    @Transactional
    public String generarCodigoVerificacion(Usuario usuario) {
        // Invalidar códigos anteriores de verificación
        mfaCodeRepository.invalidarCodigosVerificacionAnteriores(usuario.getId());
        
        String codigo = generarCodigoNumerico();
        LocalDateTime fechaExpiracion = LocalDateTime.now().plusMinutes(15); // 15 min para verificación

        // Crear código de VERIFICACIÓN (tipo diferente)
        MfaCode mfaCode = new MfaCode(usuario, codigo, "verificacion_correo", fechaExpiracion);
        mfaCodeRepository.save(mfaCode);

        return codigo;
    }

    /**
     * ✅ NUEVO: Verificar código de verificación de correo
     */
    @Transactional
    public boolean verificarCodigoVerificacion(Integer idUsuario, String codigo) {
        Optional<MfaCode> mfaCodeOpt = mfaCodeRepository.findByUsuarioIdAndCodigoAndTipo(
            idUsuario, codigo, "verificacion_correo");

        if (mfaCodeOpt.isPresent()) {
            MfaCode mfaCode = mfaCodeOpt.get();

            if (mfaCode.esValido() && !mfaCode.getUtilizado()) {
                // Marcar como utilizado
                mfaCode.marcarComoUtilizado();
                mfaCodeRepository.save(mfaCode);
                return true;
            }
        }
        return false;
    }

    /**
     * Iniciar proceso MFA para login (cuenta ya activa)
     */
    @Transactional
    public String iniciarMfa(Usuario usuario, String tipo) {
        // Invalidar códigos anteriores del usuario
        mfaCodeRepository.invalidarCodigosLoginAnteriores(usuario.getId());

        String codigo;
        if ("sms".equals(tipo) && usuario.getTelefono() != null) {
            codigo = mfaUtil.generarCodigoSMS();
            // Aquí integrarías con un servicio SMS como Twilio
            // Por ahora usamos email como fallback
            tipo = "email";
        } else {
            codigo = mfaUtil.generarCodigoSMS();
            tipo = "email"; // Por defecto email
        }

        // Calcular fecha de expiración
        LocalDateTime fechaExpiracion = LocalDateTime.now().plusMinutes(mfaExpiration / 60000);

        // Crear y guardar código MFA para LOGIN
        MfaCode mfaCode = new MfaCode(usuario, codigo, "login_mfa", fechaExpiracion);
        mfaCodeRepository.save(mfaCode);

        // Enviar código por email
        if ("email".equals(tipo)) {
            emailService.enviarCodigoMFA(usuario.getCorreo(), usuario.getNombre(), codigo);
        }

        return codigo;
    }

    /**
     * Verificar código MFA para login
     */
    @Transactional
    public boolean verificarCodigoMfa(Integer idUsuario, String codigo) {
        Optional<MfaCode> mfaCodeOpt = mfaCodeRepository.findByUsuarioIdAndCodigoAndTipo(
            idUsuario, codigo, "login_mfa");

        if (mfaCodeOpt.isPresent()) {
            MfaCode mfaCode = mfaCodeOpt.get();

            if (mfaCode.esValido() && !mfaCode.getUtilizado()) {
                // Marcar como utilizado
                mfaCode.marcarComoUtilizado();
                mfaCodeRepository.save(mfaCode);
                return true;
            }
        }
        return false;
    }

    /**
     * Verificar código MFA usando app autenticadora (TOTP)
     */
    public boolean verificarCodigoApp(Integer idUsuario, String codigo) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);
        if (usuarioOpt.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOpt.get();
        if (usuario.getMfaSecret() == null || !usuario.getMfaHabilitado()) {
            return false;
        }

        return mfaUtil.verificarCodigo(usuario.getMfaSecret(), codigo);
    }

    /**
     * Configurar MFA con app autenticadora para un usuario
     */
    @Transactional
    public String configurarMfaApp(Integer idUsuario) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            String secret = mfaUtil.generarSecret();
            usuario.habilitarMFA(secret);
            usuarioRepository.save(usuario);
            return secret;
        }
        throw new RuntimeException("Usuario no encontrado");
    }

    /**
     * Deshabilitar MFA para un usuario
     */
    @Transactional
    public void deshabilitarMfa(Integer idUsuario) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.deshabilitarMFA();
            usuarioRepository.save(usuario);

            // Limpiar códigos MFA del usuario
            mfaCodeRepository.invalidarCodigosAnteriores(idUsuario);
        } else {
            throw new RuntimeException("Usuario no encontrado");
        }
    }

    /**
     * Verificar si un usuario tiene MFA habilitado
     */
    public boolean tieneMfaHabilitado(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .map(Usuario::getMfaHabilitado)
                .orElse(false);
    }

    /**
     * Obtener el secret MFA de un usuario
     */
    public String obtenerSecretMfa(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .map(Usuario::getMfaSecret)
                .orElse(null);
    }

    /**
     * Reenviar código MFA para login
     */
    @Transactional
    public boolean reenviarCodigoMfa(Integer idUsuario) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);
        if (usuarioOpt.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOpt.get();

        // Verificar si ya existe un código activo reciente
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime hace5Minutos = ahora.minusMinutes(5);

        Optional<MfaCode> codigoReciente = mfaCodeRepository.findUltimoCodigoLoginByUsuarioId(idUsuario);
        if (codigoReciente.isPresent()
                && codigoReciente.get().getFechaCreacion().isAfter(hace5Minutos)
                && codigoReciente.get().esValido()) {
            // Reenviar el mismo código
            MfaCode codigo = codigoReciente.get();
            emailService.enviarCodigoMFA(usuario.getCorreo(), usuario.getNombre(), codigo.getCodigo());
            return true;
        } else {
            // Generar nuevo código
            String nuevoCodigo = iniciarMfa(usuario, "email");
            return nuevoCodigo != null;
        }
    }

    /**
     * Limpiar códigos MFA expirados (para ser ejecutado periódicamente)
     */
    @Transactional
    public void limpiarCodigosExpirados() {
        mfaCodeRepository.eliminarCodigosExpirados(LocalDateTime.now());
    }

    /**
     * Verificar estado MFA de un usuario
     */
    public MfaEstado obtenerEstadoMfa(Integer idUsuario) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);
        if (usuarioOpt.isEmpty()) {
            return MfaEstado.NO_CONFIGURADO;
        }

        Usuario usuario = usuarioOpt.get();
        if (!usuario.getMfaHabilitado()) {
            return MfaEstado.NO_CONFIGURADO;
        }

        if (usuario.getMfaSecret() != null) {
            return MfaEstado.APP_CONFIGURADA;
        }

        return MfaEstado.EMAIL_CONFIGURADO;
    }

    /**
     * Generar código numérico simple
     */
    private String generarCodigoNumerico() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    /**
     * Enum para estados MFA
     */
    public enum MfaEstado {
        NO_CONFIGURADO,
        EMAIL_CONFIGURADO,
        APP_CONFIGURADA
    }
}