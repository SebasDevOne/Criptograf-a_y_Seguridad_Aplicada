package co.edu.uniminuto.securelink.seguridad;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * PROVISIONAL: sera reemplazado por el backend de A.
 *
 * <p>Convierte una denegacion de acceso en HTTP 403 mostrando la vista
 * "acceso-denegado", tal como exige el contrato.</p>
 */
@ControllerAdvice
public class ManejadorAccesoDenegado {

    @ExceptionHandler(AccesoDenegadoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String mostrarAccesoDenegado() {
        return "acceso-denegado";
    }
}
