# Parte III - Registro, autenticación y control de acceso por roles

Trabajo del taller **Criptografía y Seguridad Aplicada** de UNIMINUTO, empresa
ficticia **SECURELINK**. Esta carpeta contiene la interfaz, las pruebas y un
backend provisional para ejecutar la aplicación en local.

> Los ejercicios de la Parte II (`rc4_demo.py`, `rc5_simulacion.py`) están fuera
> de esta carpeta y no se modificaron.

## Tecnologías

- Java 17 como objetivo de compilación (probado con **JDK 25.0.2 Temurin**).
- Spring Boot **4.1.2** (última versión estable al momento del trabajo).
- Spring MVC + Thymeleaf + Bootstrap 5 por CDN.
- Spring Data JPA + H2 en memoria.
- `BCryptPasswordEncoder` de `spring-security-crypto` (sin Spring Security completo).
- JUnit Jupiter + MockMvc (Spring Boot 4.1.2 administra JUnit 6; las
  anotaciones usadas son las habituales `@Test` / `@SpringBootTest`).
- Maven Wrapper (`mvnw` y `mvnw.cmd`); no hace falta instalar Maven.

## Requisitos

- JDK 17 o superior. Recomendado: JDK 25.
- Conexión a Internet la primera vez (Maven descarga dependencias y Bootstrap se
  carga por CDN).
- No hace falta instalar Maven ni una base de datos.

## Comprobar el JDK (Windows PowerShell)

```powershell
java -version
java -version 2>&1 | Select-Object -First 1
```

Debe aparecer una versión 17 o superior. Para comprobar también el wrapper:

```powershell
cd C:\Users\user\Documents\Talller_Cripto\SecureLink
.\mvnw.cmd -version
```

La primera ejecución descarga Maven 3.9.11 y las dependencias del proyecto.

## Ejecutar la aplicación

```powershell
cd C:\Users\user\Documents\Talller_Cripto\SecureLink
.\mvnw.cmd spring-boot:run
```

Abrir en el navegador:

**http://localhost:8080/login**

Para detener la aplicación: `Ctrl + C` en la terminal.

La base H2 es en memoria: se crea al arrancar y se borra al detener. Las
contraseñas de demostración se convierten a bcrypt al iniciar.

## Usuarios de demostración

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `Admin123*` | `admin` |
| `usuario` | `Usuario123*` | `usuario` |
| `invitado` | `Invitado123*` | `invitado` |

Son contraseñas ficticias, solo para el taller. El panel `/admin` muestra los
primeros 20 caracteres del hash bcrypt de cada usuario.

## Ejecutar las pruebas

```powershell
cd C:\Users\user\Documents\Talller_Cripto\SecureLink
.\mvnw.cmd test
```

Resultado esperado:

```text
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Las pruebas están en
`src/test/java/co/edu/uniminuto/securelink/FlujoAutenticacionTest.java` y cubren:

1. login correcto y redirección por rol;
2. login fallido con mensaje genérico;
3. ruta protegida sin sesión;
4. usuario sin rol admin recibe 403;
5. reautenticación después del logout;
6. hash bcrypt en el panel admin, sin contraseña en texto plano.

## Estructura del proyecto

```text
SecureLink/
  src/main/resources/templates/     <- INTERFAZ (se conserva al integrar)
  src/test/java/...                 <- PRUEBAS (se conservan al integrar)
  src/main/java/...                 <- BACKEND PROVISIONAL (lo reemplaza A)
  CONTRATO.md                       <- acuerdo backend <-> plantillas
  pruebas_manuales.md               <- pasos para las capturas
  explicacion_tecnica.md            <- borrador para el informe
  README.md
  pom.xml
  mvnw, mvnw.cmd, .mvn/
  .gitignore
```

El backend Java incluido está marcado como **PROVISIONAL** en el comentario de
cada clase. Sirve para probar la interfaz y las pruebas, y será reemplazado por
el backend de A.

## Cómo integrar con el backend de A

1. **Copiar solo la interfaz:** pasar
   `src/main/resources/templates/` al proyecto de A. No hace falta copiar el
   paquete `co.edu.uniminuto.securelink` completo.
2. **Alinear el backend con `CONTRATO.md`:** rutas, métodos, campos de
   formulario, atributos de sesión, atributos de modelo y nombres de vista.
   Las plantillas usan `@{...}` y no tienen rutas ni clases Java escritas a mano.
3. **Dependencias del proyecto de A:** debe tener Thymeleaf y
   `spring-security-crypto` para bcrypt. No se necesita Spring Security completo.
4. **Copiar las pruebas:** llevar
   `FlujoAutenticacionTest.java` al proyecto de A y agregar la dependencia de
   pruebas MVC. Con Spring Boot 4.1.x es
   `spring-boot-starter-webmvc-test` y el import de `@AutoConfigureMockMvc` es
   `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`.
   Si A usa Spring Boot 3.x, el import cambia a
   `org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc`
   y la dependencia de pruebas es `spring-boot-starter-test`; **[AJUSTAR]** en
   ese caso.
5. **Cuentas de demostración:** conservar `admin`, `usuario` e `invitado` con
   las contraseñas de la tabla anterior, o actualizar las credenciales de las
   pruebas y de `CONTRATO.md`.
6. **Verificar:** ejecutar `.\mvnw.cmd test` en el proyecto integrado y repetir
   las pruebas manuales de `pruebas_manuales.md`. Las capturas **finales** del
   informe se toman con el backend real de A.

## Notas de seguridad del backend provisional

- Las contraseñas se guardan con `BCryptPasswordEncoder` (costo 10).
- El login usa un mensaje genérico para no revelar si el usuario existe.
- El registro público fija el rol `usuario` en el servidor.
- El interceptor vuelve a leer el rol desde la base de datos en cada petición
  protegida.
- El logout ejecuta `session.invalidate()`.
- La sesión viaja solo en la cookie (`tracking-modes=cookie`), no en la URL.
- La vista admin solo muestra un fragmento del hash, nunca la contraseña.
