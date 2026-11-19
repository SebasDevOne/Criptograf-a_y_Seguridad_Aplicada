package co.edu.uniminuto.securelink.datos;

import co.edu.uniminuto.securelink.modelo.Usuario;
import co.edu.uniminuto.securelink.repositorio.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * PROVISIONAL: sera reemplazado por el backend de A.
 *
 * <p>Crea tres usuarios de demostracion al arrancar. Las contrasenas son
 * ficticias y estan documentadas en README.md. En la base solo queda el hash
 * bcrypt, generado con el mismo PasswordEncoder de la aplicacion.</p>
 */
@Component
public class DatosDemo implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DatosDemo(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste("admin", "Admin123*", "admin");
        crearSiNoExiste("usuario", "Usuario123*", "usuario");
        crearSiNoExiste("invitado", "Invitado123*", "invitado");
    }

    private void crearSiNoExiste(String nombre, String contrasena, String rol) {
        if (usuarioRepository.findByUsuario(nombre).isEmpty()) {
            String hash = passwordEncoder.encode(contrasena);
            usuarioRepository.save(new Usuario(nombre, hash, rol));
        }
    }
}
