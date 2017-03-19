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
  val carpeta_destino : String = ConfigFile.getFolder()

  def startFunction = {
    // Cada vez que ejecuta esta funcionalidad, recupero la lista de filtros si es que lo modifico en tiempo de ejecucion
    val filtros : List[FileKeyword] = ConfigFile.getFilters
    val `tamFiltros` = filtros.size
    // La secuencia empieza siempre en 000
    var i : Int = 0
    // Meto los archivos de las 2 carpeta en una lista
    restantes = getFiles.sorted
    // Recorro la lista de filtros de arriba hacia abajo
    for (i <- 0 to `tamFiltros`) {
      if (i == `tamFiltros`)
        // llego al ultimo, ejecuto los restantes
        filtrar(".", "", ".*")
      else
        // Paso los valores de cada filtro de la lista, para que el metodo filtro filtre con esos valores
        filtrar(filtros(i).keyword, filtros(i).folder, filtros(i).match_pos)
    }
    // Copio los archivos de las 2 carpetas a un nueva en base a los nuevos nombres de la lista nueva
    createFiles(new File(ruta + "\\" + carpeta_destino))
    out.append(index + " archivos fueron organizados\n")
  }

  private def getFiles(r: String): List[File] = ConfigFile.getFolderList.flatMap(getFilesFrom(r + "\\" + _.name))

	private def filtrar (word: String, folder: String, pos: String): Unit = {
    // Funcion que compara el nombre de la carpeta "NEW/OLD", el patron "keyword" y la posicion "INICIO/MEDIO"
    def criteria(x: File) : Boolean = {
      val sameFolder = x.getParentFile.getName == folder
      val regex = word.replaceAllLiterally("***", ".*") + ".*"
      val patron = Pattern.compile(regex)
      val matcher = patron.matcher(x.getName)
      if (matcher.find) {
        val position = matcher.start match {
          case 0 => "INICIO"
          case n => "MEDIO"
        }
        sameFolder && position == pos
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
      case Success(s) => if (!s) throw new ErrorCrearCarpeta("No se limpio la carpeta ARCHIVOS_ORGANIZADOS")
      case Failure(e) => out.append(e.getMessage)
    }
    // CREO LOS ARCHIVOS SQL CON SUS RESPECTIVOS NOMBRES
    for ((src, name) <- ordenados.sortBy(_._2)){
      val destin = new File(dir.getPath + "\\" + name)
      val fos = new FileOutputStream(destin)
      fos.getChannel.transferFrom(new FileInputStream(src).getChannel, 0, Long.MaxValue)
      fos.close // IMPORTANTE CERRAR EL ARCHIVO
      out.append("Archivo creado: " + name + "\n")
    }
  }

	private def getNewName (i: Int, prefix: String, filename: String) = prefix match {
		case "OLD" => getIndex(i) + "_" + prefix + "_" + filename
		case "NEW" => getIndex(i) + "_" + filename // a los archivos del /NEW no muestro este prefijo
	}

  // Creo el indice de los archivos
	private def getIndex(i: Int) = i.toString.length match {
		case 1 => "00" + i.toString
		case 2 => "0" + i.toString
		case _ => i.toString
	}
}
