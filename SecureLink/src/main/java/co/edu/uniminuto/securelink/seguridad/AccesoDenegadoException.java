package co.edu.uniminuto.securelink.seguridad;

/**
 * PROVISIONAL: sera reemplazado por el backend de A.
 *
 * <p>Se lanza cuando hay sesion valida pero el rol no permite la ruta. El
 * manejador global la convierte en una respuesta HTTP 403 con la vista
 * acceso-denegado.</p>
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
