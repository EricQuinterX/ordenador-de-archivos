# Ordenador de Archivos
Archivos de diferentes carpetas adyacentes son volcados a una carpeta con un order establecido (basada en una lista de criterios) y finalmente enumerados. <br/>
El motivo de su desarrollo surgio por el tiempo extra que se necesitaba para ordenarlos y renombrarlos a cada uno. 

## Requisitos
* Windows 7+
* Java version 1.8 (obligatorio para el ejecutable)
* SBT 0.13.13

## Generar Ejecutable
1. Abrir la consola de windows dentro del proyecto clonado y ejecutar:
```
> sbt assembly
```
2. Recuperar el `.jar` generado
3. Seguir el paso a paso de [Generar .exe](http://trabajosdesisifo.blogspot.com.ar/2015/12/java-bundle-jre-inside-executable-file.html) apartir del **Step 2**

## Utilizacion
0. Instalar el aplicativo
1. Ejecutar el aplicativo `.exe`
2. Especificar la ruta donde se encuentran las carpetas adyacentes con los archivos para ordenar
3. Click en **Config** para establecer:
	* El nombre de la carpeta destino
	* Modificar la lista de filtros de prioridades
	* Establecer el tipo de archivo a filtrar
	* Establecer las carpetas adyacentes involucradas
4. Click en **Ordenar**
5. Ver la carpeta nueva resultante con los archivos ordenados y enumerados
