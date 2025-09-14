package iscm.manageruser.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtKeyConfig {

    @Value("${jwt.keystore.location}")
    private Resource keystoreResource;

    @Value("${jwt.keystore.password}")
    private String keystorePassword;

    @Value("${jwt.keystore.alias}")
    private String keyAlias;

    @Bean
    public KeyStore keyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream inputStream = keystoreResource.getInputStream()) {
            keyStore.load(inputStream, keystorePassword.toCharArray());
        }
        return keyStore;
    }

    @Bean
    public RSAPrivateKey rsaPrivateKey(KeyStore keyStore) throws Exception {
        return (RSAPrivateKey) keyStore.getKey(keyAlias, keystorePassword.toCharArray());
    }

    @Bean
    public RSAPublicKey rsaPublicKey(KeyStore keyStore) throws Exception {
        return (RSAPublicKey) keyStore.getCertificate(keyAlias).getPublicKey();
    }
}