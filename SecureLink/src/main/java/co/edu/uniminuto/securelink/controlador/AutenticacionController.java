package co.edu.uniminuto.securelink.controlador;

import co.edu.uniminuto.securelink.modelo.Usuario;
import co.edu.uniminuto.securelink.servicio.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * PROVISIONAL: sera reemplazado por el backend de A.
 *
 * <p>Implementa las rutas del contrato relacionadas con registro, inicio y
 * cierre de sesion.</p>
 */
@Controller
public class AutenticacionController {

    /** Mensaje generico: no revela si el usuario existe o la contrasena es incorrecta. */
    static final String MENSAJE_LOGIN_FALLIDO = "Usuario o contraseña incorrectos";

    private final UsuarioService usuarioService;

    public AutenticacionController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String iniciarSesion(@RequestParam String usuario,
                                @RequestParam String contrasena,
                                HttpSession sesion,
                                Model modelo) {

        Optional<Usuario> autenticado = usuarioService.autenticar(usuario.trim(), contrasena);

        if (autenticado.isEmpty()) {
            modelo.addAttribute("error", MENSAJE_LOGIN_FALLIDO);
            return "login";
        }

        Usuario usuarioActual = autenticado.get();
        sesion.setAttribute("usuario", usuarioActual.getUsuario());
        sesion.setAttribute("rol", usuarioActual.getRol());

        return redirigirSegunRol(usuarioActual.getRol());
    }

    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@RequestParam String usuario,
                            @RequestParam String contrasena,
                            @RequestParam String confirmarContrasena,
                            Model modelo,
                            RedirectAttributes atributosRedireccion) {

        String nombreUsuario = usuario.trim();

        if (nombreUsuario.isBlank() || contrasena.isBlank()) {
            modelo.addAttribute("error", "El usuario y la contraseña son obligatorios.");
            return "registro";
        }

        if (nombreUsuario.length() > 50) {
            modelo.addAttribute("error", "El nombre de usuario no puede superar 50 caracteres.");
            return "registro";
        }

        if (!contrasena.equals(confirmarContrasena)) {
            modelo.addAttribute("error", "Las contraseñas no coinciden.");
            return "registro";
        }

        if (usuarioService.existe(nombreUsuario)) {
            modelo.addAttribute("error", "Ese nombre de usuario ya está registrado.");
            return "registro";
        }

        // El rol NO se toma del formulario: el registro publico siempre crea "usuario".
        usuarioService.registrar(nombreUsuario, contrasena);

        atributosRedireccion.addFlashAttribute(
                "mensaje", "Usuario registrado correctamente. Ya puedes iniciar sesión.");
        return "redirect:/login";
    }

    @PostMapping("/logout")
    public String cerrarSesion(HttpSession sesion,
                               HttpServletResponse respuesta,
                               RedirectAttributes atributosRedireccion) {
        // La respuesta del logout tampoco debe quedar en la cache del navegador.
        respuesta.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        respuesta.setHeader("Pragma", "no-cache");
        respuesta.setHeader("Expires", "0");

        sesion.invalidate();
        atributosRedireccion.addFlashAttribute("mensaje", "Sesión cerrada correctamente.");
        return "redirect:/login";
    }

    private String redirigirSegunRol(String rol) {
        if ("admin".equals(rol)) {
            return "redirect:/admin";
        }
        if ("usuario".equals(rol)) {
            return "redirect:/usuario";
        }
        if ("invitado".equals(rol)) {
            return "redirect:/invitado";
        }
        // Un rol desconocido no debe llevar a ninguna ruta protegida.
        return "redirect:/login";
    }
}
