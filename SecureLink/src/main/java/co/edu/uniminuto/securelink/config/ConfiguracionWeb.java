package co.edu.uniminuto.securelink.config;

import co.edu.uniminuto.securelink.seguridad.ControlAccesoInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * PROVISIONAL: sera reemplazado por el backend de A.
 *
 * <p>Registra el interceptor que revisa la sesion y el rol antes de entrar a
 * las rutas protegidas. No se usa Spring Security completo: el control es
 * explicito para poder explicarlo en el informe.</p>
 */
@Configuration
public class ConfiguracionWeb implements WebMvcConfigurer {

    private final ControlAccesoInterceptor controlAccesoInterceptor;

    public ConfiguracionWeb(ControlAccesoInterceptor controlAccesoInterceptor) {
        this.controlAccesoInterceptor = controlAccesoInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(controlAccesoInterceptor)
                .addPathPatterns("/admin", "/usuario", "/invitado");
    }
}
