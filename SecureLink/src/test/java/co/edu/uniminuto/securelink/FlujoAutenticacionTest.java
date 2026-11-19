package co.edu.uniminuto.securelink;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Pruebas de integracion del flujo de autenticacion.
 *
 * <p>Dependen solo de las rutas, campos de formulario y atributos definidos en
 * CONTRATO.md. Por eso deben seguir sirviendo cuando se reemplace el backend
 * provisional por el backend real del companero.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class FlujoAutenticacionTest {

    @Autowired
    private MockMvc mockMvc;

    // (a) Inicio de sesion correcto y redireccion segun el rol.
    @Test
    void inicioDeSesionCorrectoRedirigeSegunElRol() throws Exception {
        MockHttpSession sesionAdmin = iniciarSesion("admin", "Admin123*", "/admin");
        assertThat(sesionAdmin.getAttribute("usuario")).isEqualTo("admin");
        assertThat(sesionAdmin.getAttribute("rol")).isEqualTo("admin");
        mockMvc.perform(get("/admin").session(sesionAdmin))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"));

        MockHttpSession sesionUsuario = iniciarSesion("usuario", "Usuario123*", "/usuario");
        assertThat(sesionUsuario.getAttribute("rol")).isEqualTo("usuario");
        mockMvc.perform(get("/usuario").session(sesionUsuario))
                .andExpect(status().isOk())
                .andExpect(view().name("usuario"));

        MockHttpSession sesionInvitado = iniciarSesion("invitado", "Invitado123*", "/invitado");
        assertThat(sesionInvitado.getAttribute("rol")).isEqualTo("invitado");
        mockMvc.perform(get("/invitado").session(sesionInvitado))
                .andExpect(status().isOk())
                .andExpect(view().name("invitado"));
    }

    // (b) Inicio de sesion fallido: mismo mensaje generico, sin iniciar sesion.
    @Test
    void inicioDeSesionFallidoMuestraMensajeGenerico() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/login")
                        .param("usuario", "admin")
                        .param("contrasena", "contrasena-incorrecta"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Usuario o contraseña incorrectos"))
                .andReturn();

        Object usuarioEnSesion = resultado.getRequest().getSession(false) == null
                ? null
                : resultado.getRequest().getSession(false).getAttribute("usuario");
        assertThat(usuarioEnSesion).isNull();
    }

    // (c) Ruta protegida sin sesion: redirige a /login.
    @Test
    void rutaProtegidaSinSesionRedirigeAlLogin() throws Exception {
        List<String> rutasProtegidas = List.of("/admin", "/usuario", "/invitado");
        for (String ruta : rutasProtegidas) {
            mockMvc.perform(get(ruta))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }
    }

    // (d) Usuario autenticado con rol "usuario" no puede entrar a /admin: 403.
    @Test
    void usuarioSinRolAdminRecibe403EnAdmin() throws Exception {
        MockHttpSession sesion = iniciarSesion("usuario", "Usuario123*", "/usuario");

        mockMvc.perform(get("/admin").session(sesion))
                .andExpect(status().isForbidden())
                .andExpect(view().name("acceso-denegado"))
                .andExpect(content().string(containsString("Acceso denegado")));
    }

    // (e) Tras cerrar sesion se invalida la sesion y se vuelve a pedir autenticacion.
    @Test
    void trasCerrarSesionSeVuelveAPedirAutenticacion() throws Exception {
        MockHttpSession sesion = iniciarSesion("admin", "Admin123*", "/admin");

        mockMvc.perform(post("/logout").session(sesion))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        assertThat(sesion.isInvalid()).isTrue();

        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // (f) El panel admin muestra un hash bcrypt (empieza por "$2") y nunca la contrasena.
    // Se comprueba a traves del contrato (vista admin) para no depender del repository de A.
    @Test
    void elHashGuardadoEmpiezaPorDosYNoEsLaContrasena() throws Exception {
        String usuario = "usuario_prueba_hash";
        String contrasena = "ClaveDePrueba123*";

        mockMvc.perform(post("/registro")
                        .param("usuario", usuario)
                        .param("contrasena", contrasena)
                        .param("confirmarContrasena", contrasena))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        MockHttpSession sesionAdmin = iniciarSesion("admin", "Admin123*", "/admin");
        String htmlAdmin = mockMvc.perform(get("/admin").session(sesionAdmin))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String filaUsuario = Arrays.stream(htmlAdmin.split("<tr>"))
                .filter(fragmento -> fragmento.contains(">" + usuario + "<"))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No se encontro el usuario registrado en el panel de administracion"));

        Matcher matcher = Pattern.compile("<code>(\\$2[^<]+)</code>").matcher(filaUsuario);
        assertThat(matcher.find())
                .as("La vista admin debe mostrar un hash que empiece por $2")
                .isTrue();

        String vistaPreviaHash = matcher.group(1);
        assertThat(vistaPreviaHash)
                .as("Se muestran los primeros 20 caracteres del hash")
                .startsWith("$2")
                .hasSize(20);
        assertThat(htmlAdmin)
                .as("La contrasena nunca debe aparecer en la pagina")
                .doesNotContain(contrasena);
    }

    // (g) Las rutas protegidas no deben quedar en la cache del navegador.
    @Test
    void panelAdminEnviaCabecerasParaNoUsarCache() throws Exception {
        MockHttpSession sesion = iniciarSesion("admin", "Admin123*", "/admin");

        mockMvc.perform(get("/admin").session(sesion))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("no-store")))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("Expires", "0"));
    }

    /**
     * Inicia sesion con una peticion POST /login y devuelve la sesion creada.
     * Solo usa los campos y rutas del contrato.
     */
    private MockHttpSession iniciarSesion(String usuario, String contrasena, String redireccionEsperada)
            throws Exception {

        MvcResult resultado = mockMvc.perform(post("/login")
                        .param("usuario", usuario)
                        .param("contrasena", contrasena))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(redireccionEsperada))
                .andReturn();

        MockHttpSession sesion = (MockHttpSession) resultado.getRequest().getSession(false);
        assertThat(sesion)
                .as("El login correcto debe crear una sesion")
                .isNotNull();
        return sesion;
    }
}
