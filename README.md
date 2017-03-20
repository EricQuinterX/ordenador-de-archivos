# Ordenador de Archivos
Archivos de diferentes carpetas adyacentes son volcados a una carpeta con un order establecido (basada en una lista de prioridades) y enumerados.
<br />
<br />
## Requisitos
* Windows 7+
* Java version 1.8 (obligatorio para el ejecutable)
* SBT 0.13.13
<br />
<br/>
## Generar Ejecutable
1. Abrir la consola de windows dentro del proyecto clonado y ejecutar:
```
> sbt build-launcher
```
2. Entrar en `/target/sbt-launch4j`, copiar la carpeta `app` y pegarlo en otro lado.
3. Copiar el `application.config` del root del proyecto, podes modificarlo en base a tus requerimientos.
4. Copiar el `/jre` del jdk instalado.
5. Pegar estos 2 ultimos archivos en la carpeta `/app`. Al final quedaria asi:
```
/app
  |- /lib
  |- /jre
  |- application.config
  |- ordenador-de-archivos.exe
```
<br />
## Utilizacion
1. Ejecutar el aplicativo `.exe`
2. Especificar la ruta donde se encuentran las carpetas adyacentes con los archivos a ordenar. 
3. Click en **Config** para establecer:
	* El nombre de la carpeta ...
4.
5.

