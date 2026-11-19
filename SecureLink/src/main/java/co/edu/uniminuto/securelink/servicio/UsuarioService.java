package co.edu.uniminuto.securelink.servicio;

import co.edu.uniminuto.securelink.modelo.Usuario;
import co.edu.uniminuto.securelink.modelo.UsuarioPanel;
import co.edu.uniminuto.securelink.repositorio.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PROVISIONAL: sera reemplazado por el backend de A.
 *
 * <p>Centraliza el registro, la verificacion de contrasenas con bcrypt y la
 * consulta de usuarios para el panel de administracion.</p>
 */
@Service
public class UsuarioService {

    /** El registro publico siempre crea usuarios con este rol. */
    private static final String ROL_USUARIO = "usuario";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean existe(String usuario) {
        return usuarioRepository.existsByUsuario(usuario);
    }

    /**
     * Registra un usuario nuevo. La contrasena se transforma con bcrypt antes
     * de guardarla y el rol se fija en "usuario" desde el servidor.
     */
    public Usuario registrar(String usuario, String contrasena) {
        String hash = passwordEncoder.encode(contrasena);
        Usuario nuevo = new Usuario(usuario, hash, ROL_USUARIO);
        return usuarioRepository.save(nuevo);
    }

    /**
     * Verifica usuario y contrasena. Usa matches(), que compara la contrasena
     * ingresada con el hash almacenado; la contrasena original no se guarda.
     */
    public Optional<Usuario> autenticar(String usuario, String contrasena) {
        if (usuario == null || contrasena == null) {
            return Optional.empty();
        }
        return usuarioRepository.findByUsuario(usuario)
                .filter(encontrado -> passwordEncoder.matches(contrasena, encontrado.getHash()));
    }

    public Optional<Usuario> buscar(String usuario) {
        return usuarioRepository.findByUsuario(usuario);
    }

    /** Devuelve la lista que espera la plantilla admin, con el hash recortado. */
    public List<UsuarioPanel> listarParaAdmin() {
        return usuarioRepository.findAllByOrderByUsuarioAsc().stream()
                .map(usuario -> new UsuarioPanel(
                        usuario.getUsuario(),
                        usuario.getRol(),
                        acortarHash(usuario.getHash())))
                .collect(Collectors.toList());
    }

    private String acortarHash(String hash) {
        return hash.substring(0, Math.min(20, hash.length()));
    }
}
