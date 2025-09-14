package iscm.manageruser.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Order(1) // Asegura que este filtro se ejecute primero para medir el tiempo correctamente
public class LoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // --- Lista de campos sensibles a enmascarar ---
    private static final List<String> SENSITIVE_FIELDS = Arrays.asList(
            "password", "passwordActual", "newPassword", "token", "jwt"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Generar un ID de correlación único para esta solicitud
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId); // Poner en el contexto de logging

        // Usar los wrappers de Spring
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper((HttpServletResponse) response);

        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequest(wrappedRequest, correlationId);
            logResponse(wrappedResponse, duration, correlationId);

            // ¡Importante! Copiar el cuerpo de la respuesta al stream de salida original
            wrappedResponse.copyBodyToResponse();

            // Limpiar el contexto de logging para el siguiente hilo
            MDC.clear();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, String correlationId) {
        try {
            String requestBody = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
            String sanitizedBody = sanitizeBody(requestBody);

            Map<String, Object> logMap = Map.of(
                    "type", "REQUEST_IN",
                    "correlationId", correlationId,
                    "method", request.getMethod(),
                    "uri", request.getRequestURI(),
                    "clientIp", request.getRemoteAddr(),
                    "principal", request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous",
                    "headers", getHeaders(request),
                    "body", sanitizedBody
            );

            logger.info(objectMapper.writeValueAsString(logMap));
        } catch (Exception e) {
            logger.warn("Failed to log incoming request", e);
        }
    }

    private void logResponse(ContentCachingResponseWrapper response, long duration, String correlationId) {
        try {
            String responseBody = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
            String sanitizedBody = sanitizeBody(responseBody);

            Map<String, Object> logMap = Map.of(
                    "type", "RESPONSE_OUT",
                    "correlationId", correlationId,
                    "status", response.getStatus(),
                    "durationMs", duration,
                    "body", sanitizedBody
            );

            logger.info(objectMapper.writeValueAsString(logMap));
        } catch (Exception e) {
            logger.warn("Failed to log outgoing response", e);
        }
    }

    private String sanitizeBody(String body) {
        if (body == null || body.isEmpty()) {
            return "";
        }
        try {
            // Intenta parsear como JSON para enmascarar campos específicos
            // Si no es JSON, devuelve un placeholder para evitar ingresar contenido no estructurado
            @SuppressWarnings("unchecked")
            Map<String, Object> bodyMap = objectMapper.readValue(body, Map.class);

            SENSITIVE_FIELDS.forEach(field -> {
                if (bodyMap.containsKey(field)) {
                    bodyMap.put(field, "********");
                }
            });
            return objectMapper.writeValueAsString(bodyMap);
        } catch (JsonProcessingException e) {
            // Si el cuerpo no es JSON (ej. texto plano, form-data), no lo registramos
            // para evitar ingresar datos potencialmente sensibles sin querer.
            return "[Non-JSON body]";
        }
    }

    private Map<String, String> getHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(Function.identity(), request::getHeader));
    }
}