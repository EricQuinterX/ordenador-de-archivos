# Ordenador de Archivos
Archivos de diferentes carpetas adyacentes son volcados a una carpeta con un order establecido (basada en una lista de criterios) y finalmente enumerados. <br/>
El motivo de su desarrollo surgio por el tiempo extra que se necesitaba para ordenarlos y renombrarlos a cada uno. 

## Requisitos
* Windows 7+
* Java version 1.6+
* SBT 0.13.13

## Generar Ejecutable
1. Abrir la consola de windows dentro del proyecto clonado y ejecutar:
```
> sbt assembly
```
2. Seguir el paso a paso de [Generar .exe](http://trabajosdesisifo.blogspot.com.ar/2015/12/java-bundle-jre-inside-executable-file.html) apartir del **Step 2**

## Utilizacion
1. Instalar el aplicativo
2. Ejecutar el aplicativo `Ordenador.exe`
3. Indicar la ruta donde se encuentren las carpetas adyacentes con los archivos para ordenar
4. Indicar el nombre de la carpeta en donde se va a generar los arhivos ordenados (por default, esta en el `application.config`)
5. Click en **Config** para establecer:
	* Modificar la lista de filtros de prioridades
	* Establecer el tipo de archivo a filtrar, puede ser sql txt cpp etc....
	* Establecer las carpetas adyacentes involucradas
6. Click en **Ordenar**
7. Ver la carpeta nueva resultante con los archivos ordenados y enumerados
