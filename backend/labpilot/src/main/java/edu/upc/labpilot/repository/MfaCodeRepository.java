package edu.upc.labpilot.repository;

import edu.upc.labpilot.model.MfaCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MfaCodeRepository extends JpaRepository<MfaCode, Integer> {

    // ✅ Para verificación de correo
    Optional<MfaCode> findByUsuarioIdAndCodigoAndTipo(Integer usuarioId, String codigo, String tipo);
    
    // ✅ Para login MFA
    @Query("SELECT m FROM MfaCode m WHERE m.usuario.id = :usuarioId AND m.codigo = :codigo AND m.tipo = 'login_mfa'")
    Optional<MfaCode> findByUsuarioIdAndCodigoForLogin(@Param("usuarioId") Integer usuarioId, @Param("codigo") String codigo);

    // ✅ Invalidar códigos de verificación anteriores
    @Modifying
    @Query("UPDATE MfaCode m SET m.utilizado = true WHERE m.usuario.id = :usuarioId AND m.tipo = 'verificacion_correo' AND m.utilizado = false")
    void invalidarCodigosVerificacionAnteriores(@Param("usuarioId") Integer usuarioId);

    // ✅ Invalidar códigos de login anteriores
    @Modifying
    @Query("UPDATE MfaCode m SET m.utilizado = true WHERE m.usuario.id = :usuarioId AND m.tipo = 'login_mfa' AND m.utilizado = false")
    void invalidarCodigosLoginAnteriores(@Param("usuarioId") Integer usuarioId);

    // ✅ Buscar último código de login
    @Query("SELECT m FROM MfaCode m WHERE m.usuario.id = :usuarioId AND m.tipo = 'login_mfa' ORDER BY m.fechaCreacion DESC LIMIT 1")
    Optional<MfaCode> findUltimoCodigoLoginByUsuarioId(@Param("usuarioId") Integer usuarioId);

    // Métodos existentes (mantener)
    Optional<MfaCode> findByUsuarioIdAndCodigo(Integer usuarioId, String codigo);
    
    @Modifying
    @Query("UPDATE MfaCode m SET m.utilizado = true WHERE m.usuario.id = :usuarioId AND m.utilizado = false")
    void invalidarCodigosAnteriores(@Param("usuarioId") Integer usuarioId);

    @Modifying
    @Query("DELETE FROM MfaCode m WHERE m.fechaExpiracion < :fecha")
    void eliminarCodigosExpirados(@Param("fecha") LocalDateTime fecha);
    
    @Query("SELECT m FROM MfaCode m WHERE m.usuario.id = :usuarioId ORDER BY m.fechaCreacion DESC LIMIT 1")
    Optional<MfaCode> findUltimoCodigoByUsuarioId(@Param("usuarioId") Integer usuarioId);
}