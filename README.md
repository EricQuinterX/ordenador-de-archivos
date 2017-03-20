# Ordenador de Archivos
Archivos de diferentes carpetas adyacentes son volcados a una carpeta con un order establecido (basada en una lista de prioridades) y enumerados.
<br />
## Requisitos
* Windows 7+
* Java version 1.8 (obligatorio para el ejecutable)
* SBT 0.13.13
<br />
## Generar Ejecutable
  1. Abrir la consola de windows dentro del proyecto clonado y ejecutar:
```
> sbt assembly
```
2. Entrar `\target\scala-2.12` y copiar el `.jar` generado.
3. Copiar el `application.conf` del root del proyecto.
4. Los 2 archivos copiados, pegarlos en otro lugar deseado.

## Utilizacion
1. Ejecutar el aplicativo `.jar`
2. Especificar la ruta de las carpetas **NEW** y **OLD**. Aquellas deben contener los script `*.sql`. 
3.
4.
5.
