package co.edu.uniminuto.securelink.controlador;

import co.edu.uniminuto.securelink.servicio.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * PROVISIONAL: sera reemplazado por el backend de A.
 *
 * <p>Vistas protegidas del contrato. El interceptor ya valido la sesion y el
 * rol antes de que estas rutas se ejecuten.</p>
 */
@Controller
public class PanelController {

    private final UsuarioService usuarioService;

    public PanelController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/admin")
    public String mostrarAdmin(Model modelo) {
        modelo.addAttribute("usuarios", usuarioService.listarParaAdmin());
        return "admin";
    }

    @GetMapping("/usuario")
    public String mostrarUsuario() {
        return "usuario";
    }

    @GetMapping("/invitado")
    public String mostrarInvitado() {
        return "invitado";
    }
}
