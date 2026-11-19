package co.edu.uniminuto.securelink.modelo;

/**
 * PROVISIONAL: sera reemplazado por el backend de A.
 *
 * <p>Datos que la vista admin necesita de cada usuario. Se entrega solo un
 * fragmento del hash (primeros 20 caracteres) para demostrar que no se guarda
 * ni se muestra la contrasena en texto plano.</p>
 */
public class UsuarioPanel {

    private final String usuario;
    private final String rol;
    private final String hashPreview;

    public UsuarioPanel(String usuario, String rol, String hashPreview) {
        this.usuario = usuario;
        this.rol = rol;
        this.hashPreview = hashPreview;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getRol() {
        return rol;
    }

    public String getHashPreview() {
        return hashPreview;
    }
}
