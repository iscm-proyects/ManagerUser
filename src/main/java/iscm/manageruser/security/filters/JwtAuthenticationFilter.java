package iscm.manageruser.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import iscm.manageruser.model.UserEntity;
import iscm.manageruser.repositories.UserRepository;
import iscm.manageruser.request.LoginRequest;
import iscm.manageruser.security.jwt.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    // Define una constante para el nombre del atributo, para evitar errores de tipeo.
    private static final String USERNAME_ATTRIBUTE = "ATTEMPTED_USERNAME";

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            LoginRequest loginRequest = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);

            // <-- CAMBIO 1: Guardar el username en los atributos de la petición
            // Esto lo hace disponible para otros métodos más adelante en la cadena, como unsuccessfulAuthentication.
            request.setAttribute(USERNAME_ATTRIBUTE, loginRequest.username());

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password());

            return getAuthenticationManager().authenticate(authenticationToken);
        } catch (IOException e) {
            // Es mejor encapsular la excepción original para no perder el contexto.
            throw new RuntimeException("Error al leer las credenciales de la solicitud", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        User user = (User) authResult.getPrincipal();

        // 1. Obtener el UserEntity completo desde la base de datos
        // Usamos orElseThrow para manejar el caso (muy improbable) de que el usuario no se encuentre
        UserEntity userEntity = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Error: Usuario autenticado no encontrado en la base de datos."));

        // No es necesario reiniciar los intentos aquí si ya lo hace el UserDetailService
        // Pero si no, esta es una buena práctica:
        if (userEntity.getIntentos_ingreso() > 0) {
            userEntity.setIntentos_ingreso(0);
            userRepository.save(userEntity);
        }

        // 2. Crear el mapa de claims adicionales
        Map<String, Object> additionalClaims = new HashMap<>();
        additionalClaims.put("username", userEntity.getUsername()); // Aunque está en 'sub', a veces es útil tenerlo explícito
        additionalClaims.put("cargo", userEntity.getCargo());
        additionalClaims.put("ciudad", userEntity.getCiudad());

        // Convertimos LocalDate a String para que sea compatible con JSON
        if (userEntity.getFecha_caducidad_password() != null) {
            additionalClaims.put("fecha_caducidad_password", userEntity.getFecha_caducidad_password().toString());
        }

        // 3. Generar el token con los claims adicionales
        String token = jwtUtils.generateAccessToken(user.getUsername(), user.getAuthorities(), additionalClaims);

        // 4. Construir la respuesta HTTP (sin cambios aquí)
        Map<String, Object> httpResponse = new HashMap<>();
        httpResponse.put("token", token);
        httpResponse.put("message", "Autenticación Correcta");
        httpResponse.put("username", user.getUsername());

        response.addHeader("Authorization", "Bearer " + token);
        response.getWriter().write(new ObjectMapper().writeValueAsString(httpResponse));
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().flush();
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        // <-- CAMBIO 2: Obtener el username del atributo de la petición, no de los parámetros.
        String username = (String) request.getAttribute(USERNAME_ATTRIBUTE);

        // Solo procede si pudimos obtener el username.
        if (username != null) {
            userRepository.findByUsername(username).ifPresent(userEntity -> {
                // Solo incrementa si el usuario no está ya bloqueado
                if (!userEntity.isBloqueado()) {
                    userEntity.setIntentos_ingreso(userEntity.getIntentos_ingreso() + 1);
                    if (userEntity.getIntentos_ingreso() >= 3) {
                        userEntity.setBloqueado(true);
                        logger.warn(String.format("Usuario '%s' ha sido bloqueado por exceso de intentos de login.", username));
                    }
                    userRepository.save(userEntity);
                }
            });
        }

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Credenciales inválidas o usuario bloqueado.");
        errorResponse.put("message", failed.getMessage());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
}