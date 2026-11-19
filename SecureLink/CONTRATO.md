# Contrato entre el backend y las plantillas

Este documento es todo lo que el backend necesita respetar para que las plantillas
de `src/main/resources/templates/` funcionen sin cambios. Las plantillas **no**
dependen de clases, paquetes ni tablas del backend.

## 1. Rutas y métodos

| Método | Ruta | Acceso | Respuesta esperada |
|---|---|---|---|
| GET | `/login` | Público | Vista `login` (200). |
| POST | `/login` | Público | Campos `usuario` y `contrasena`. Si es correcto: redirección según el rol. Si falla: vista `login` (200) con el atributo `error`. |
| GET | `/registro` | Público | Vista `registro` (200). |
| POST | `/registro` | Público | Campos `usuario`, `contrasena` y `confirmarContrasena`. Si es correcto: redirección a `/login` con mensaje de éxito. Si falla: vista `registro` (200) con el atributo `error`. |
| POST | `/logout` | Con o sin sesión | Invalida la sesión y redirige a `/login` (302). |
| GET | `/admin` | Solo rol `admin` | Vista `admin` (200). |
| GET | `/usuario` | Solo rol `usuario` | Vista `usuario` (200). |
| GET | `/invitado` | Solo rol `invitado` | Vista `invitado` (200). |

Los formularios usan `method="post"` y las acciones generadas con `@{...}`.
No se envían campos adicionales ni tokens CSRF (no se usa Spring Security completo).

## 2. Campos de los formularios

| Formulario | `action` | Campos obligatorios |
|---|---|---|
| Login | `POST /login` | `usuario`, `contrasena` |
| Registro | `POST /registro` | `usuario`, `contrasena`, `confirmarContrasena` |

El registro público **no** recibe ni acepta un campo de rol. El backend siempre
crea esas cuentas con rol `usuario`.

## 3. Atributos de sesión

| Atributo | Tipo | Valores |
|---|---|---|
| `usuario` | `String` | Nombre del usuario autenticado. |
| `rol` | `String` | `admin`, `usuario` o `invitado`. |

Se guardan al iniciar sesión. El layout los lee para mostrar el usuario, el rol
y el botón **Cerrar sesión**. Al cerrar sesión se invalida la sesión completa.

## 4. Atributos de modelo

| Vista | Atributo | Contenido |
|---|---|---|
| `admin` | `usuarios` | Lista de usuarios. Cada elemento debe exponer `usuario`, `rol` y `hashPreview`. |
| Cualquier vista | `error` | Mensaje de error para mostrar (opcional). |
| Cualquier vista | `mensaje` | Mensaje de éxito para mostrar (opcional). |

`hashPreview` son **los primeros 20 caracteres** del hash bcrypt. El hash
completo no se muestra en ninguna vista.

Los mensajes que llegan por `RedirectAttributes` (por ejemplo después del
registro o del logout) también sirven: las plantillas leen `error` y `mensaje`
como atributos normales del modelo.

## 5. Vistas que debe devolver cada controlador

| Controlador / situación | Nombre de vista |
|---|---|
| Login | `login` |
| Registro | `registro` |
| Panel de administración | `admin` |
| Panel de usuario | `usuario` |
| Zona de invitado | `invitado` |
| Acceso denegado (403) | `acceso-denegado` |

Son los archivos de `src/main/resources/templates/` sin la extensión `.html`.
El fragmento `fragments/layout` no lo devuelve ningún controlador: lo incluyen
las plantillas.

## 6. Comportamiento esperado

1. **Login fallido:** mensaje genérico exacto `Usuario o contraseña incorrectos`,
   sin indicar si el usuario existe o si la contraseña es la incorrecta.
2. **Ruta protegida sin sesión:** redirección a `/login`.
3. **Rol incorrecto:** respuesta HTTP **403** mostrando la vista `acceso-denegado`.
4. **Login correcto:** redirección según el rol:
   - `admin` → `/admin`
   - `usuario` → `/usuario`
   - `invitado` → `/invitado`
5. **Logout:** `POST /logout`, `session.invalidate()` y redirección a `/login`.
6. **Registro:** solo crea rol `usuario`; contraseñas con bcrypt.
7. **Cada petición protegida:** el backend valida la sesión y el rol en el
   servidor. Se recomienda leer el rol actual de la base de datos y no confiar
   únicamente en el valor guardado en la sesión.
8. **Cabeceras anti-caché:** todas las respuestas de `/admin`, `/usuario` y
   `/invitado`, incluida la respuesta de `POST /logout`, deben enviar:

   ```http
   Cache-Control: no-store, no-cache, must-revalidate, max-age=0
   Pragma: no-cache
   Expires: 0
   ```

   Así el botón **Atrás** del navegador no muestra páginas protegidas desde la
   caché después de cerrar sesión; la petición vuelve al servidor y este exige
   autenticación de nuevo.

## 7. Cuentas de demostración usadas por las pruebas

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `Admin123*` | `admin` |
| `usuario` | `Usuario123*` | `usuario` |
| `invitado` | `Invitado123*` | `invitado` |

Son cuentas ficticias del entorno académico. El backend final debe crearlas (o
las pruebas deben ajustarse a las cuentas que defina A).

## Checklist rápido para A

- [ ] Las rutas y métodos de la tabla existen.
- [ ] Los formularios reciben exactamente `usuario`, `contrasena` y `confirmarContrasena`.
- [ ] La sesión tiene `usuario` y `rol`.
- [ ] La vista `admin` recibe `usuarios` con `usuario`, `rol` y `hashPreview`.
- [ ] Los mensajes usan `error` y `mensaje`.
- [ ] Los controladores devuelven los nombres de vista de la tabla.
- [ ] Sin sesión se redirige a `/login`; con rol incorrecto se responde 403.
- [ ] El login correcto redirige según el rol y el logout invalida la sesión.
- [ ] Las rutas protegidas y el logout envían `Cache-Control: no-store, no-cache, must-revalidate, max-age=0`, `Pragma: no-cache` y `Expires: 0`.
