package co.edu.uniminuto.securelink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * PROVISIONAL: sera reemplazado por el backend de A.
 *
 * <p>Solo se usa la libreria de criptografia de Spring Security, no Spring
 * Security completo. El codificador elegido es BCrypt con costo 10, que es el
 * valor por defecto recomendado por la libreria.</p>
 */
@Configuration
public class ConfiguracionSeguridad {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
