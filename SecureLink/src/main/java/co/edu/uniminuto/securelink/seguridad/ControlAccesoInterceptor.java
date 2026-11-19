package co.edu.uniminuto.securelink.seguridad;

import co.edu.uniminuto.securelink.modelo.Usuario;
import co.edu.uniminuto.securelink.repositorio.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

/**
 * PROVISIONAL: sera reemplazado por el backend de A.
 *
 * <p>Control de acceso explicito para /admin, /usuario y /invitado. En cada
 * peticion:</p>
 * <ol>
 *   <li>verifica que exista una sesion con el atributo "usuario";</li>
 *   <li>vuelve a leer el usuario y su rol desde la base de datos;</li>
 *   <li>compara el rol de la base de datos con el rol exigido por la ruta.</li>
 * </ol>
 *
 * <p>No se confia solo en el atributo "rol" de la sesion: ese atributo se usa
 * para mostrar la interfaz, pero la decision de acceso se toma con el dato
 * actual de la base de datos.</p>
 */
@Component
public class ControlAccesoInterceptor implements HandlerInterceptor {

    private final UsuarioRepository usuarioRepository;

    public ControlAccesoInterceptor(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Evita que el navegador guarde en cache las paginas protegidas. Asi el
        // boton "Atras" no puede mostrar contenido de una sesion ya cerrada.
        agregarCabecerasNoCache(response);

        HttpSession sesion = request.getSession(false);
        if (sesion == null) {
            return redirigirAlLogin(request, response);
        }

        Object atributoUsuario = sesion.getAttribute("usuario");
        if (!(atributoUsuario instanceof String nombreUsuario) || nombreUsuario.isBlank()) {
            return redirigirAlLogin(request, response);
        }

        Optional<Usuario> encontrado = usuarioRepository.findByUsuario(nombreUsuario);
        if (encontrado.isEmpty()) {
            // La sesion apunta a un usuario que ya no existe: se invalida.
            sesion.invalidate();
            return redirigirAlLogin(request, response);
        }

        Usuario usuarioActual = encontrado.get();

        // Se refresca el rol visible en la sesion con el valor real de la BD.
        sesion.setAttribute("rol", usuarioActual.getRol());

        String rolRequerido = rolRequeridoPara(request);
        if (rolRequerido == null) {
            // Si en el futuro se agrega otra ruta al interceptor, no se bloquea
            // por accidente; las rutas del contrato siempre tienen rol definido.
            return true;
        }

        if (!rolRequerido.equals(usuarioActual.getRol())) {
            throw new AccesoDenegadoException(
                    "El rol " + usuarioActual.getRol() + " no puede acceder a " + request.getRequestURI());
        }

        return true;
    }

    private String rolRequeridoPara(HttpServletRequest request) {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        if ("/admin".equals(ruta)) {
            return "admin";
        }
        if ("/usuario".equals(ruta)) {
            return "usuario";
        }
        if ("/invitado".equals(ruta)) {
            return "invitado";
        }
        return null;
    }

    private boolean redirigirAlLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.sendRedirect(request.getContextPath() + "/login");
        return false;
    }

    private void agregarCabecerasNoCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }
}
