# ModernMail

ModernMail es una **versión modificada de [FairEmail](https://github.com/M66B/FairEmail)**, la aplicación de correo
de código abierto de Marcel Bokhorst. Conserva todo su funcionamiento interno (cuentas, sincronización, cifrado,
filtros, privacidad) y cambia la parte visual.

> ModernMail **no es la aplicación oficial** ni está respaldada por el autor de FairEmail.
> No le pidas soporte a él por problemas de esta versión.

## Qué cambia respecto a FairEmail

- **Diseño "Aire"** (tema por defecto, en claro, oscuro y negro): fondo gris muy claro, bloques blancos
  redondeados y un único color de acento. También hay dos variantes de color, Violeta y Profesional.
- **Bandeja de entrada**: título grande, buscador, filtro segmentado (Todos, No leídos, Destacados),
  correos agrupados por día en bloques, filas de alto uniforme con punto de no leído y etiqueta de color
  de la cuenta, y barra inferior flotante con botón de redactar.
- **Lectura de un correo**: asunto destacado, más margen en el texto, adjuntos como tarjetas, barra
  flotante de acciones y botón redondo de responder.
- **Selección de mensajes**: las acciones ocupan el sitio de la barra inferior.
- **Panel de buzones**: título grande y secciones en bloques redondeados.
- **Transición** suave al abrir y cerrar un correo.
- **General**: diálogos y menús redondeados, botones en forma de píldora y barra superior plana.

Al elegir un tema se aplican los ajustes de visualización que lo acompañan; después se pueden cambiar
en Ajustes.

## Instalación y actualizaciones

Cada cambio en la rama `estetica-moderna` se compila automáticamente y se publica en
[Releases](../../releases). Se puede seguir con [Obtainium](https://github.com/ImranR98/Obtainium)
añadiendo la dirección de este repositorio.

ModernMail se instala con el identificador `eu.faircode.email.modern`, de modo que puede convivir con la
FairEmail oficial.

Limitación conocida: el inicio de sesión con Google o Microsoft (OAuth) está ligado a la aplicación
oficial y a su firma; en esas cuentas puede hacer falta una contraseña de aplicación.

## Licencia

Como FairEmail, ModernMail se distribuye bajo la licencia
[GNU GPL v3](LICENSE). Copyright © 2018-2026 Marcel Bokhorst por FairEmail; las modificaciones de
ModernMail se publican bajo la misma licencia. El código fuente completo está en este repositorio.
