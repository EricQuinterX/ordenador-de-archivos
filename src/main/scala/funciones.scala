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

  def getFilesFrom(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory){
      val tipo = ConfigFile.getFileType.toUpperCase
      val query = if (tipo.isEmpty) "." else s".$tipo"
      d.listFiles.filter(_.isFile).toList.filter(_.getName.toUpperCase.contains(query))
    }
    else List[File]()
  }

  def clearAndCreateDir(destino: File) : Boolean = {
    def deleteAll (file: File) : Boolean = {
      if (file.isDirectory)
        file.listFiles.foreach(deleteAll)
      !(file.exists && !file.delete)
    }
    deleteAll(destino) && destino.mkdir
  }
}



class Ordenador (ruta : String, out : TextArea) extends Modulo {

  var index: Int = 0
  var restantes : List[File] = Nil
  var ordenados : List[(File, String)] = Nil
  var total : Int = 0
  val folders : List[Folder] = ConfigFile.getFolderList
  val carpeta_destino : String = ConfigFile.getFolder()

  def startFunction = {
    // Cada vez que ejecuta esta funcionalidad, recupero la lista de filtros si es que lo modifico en tiempo de ejecucion
    val filtros : List[FileKeyword] = ConfigFile.getFilters
    val tamFiltros = filtros.size
    // La secuencia empieza siempre en 000
    var i : Int = 0
    // Meto los archivos de las carpetas en una lista
    restantes = getFiles(ruta).sorted
    total = restantes.size
    // Recorro la lista de filtros de arriba hacia abajo
    for (i <- 0 to tamFiltros) {
      if (i == tamFiltros)
        // llego al ultimo, ejecuto los restantes
        filtrar(".", "", ".*")
      else
        // Paso los valores de cada filtro de la lista, para que el metodo filtro filtre con esos valores
        filtrar(filtros(i).keyword, filtros(i).folder, filtros(i).match_pos)
    }
    // Copio los archivos de las 2 carpetas a un nueva en base a los nuevos nombres de la lista nueva
    createFiles(new File(ruta + "\\" + carpeta_destino))
    out.append(s"($index/$total) archivos fueron organizados\n")
    // Verifico si se organizaron todos los archivos de las carpetas
    val finalMsg = if (total == index) "La operacion se completo satisfactoriamente" else "No se organizaron todos los archivos"
    out.append(s"$finalMsg\n")
  }


  private def getFiles(r: String): List[File] = ConfigFile.getFolderList.flatMap(folder => getFilesFrom(r + "\\" + folder.name))


	private def filtrar (word: String, folder: String, pos: String): Unit = {
    // Funcion que compara el nombre de la carpeta "NEW/OLD", el patron "keyword" y la posicion "INICIO/MEDIO"
    def criteria(x: File) : Boolean = {
      val regex = word.replaceAllLiterally("***", ".*") + ".*"
      val patron = Pattern.compile(regex)
      val matcher = patron.matcher(x.getName)
      if (matcher.find) {
        val p = if (matcher.start == 0) "INICIO" else "MEDIO"
        x.getParentFile.getName == folder && p == pos
      } else false
    }
    // La lista de los archivos filtrados para luego sacar una diferencia
    var filtrados: List[File] = Nil
    for (f <- restantes.filter(criteria).sortBy(_.getName)){
      // Inserto en la lista el nuevo nombre del archivo
      ordenados = (f, getNewName(index, folder, f.getName)) ::  ordenados
      index += 1
      filtrados = f :: filtrados
    }
    // Hago la diferencia de los restantes menos los filtrados
    restantes = restantes filterNot filtrados.contains
	}


  private def createFiles(dir: File) = {
    Try(clearAndCreateDir(dir)) match {
      case Success(s) => if (!s) throw new ErrorCrearCarpeta("No se limpio la carpeta destino")
      case Failure(e) => out.append(e.getMessage)
    }
    out.append(s"Directorio ${dir.getName} creado\n")
    out.append(s"Archivos creados:\n")
    // CREO LOS ARCHIVOS SQL CON SUS RESPECTIVOS NOMBRES
    for ((src, name) <- ordenados.sortBy(_._2)){
      val destin = new File(dir.getPath + "\\" + name)
      val fos = new FileOutputStream(destin)
      fos.getChannel.transferFrom(new FileInputStream(src).getChannel, 0, Long.MaxValue)
      fos.close // IMPORTANTE CERRAR EL ARCHIVO
      out.append("  " + name + "\n")
    }
  }

	private def getNewName (i: Int, prefix: String, filename: String) = {
    if (folders.filter(_.name == prefix).map(_.show).head)
      getIndex(i,total) + "_" + prefix + "_" + filename
    else
      getIndex(i,total) + "_" + filename
  }

  // Creo el indice de los archivos
	private def getIndex(i: Int, max: Int) = "0" * (max.toString.length - i.toString.length) + i.toString

}
