package iscm.manageruser.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtUtils {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    private final Long timeExpiration = 86400000L;

    @Value("${jwt.keystore.alias}")
    private String keyAlias;

    // Inyección de dependencias de las claves RSA a través del constructor
    public JwtUtils(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    /**
     * Genera un token de acceso firmado con la clave privada RSA (RS256).
     */
    public String generateAccessToken(String username, Collection<? extends GrantedAuthority> authorities) {
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setHeaderParam("kid", keyAlias) // Añade el Key ID a la cabecera
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + timeExpiration))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * Genera un token de acceso incluyendo claims adicionales.
     * @param username El subject del token.
     * @param authorities Las autoridades/roles del usuario.
     * @param additionalClaims Un mapa con los claims extra a añadir al payload.
     * @return El token JWT como un String.
     */
    public String generateAccessToken(String username, Collection<? extends GrantedAuthority> authorities, Map<String, Object> additionalClaims) {
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Construye el builder del token
        JwtBuilder builder = Jwts.builder()
                .setHeaderParam("kid", keyAlias)
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + timeExpiration));

        // Añade los claims adicionales al payload
        if (additionalClaims != null) {
            builder.addClaims(additionalClaims);
        }

        // Firma y compacta el token
        return builder.signWith(privateKey, SignatureAlgorithm.RS256).compact();
    }


    /**
     * Valida la firma de un token utilizando la clave pública RSA.
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(publicKey) // <-- USA LA CLAVE PÚBLICA
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Token JWT inválido o expirado: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrae todos los claims (cuerpo) de un token, validando la firma con la clave pública.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey) // <-- USA LA CLAVE PÚBLICA
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extrae un claim específico de un token.
     */
    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae el nombre de usuario (subject) de un token.
     */
    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }
}