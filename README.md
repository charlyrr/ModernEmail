# ModernMail

**ModernMail** es un cliente de correo para Android con un diseño limpio y actual, construido sobre
[FairEmail](https://github.com/M66B/FairEmail), la aplicación de código abierto de Marcel Bokhorst.

Conserva todo lo que hace buena a FairEmail (varias cuentas, bandeja unificada, privacidad, cifrado,
filtros, funcionamiento sin conexión) y renueva la interfaz.

> ModernMail es una **versión modificada** de FairEmail. No es la aplicación oficial ni cuenta con el
> respaldo o el soporte de su autor: por favor, no le pidas ayuda a él por problemas de esta versión.

## Diseño

- **Tema "Aire"** en claro, oscuro y negro: fondo gris muy claro, bloques blancos redondeados y un
  único color de acento. Incluye dos variantes de color, Violeta y Profesional.
- **Bandeja de entrada**: título grande, buscador, filtro segmentado (Todos, No leídos, Destacados),
  correos agrupados por día, filas de alto uniforme con punto de no leído y etiqueta de color de la
  cuenta, y barra inferior flotante con botón de redactar.
- **Lectura**: asunto destacado, texto con más margen, adjuntos como tarjetas, barra flotante de
  acciones y botón redondo de responder.
- **Selección de mensajes**: las acciones ocupan el sitio de la barra inferior.
- **Panel de buzones**: título grande y secciones en bloques redondeados.
- **Detalles**: transición suave al abrir un correo, diálogos y menús redondeados y botones en forma
  de píldora.

Al elegir un tema se aplican los ajustes de visualización que lo acompañan; después se pueden cambiar
en Ajustes.

## Instalar y actualizar

Las versiones se publican en [Releases](../../releases).

La forma más cómoda de mantenerla al día es [Obtainium](https://github.com/ImranR98/Obtainium):
añade la dirección de este repositorio y te avisará de cada versión nueva.

ModernMail usa el identificador `eu.faircode.email.modern`, así que puede convivir con la FairEmail
oficial en el mismo teléfono.

## Cómo se mantiene al día con FairEmail

- Cada cambio en la rama `master` se compila y se publica automáticamente.
- Una vez por semana, un proceso automático trae las novedades de FairEmail y las fusiona con los
  cambios de ModernMail. Si todo encaja, publica una versión nueva; si algún cambio choca con el
  diseño de ModernMail, el proceso se detiene y avisa para resolverlo a mano.
- El proceso también se puede lanzar a mano desde la pestaña **Actions**
  ("Incorporar novedades de FairEmail").

## Limitaciones conocidas

- El inicio de sesión con Google o Microsoft (OAuth) está ligado a la aplicación oficial y a su
  firma; en esas cuentas puede hacer falta una contraseña de aplicación.
- La pantalla de redactar y las pantallas de ajustes conservan la estructura de FairEmail, con los
  estilos nuevos.

## Compilar

```
./gradlew assembleGithubRelease
```

Hace falta un archivo `keystore.properties` con una clave de firma propia, como en FairEmail.

## Licencia y créditos

ModernMail se distribuye bajo la licencia [GNU GPL v3](LICENSE), la misma que FairEmail.

Copyright © 2018-2026 Marcel Bokhorst por FairEmail. Las modificaciones de ModernMail se publican
bajo la misma licencia y su código fuente completo está en este repositorio. La documentación
original de FairEmail está en su [repositorio](https://github.com/M66B/FairEmail).
