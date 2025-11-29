package edu.upc.labpilot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    // ============================================
    // 🔓 RUTAS PÚBLICAS (DEBE COINCIDIR CON SecurityConfig)
    // ============================================
    private final String[] PUBLIC_PATHS = {

        // ADMIN AUTH
        "/api/auth/admin/login",

        // ESTUDIANTE AUTH
        "/api/auth/estudiante/registro",
        "/api/auth/estudiante/validar-correo",
        "/api/auth/estudiante/login",
        "/api/auth/estudiante/validar-mfa",

        // PROFESOR AUTH
        "/api/auth/profesor/registro",
        "/api/auth/profesor/validar-correo",
        "/api/auth/profesor/login",
        "/api/auth/profesor/validar-mfa",

        // MFA
        "/api/mfa",

        // CONTRATOS Y ADMIN
        "/api/admins/solicitar",
        "/api/admins/aprobar",
        "/api/admins/firmar-contrato",  // ✅ AGREGADO - Esta era la que faltaba!
        "/api/contrato",

        // LEGACY Usuarios
        "/api/usuarios/registrar",
        "/api/usuarios/verificar-codigo",
        "/api/usuarios/reenviar-codigo",
        "/api/usuarios/firmar-contrato",

        // RECURSOS PÚBLICOS
        "/api/laboratorios/public",
        "/api/elementos/public",
        "/api/cursos/public",

        // DOCUMENTACIÓN Y MONITORING
        "/swagger-ui",
        "/v3/api-docs",
        "/api-docs",
        "/actuator/health",
        "/actuator/info",
        "/error"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getServletPath();
        logger.debug("🔍 Ruta solicitada: {}", requestPath);

        // 🔓 Si la ruta es pública, no validar JWT
        if (isPublicPath(requestPath)) {
            logger.debug("✅ Ruta pública detectada, saltando JWT → {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        logger.debug("🔐 Ruta protegida, procesando JWT → {}", requestPath);

        try {
            final String authorizationHeader = request.getHeader("Authorization");
            String jwt = null;
            String username = null;

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);

                try {
                    username = jwtUtil.extractUsername(jwt);
                } catch (Exception e) {
                    logger.error("❌ Error extrayendo usuario del token: {}", e.getMessage());
                }
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    logger.debug("✅ JWT válido. Usuario autenticado: {}", username);
                } else {
                    logger.warn("⚠️ JWT inválido para usuario: {}", username);
                }
            }

        } catch (Exception e) {
            logger.error("❌ Error en JwtAuthenticationFilter: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }
}