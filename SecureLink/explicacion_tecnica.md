# Explicación técnica

Borrador para el informe. Las marcas **[AJUSTAR]** señalan lo que puede cambiar
cuando se integre el backend real de A.

## 1. Algoritmo de hash: bcrypt

Las contraseñas se almacenan con **bcrypt** mediante `BCryptPasswordEncoder` de
la librería `spring-security-crypto`. No se guarda la contraseña en texto plano
ni se usa un hash rápido como SHA-256 o SHA-512 por sí solo.

Bcrypt es adecuado para contraseñas porque:

- **Genera una sal aleatoria por contraseña.** La sal queda incluida dentro del
  propio hash (por eso el valor almacenado empieza por `$2a$10$...`). Dos
  usuarios con la misma contraseña obtienen hashes distintos, y las tablas
  precalculadas (rainbow tables) dejan de ser útiles.
- **Tiene costo adaptativo.** El factor de costo `10` significa
  aproximadamente `2^10` iteraciones de la función. Si el hardware mejora, se
  puede aumentar el costo sin cambiar el formato del hash.
- **Es deliberadamente lento.** Verificar una contraseña cuesta tiempo; un
  atacante que prueba millones de combinaciones necesita mucho más tiempo que
  con un hash diseñado para ser rápido.
- **Resiste mejor la fuerza bruta y los ataques por diccionario** que un
  SHA-256 sin sal. SHA-256 está pensado para verificar integridad rápidamente,
  no para almacenar contraseñas: es veloz, no incorpora sal por defecto y
  permite probar muchísimas combinaciones por segundo.

El proyecto usa el costo 10 configurado explícitamente en
`ConfiguracionSeguridad.passwordEncoder()`. **[AJUSTAR]** si A cambia el costo
o centraliza el `PasswordEncoder` en otra clase.

## 2. Generación y verificación del hash en Java

- Al registrar un usuario, `UsuarioService.registrar(...)` llama a
  `passwordEncoder.encode(contrasena)`. Ese método genera la sal, aplica bcrypt
  y devuelve una cadena de 60 caracteres que se guarda en el campo `hash` de la
  entidad `Usuario` (columna `hash_contrasena`). La contraseña original no se
  almacena.
- Al iniciar sesión, `UsuarioService.autenticar(...)` busca el usuario y usa
  `passwordEncoder.matches(contrasena, encontrado.getHash())`. `matches()`
  extrae la sal y el costo del hash almacenado, recalcula bcrypt sobre la
  contraseña ingresada y compara el resultado. La aplicación nunca necesita
  "desencriptar" nada, porque bcrypt es una función de un solo sentido.
- Los usuarios de demostración de `DatosDemo` usan el mismo
  `passwordEncoder.encode(...)`; por eso sus hashes también son bcrypt y
  cambian entre arranques (la sal es aleatoria).
- La vista `admin` no recibe el hash completo. `UsuarioService.listarParaAdmin()`
  construye objetos `UsuarioPanel` con `hashPreview`, que son los primeros 20
  caracteres del hash, y la plantilla `admin.html` los muestra en una etiqueta
  `<code>`. Así se evidencia que no hay contraseñas en texto plano sin exponer
  el valor completo.

**[AJUSTAR]** Los nombres de clases y paquetes pueden cambiar en el backend de
A. Lo que debe conservarse es el uso de `BCryptPasswordEncoder.encode(...)` al
registrar y `matches(...)` al autenticar, además del atributo `hashPreview` en
la lista `usuarios` de la vista `admin`.

## 3. Roles y control de acceso en el servidor

El control es explícito, sin Spring Security completo:

1. **Inicio de sesión.** `AutenticacionController.iniciarSesion(...)` verifica
   las credenciales y guarda en la sesión los atributos `usuario` y `rol`. Luego
   redirige según el rol: `admin` a `/admin`, `usuario` a `/usuario` e
   `invitado` a `/invitado`.
2. **Interceptor.** `ConfiguracionWeb.addInterceptors(...)` registra
   `ControlAccesoInterceptor` para `/admin`, `/usuario` y `/invitado`. En cada
   petición, `preHandle(...)`:
   - comprueba que exista una sesión con el atributo `usuario`;
   - busca ese usuario en la base de datos con `UsuarioRepository`;
   - toma el rol desde la base de datos, no solo desde la sesión;
   - actualiza el atributo `rol` de la sesión para que la interfaz muestre el
     valor vigente;
   - compara el rol real con el rol exigido por la ruta.
3. **Sin sesión.** Si no hay sesión válida, el interceptor redirige a `/login`
   y no ejecuta el controlador protegido.
4. **Rol incorrecto.** Si hay sesión pero el rol no corresponde, lanza
   `AccesoDenegadoException`. `ManejadorAccesoDenegado` la convierte en una
   respuesta **HTTP 403** y devuelve la vista `acceso-denegado`.
5. **Cierre de sesión.** `AutenticacionController.cerrarSesion(...)` ejecuta
   `session.invalidate()`, de modo que los atributos `usuario` y `rol`
   desaparecen y las rutas protegidas vuelven a redirigir a `/login`.
6. **Registro.** El formulario público no recibe el rol. El servicio fija
   siempre `usuario`; no es posible crear un administrador enviando un campo
   adicional desde el navegador.
7. **Sin caché en el navegador.** Las respuestas de las rutas protegidas
   (`/admin`, `/usuario`, `/invitado`) y la respuesta del logout envían
   `Cache-Control: no-store, no-cache, must-revalidate, max-age=0`,
   `Pragma: no-cache` y `Expires: 0`. Esta medida evita que, después de cerrar
   sesión, el botón **Atrás** reutilice una página protegida guardada en la
   caché del navegador: la página se vuelve a solicitar al servidor y el
   interceptor exige sesión y rol otra vez.

La decisión de autorización vive en el servidor. La barra de navegación solo
usa `usuario` y `rol` para mostrar información y enlaces; aunque alguien
modifique el HTML desde el navegador, el interceptor vuelve a validar contra la
base de datos antes de permitir el acceso.

**[AJUSTAR]** Si el backend final de A usa un `enum` para el rol, otro nombre de
repositorio o Spring Security completo, cambian las clases concretas. En ese
caso el control de acceso podría implementarse con `SecurityFilterChain` y
`hasRole(...)`, pero deben conservarse las rutas, los atributos de sesión, los
nombres de vista y la respuesta 403 definidos en `CONTRATO.md`. También debe
mantenerse la regla de que el registro público solo crea usuarios con rol
`usuario`, y las respuestas protegidas y el logout deben conservar las
cabeceras anti-caché descritas en el punto 7.
