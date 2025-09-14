package iscm.manageruser.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "User Management API - User Manager ISCM",
                description = "API para la gestión y autenticación de usuarios del sistema interno.",
                version = "1.0.0",
                contact = @Contact(
                        name = "Área de Sistemas",
                        email = "sistemas@iscm.com",
                        url = "https://www.iscm.com"
                ),
                license = @License(
                        name = "Licencia Propietaria",
                        url = "https://www.iscm.com/licencia"
                )
        ),
        servers = {
                @Server(
                        description = "Servidor de Desarrollo Local",
                        url = "http://localhost:8080"
                ),
                @Server(
                        description = "Servidor de Producción",
                        url = "https://api.iscm.com"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Token JWT para autenticación",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}