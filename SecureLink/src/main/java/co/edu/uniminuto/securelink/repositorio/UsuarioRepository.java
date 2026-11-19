package co.edu.uniminuto.securelink.repositorio;

import co.edu.uniminuto.securelink.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * PROVISIONAL: sera reemplazado por el backend de A.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsuario(String usuario);

    boolean existsByUsuario(String usuario);

    List<Usuario> findAllByOrderByUsuarioAsc();
}
