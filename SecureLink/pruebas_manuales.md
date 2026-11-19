# Pruebas manuales y capturas para el informe

> **Importante:** estas capturas son de control mientras se desarrolla la
> interfaz. Las capturas **FINALES** se toman cuando el backend real de A esté
> integrado, usando los mismos pasos y los mismos nombres de archivo.

## Preparación

1. Abrir PowerShell en la carpeta del proyecto:

   ```powershell
   cd C:\Users\user\Documents\Talller_Cripto\SecureLink
   ```

2. Arrancar la aplicación:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

3. Abrir `http://localhost:8080/login` en el navegador.
4. Usar una ventana de aproximadamente 1280 x 800 px, zoom al 100 %, y procurar
   que se vean la barra de direcciones, el usuario y el rol en la barra superior.
5. Contraseñas de demostración:

   | Usuario | Contraseña | Rol |
   |---|---|---|
   | `admin` | `Admin123*` | `admin` |
   | `usuario` | `Usuario123*` | `usuario` |
   | `invitado` | `Invitado123*` | `invitado` |

> Si una captura se repite, cerrar sesión antes o abrir una ventana de
> incógnito para no arrastrar la sesión anterior.

## 01_login_correcto.png

1. Abrir `http://localhost:8080/login`.
2. Escribir usuario `admin` y contraseña `Admin123*`.
3. Presionar **Entrar**.
4. Verificar que la URL cambió a `/admin` y que la barra superior muestra
   `Usuario: admin`, el rol `admin` y el botón **Cerrar sesión**.
5. Capturar la pantalla completa y guardarla como `01_login_correcto.png`.

## 02_login_fallido.png

1. Si hay una sesión abierta, presionar **Cerrar sesión**.
2. En el formulario de login escribir usuario `admin` y una contraseña
   incorrecta, por ejemplo `incorrecta`.
3. Presionar **Entrar**.
4. Verificar que se queda en `/login` y que aparece el mensaje genérico
   **Usuario o contraseña incorrectos** (no debe decir cuál dato falló).
5. Capturar el mensaje de error y guardarlo como `02_login_fallido.png`.

## 03_ruta_sin_sesion.png

1. Asegurarse de no tener sesión iniciada (cerrar sesión o abrir una ventana
   de incógnito).
2. Escribir directamente en la barra de direcciones:
   `http://localhost:8080/admin`.
3. Verificar que la aplicación redirige a `/login` y que no muestra el panel.
4. Capturar la barra de direcciones con `/login` y la pantalla de login.
5. Guardar como `03_ruta_sin_sesion.png`.

## 04_acceso_denegado_admin.png

1. Iniciar sesión con usuario `usuario` y contraseña `Usuario123*`.
2. Verificar que entra a `/usuario`.
3. Escribir manualmente en la barra de direcciones:
   `http://localhost:8080/admin`.
4. Verificar que aparece la página **Acceso denegado** con el código **403** y
   que la barra superior sigue mostrando el rol `usuario`.
5. Capturar la página y guardarla como `04_acceso_denegado_admin.png`.

## 05_reautenticacion_tras_logout.png

1. Iniciar sesión con `admin` / `Admin123*` y esperar el panel `/admin`.
2. Presionar **Cerrar sesión**.
3. Verificar que vuelve a `/login` y que aparece el mensaje
   **Sesión cerrada correctamente**.
4. Escribir de nuevo `http://localhost:8080/admin` en la barra de direcciones.
5. Verificar que la aplicación vuelve a pedir autenticación (redirige a
   `/login`).
6. Capturar la pantalla final con el mensaje y la URL `/login`; guardar como
   `05_reautenticacion_tras_logout.png`.

## 06_hashes_en_panel_admin.png

1. Iniciar sesión con `admin` / `Admin123*`.
2. En el panel `/admin`, ubicar la tabla de usuarios. Deben verse, como mínimo,
   `admin`, `usuario` e `invitado` con su rol.
3. Verificar que la columna del hash muestra valores que empiezan por `$2`
   (por ejemplo `$2a$10$...`) y que **no** aparece ninguna contraseña en texto
   plano.
4. Capturar la tabla completa y guardarla como `06_hashes_en_panel_admin.png`.

## Lista de archivos esperados

- `01_login_correcto.png`
- `02_login_fallido.png`
- `03_ruta_sin_sesion.png`
- `04_acceso_denegado_admin.png`
- `05_reautenticacion_tras_logout.png`
- `06_hashes_en_panel_admin.png`

## Qué revisar en cada captura

| Captura | Evidencia que debe mostrar |
|---|---|
| 01 | Login correcto y redirección al panel del rol. |
| 02 | Mensaje de error genérico, sin revelar el dato que falló. |
| 03 | Ruta protegida sin sesión redirigida a `/login`. |
| 04 | HTTP 403 y vista de acceso denegado para un rol sin permiso. |
| 05 | La sesión anterior ya no sirve después del logout. |
| 06 | Hashes bcrypt y ausencia de contraseñas en texto plano. |

## Nota para la entrega final

Cuando A integre su backend:

1. Ejecutar la aplicación integrada.
2. Repetir exactamente estos seis recorridos.
3. Reemplazar las capturas de control por las definitivas.
4. Comprobar que las rutas, los campos y los mensajes siguen coincidiendo con
   `CONTRATO.md` antes de tomar las capturas.
