package edu.upc.labpilot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configure(http))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz

                // ✅ Rutas de autenticación (públicas)
                .requestMatchers("/api/auth/login").permitAll()
                
                // ✅ Rutas protegidas de autenticación (requieren token)
                .requestMatchers("/api/auth/verify").authenticated()
                .requestMatchers("/api/auth/logout").authenticated()

                // Rutas públicas
                .requestMatchers("/api/laboratorios/**").permitAll()
                .requestMatchers("/api/elementos/**").permitAll()
                .requestMatchers("/api/reservas/**").permitAll()
                .requestMatchers("/api/prestamos/**").permitAll()
                .requestMatchers("/api/usuarios/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/api/admins/solicitar/**").permitAll()
                .requestMatchers("/api/admins/aprobar/**").permitAll()

                // ✅ Rutas de administración (requieren ROLE_ADMIN o ROLE_SUPERADMIN)
                .requestMatchers("/api/admins/**").hasAnyRole("ADMIN", "SUPERADMIN")

                .anyRequest().permitAll()
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
        return new BCryptPasswordEncoder();
    }
}
