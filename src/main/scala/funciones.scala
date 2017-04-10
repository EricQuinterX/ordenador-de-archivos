package modulo_app

import core_app._
import config_app._
import errores_app._

import java.io.{File, FileInputStream, FileOutputStream}
import scala.swing.TextArea
import scala.util.{Try, Success, Failure}
import java.util.regex.{Matcher, Pattern}


trait Modulo {

  def startFunction : Unit

  /**
   * Funcion = getFilesFrom
   * Obtiene todos los archivos de un directorio dado.
   */
  def getFilesFrom(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory){
      val tipo = ConfigFile.getFileType.toUpperCase
      val query = if (tipo.isEmpty) "." else s".$tipo"
      d.listFiles.filter(_.isFile).toList.filter(_.getName.toUpperCase.contains(query))
    }
    else List[File]()
  }


  /**
   * Funcion = deleteFolder
   * Elimina los archivos recursivos a una carpeta dada.
   */
  def deleteFolder(destino: File) : Boolean = {
    def deleteAll (file: File) : Boolean = {
      if (file.isDirectory)
        file.listFiles.foreach(deleteAll)
      !(file.exists && !file.delete)
    }
    deleteAll(destino)
  }
}



class Ordenador (ruta : String, out : TextArea, dest : Option[String] = None) extends Modulo {

  var index: Int = 0
  var restantes : List[File] = Nil
  var ordenados : List[(File, String)] = Nil
  var total : Int = 0
  val folders : List[Folder] = ConfigFile.getFolderList
  val carpeta_destino : String = dest.getOrElse(ConfigFile.getFolder())


  /**
   * Funcion = startFunction
   * Funcion principal que recupera los valores configurados y procesa cada archivo.
   */
  def startFunction = {
    val filtros : List[FileKeyword] = ConfigFile.getFilters
    val tamFiltros = filtros.size
    var i : Int = 0
    restantes = getFiles(ruta).sorted
    total = restantes.size

    for (i <- 0 to tamFiltros) {
      if (i == tamFiltros)
        // llego al ultimo, ejecuto los restantes
        filtrar(".", "", "INICIO")
      else
        filtrar(filtros(i).keyword, filtros(i).folder, filtros(i).match_pos)
    }

    val dest_folder = new File(ruta + "\\" + carpeta_destino)

    Try(deleteFolder(dest_folder)) match {
      case Failure(e) => out.append(e.getMessage)
      case Success(s) =>
        if (!s) throw ErrorCrearCarpeta("No se pudo limpiar la carpeta destino")
        val p : Int = createFiles(dest_folder)
        if (p > 0) {
          out.append(s"($p/$total) archivos fueron organizados\n")
          if (p == total)
            out.append("La operacion se completo satisfactoriamente\n")
          else {
            out.append(s"Faltan ordenar ${total - p} archivos")
            throw ErrorTerminarProceso("No se procesaron todos los archivos")
          }
        }
    }
  }


  /**
   * Funcion = getFiles
   * Recupera todos los archivos de todas las carpetas involucradas para ordenarlos
   * Devuelve una lista
   */
  private def getFiles(r: String): List[File] = ConfigFile.getFolderList.flatMap(folder => getFilesFrom(r + "\\" + folder.name))



  /**
   * Funcion = filtrar
   * En base a una palabra, se fija en todos los archivos que estan en la lista 'restantes'.
   * Si encuentra algun archivo que matche con la palabra, se agrega a otra lista 'ordenados'.
   * Al final, la lista 'restantes' se reduce con los que encontro y la lista 'ordenados' crece.
   */
	private def filtrar (word: String, folder: String, pos: String): Unit = {
    // Cada vez que encuentre un *** dentro del keyword, lo reemplazo con .*
    val regex = word.replaceAllLiterally("***", ".*") + ".*"
    val patron = Pattern.compile(regex)

    /**
     * Funcion = criteria
     * Valida si el nombre de un archivo cumple con un regex.
     */
    def criteria(x: File) : Boolean = {
      if (folder.isEmpty) return true
      val matcher = patron.matcher(x.getName)
      if (matcher.find) {
        val p = if (matcher.start == 0) "INICIO" else "MEDIO"
        x.getParentFile.getName == folder && p == pos
      } else false
    }

    var filtrados: List[File] = Nil
    for (f <- restantes.filter(criteria).sortBy(_.getName)){
      ordenados = (f, getNewName(index, f.getParentFile.getName, f.getName)) ::  ordenados
      index += 1
      filtrados = f :: filtrados
    }

    restantes = restantes filterNot filtrados.contains
	}


  /**
   * Funcion = createFiles
   * Crea la carpeta y los archivos ordenados dentro con su secuencia asignada.
   * Devuelve la cantidad procesada.
   */
  private def createFiles(dir: File) : Int = {
    var procesados = 0
    if (ordenados.size == 0) throw ErrorCrearCarpeta("No existen archivos para ordenar")
    if (dir.mkdir)
      out.append(s"Directorio '${dir.getName}' creado.\n")
    else
      throw ErrorCrearCarpeta("No se limpio la carpeta destino")

    out.append(s"Archivos creados:\n")

    for ((src, name) <- ordenados.sortBy(_._2)){
      val destin = new File(dir.getPath + "\\" + name)
      val fos = new FileOutputStream(destin)
      val fin = new FileInputStream(src)
      Try(fos.getChannel.transferFrom( fin.getChannel, 0, Long.MaxValue)) match {
        case Success(_) => procesados += 1
        case Failure(_) => procesados += 0
      }
      fos.close // IMPORTANTE CERRAR EL ARCHIVO
      fin.close // IMPORTANTE CERRAR EL ARCHIVO
      out.append("   " + name + "\n")
    }
    procesados
  }

  /**
   * Funcion = getNewName
   * Construye el nuevo nombre del archivo, concatenando el indice con el prefijo (si es necesario) y su nombre original
   */
	private def getNewName (i: Int, prefix: String, filename: String) : String = {
    if (folders.filter(_.name == prefix).map(_.show).head)
      getIndex(i,total) + "_" + prefix + "_" + filename
    else
      getIndex(i,total) + "_" + filename
  }


  /**
   * Funcion = getIndex
   * Construye el indice del archivo
   */
	private def getIndex(i: Int, max: Int) = "0" * (max.toString.length - i.toString.length) + i.toString

}
