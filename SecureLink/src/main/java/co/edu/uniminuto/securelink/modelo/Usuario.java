package co.edu.uniminuto.securelink.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * PROVISIONAL: sera reemplazado por el backend de A.
 *
 * <p>Representa un usuario del sistema. En la base de datos solo se guarda el
 * hash bcrypt de la contrasena, nunca la contrasena en texto plano.</p>
 */
@Entity
@Table(name = "usuarios",
        uniqueConstraints = @UniqueConstraint(name = "uk_usuarios_usuario", columnNames = "usuario"))
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String usuario;

    @Column(name = "hash_contrasena", nullable = false, length = 60)
    private String hash;

    @Column(nullable = false, length = 20)
    private String rol;

    /** Constructor vacio requerido por JPA. */
    public Usuario() {
    }

    public Usuario(String usuario, String hash, String rol) {
        this.usuario = usuario;
        this.hash = hash;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
