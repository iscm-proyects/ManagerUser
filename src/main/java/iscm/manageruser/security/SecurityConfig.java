package iscm.manageruser.security;


import iscm.manageruser.repositories.UserRepository;
import iscm.manageruser.security.filters.JwtAuthenticationFilter;
import iscm.manageruser.security.filters.JwtAuthorizationFilter;
import iscm.manageruser.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity(prePostEnabled = true) // @EnableGlobalMethodSecurity está deprecado
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final JwtAuthorizationFilter jwtAuthorizationFilter;
    private final UserRepository userRepository; // Necesario para el filtro de autenticación

    // Inyección por constructor (MEJOR PRÁCTICA)
    public SecurityConfig(JwtUtils jwtUtils, UserDetailsService userDetailsService,
                          JwtAuthorizationFilter jwtAuthorizationFilter, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, AuthenticationManager authenticationManager) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtils, userRepository);
        jwtAuthenticationFilter.setAuthenticationManager(authenticationManager);
        jwtAuthenticationFilter.setFilterProcessesUrl("/api/v1/login"); // Es mejor ser explícito con la URL completa

        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                // SOLUCIÓN: Simplemente habilita CORS. Spring usará el bean 'CorsConfigurationSource' que ya definiste.
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> {
                    // Asegúrate que la URL del login coincida con la del filtro
                    auth.requestMatchers("/api/v1/login").permitAll();

                    // Si usas Swagger/OpenAPI, también deberías permitir el acceso a su UI
                    auth.requestMatchers( "/api-docs/**","/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll();
                    auth.requestMatchers("/.well-known/jwks.json").permitAll();

                    auth.anyRequest().authenticated();
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilter(jwtAuthenticationFilter)
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        // La forma moderna y recomendada de obtener el AuthenticationManager
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        // NO USES "*" en producción. Carga los orígenes desde application.properties
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
