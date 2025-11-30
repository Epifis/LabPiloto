package edu.upc.labpilot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // ============================================
                // 🔓 RUTAS PÚBLICAS - ORDEN ESPECÍFICO PRIMERO
                // ============================================

                // DOCUMENTACIÓN Y MONITORING (Primero para evitar conflictos)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/error").permitAll()

                // AUTENTICACIÓN - ADMIN
                .requestMatchers("/api/auth/admin/login").permitAll()

                // AUTENTICACIÓN - ESTUDIANTE
                .requestMatchers("/api/auth/estudiante/registro").permitAll()
                .requestMatchers("/api/auth/estudiante/validar-correo").permitAll()
                .requestMatchers("/api/auth/estudiante/login").permitAll()
                .requestMatchers("/api/auth/estudiante/validar-mfa").permitAll()

                // AUTENTICACIÓN - PROFESOR
                .requestMatchers("/api/auth/profesor/registro").permitAll()
                .requestMatchers("/api/auth/profesor/validar-correo").permitAll()
                .requestMatchers("/api/auth/profesor/login").permitAll()
                .requestMatchers("/api/auth/profesor/validar-mfa").permitAll()

                // MFA Y AUTH GENERALES
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/mfa/**").permitAll()

                // CONTRATOS - RUTAS ESPECÍFICAS PRIMERO
                .requestMatchers("/api/admins/solicitar/**").permitAll()
                .requestMatchers("/api/admins/aprobar/**").permitAll()
                .requestMatchers("/api/admins/rechazar/**").permitAll()
                .requestMatchers("/api/admins/firmar-contrato/**").permitAll()
                .requestMatchers("/api/contrato/firmar-estudiante/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/usuarios/firmar-contrato/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/usuarios/firmar-contrato/**").permitAll()
                
                // CONTRATOS GENERALES (después de las específicas)
                .requestMatchers("/api/contrato/**").permitAll()

                // USUARIOS - REGISTRO LEGADO
                .requestMatchers("/api/usuarios/registrar").permitAll()
                .requestMatchers("/api/usuarios/verificar-codigo").permitAll()
                .requestMatchers("/api/usuarios/reenviar-codigo").permitAll()

                // RECURSOS PÚBLICOS
                .requestMatchers("/api/laboratorios/**").permitAll()
                .requestMatchers("/api/elementos/**").permitAll()
                .requestMatchers("/api/reservas/**").permitAll()
                .requestMatchers("/api/prestamos/**").permitAll()
                .requestMatchers("/api/cursos/**").permitAll()

                // ============================================
                // 🔐 RUTAS AUTENTICADAS
                // ============================================

                // CONTRATO AUTENTICADO
                .requestMatchers("/api/contrato/aceptar").authenticated()
                .requestMatchers("/api/contrato/verificar-aceptacion").authenticated()

                // USUARIOS (ADMIN / SUPERADMIN)
                .requestMatchers(HttpMethod.GET, "/api/usuarios").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/usuarios/{id}").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/usuarios/**").hasAnyRole("ADMIN", "SUPERADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("SUPERADMIN")

                // SUPERADMIN EXCLUSIVO
                .requestMatchers("/api/superadmin/**").hasRole("SUPERADMIN")
                .requestMatchers("/api/system/**").hasRole("SUPERADMIN")

                // ============================================
                // 🚀 CUALQUIER OTRA RUTA REQUIERE AUTENTICACIÓN
                // ============================================
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
