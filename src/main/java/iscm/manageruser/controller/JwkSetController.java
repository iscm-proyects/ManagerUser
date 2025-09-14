package iscm.manageruser.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RestController
@Tag(name = "Security", description = "Endpoints de seguridad y configuración pública.")
public class JwkSetController {

    private final RSAPublicKey publicKey;

    // Spring inyectará el bean RSAPublicKey que creamos en JwtKeyConfig
    public JwkSetController(RSAPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @Operation(
            summary = "Obtener el JSON Web Key (JWK) Set",
            description = "Endpoint público que expone la(s) clave(s) pública(s) utilizadas para firmar los tokens JWT. " +
                    "Permite a los servicios cliente verificar las firmas de los tokens de forma independiente, " +
                    "siguiendo el estándar de OpenID Connect Discovery."
    )
    @ApiResponse(
            responseCode = "200",
            description = "JWK Set obtenido exitosamente.",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                        {
                          "keys": [
                            {
                              "kty": "RSA",
                              "kid": "bisa-bolsa-key-1",
                              "n": "ALongBase64UrlEncodedString...",
                              "e": "AQAB"
                            }
                          ]
                        }
                        """
                    )
            )
    )
    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> jwks() {
        // 1. Construir un objeto RSAKey a partir de nuestra clave pública.
        RSAKey rsaKey = new RSAKey.Builder(this.publicKey)
                .keyID("bisa-bolsa-jwt") // Un ID único para esta clave. Debe coincidir con el alias del keystore.
                .build();

        // 2. Crear un JWKSet, que es una lista de claves (en nuestro caso, solo una).
        JWKSet jwkSet = new JWKSet(rsaKey);

        // 3. Convertir el JWKSet a un objeto JSON (un Map<String, Object>) para la respuesta.
        return jwkSet.toJSONObject();
    }
}