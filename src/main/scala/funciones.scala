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
        filtrar(".", "", "INICIO")
      else
        // Paso los valores de cada filtro de la lista, para que el metodo filtro filtre con esos valores
        filtrar(filtros(i).keyword, filtros(i).folder, filtros(i).match_pos)
    }

    val dest_folder = new File(ruta + "\\" + carpeta_destino)

    Try(deleteFolder(dest_folder)) match {
      case Success(s) if (!s) => throw ErrorCrearCarpeta("No se pudo limpiar la carpeta destino")
      case Failure(e) => out.append(e.getMessage)
      case Success(_) =>
        val p = createFiles(dest_folder)
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


  private def getFiles(r: String): List[File] = ConfigFile.getFolderList.flatMap(folder => getFilesFrom(r + "\\" + folder.name))


	private def filtrar (word: String, folder: String, pos: String): Unit = {
    // Funcion que veriica la posicion, el patron y el nombre de la carpeta origen con el destino
    def criteria(x: File) : Boolean = {
      // En caso que haya recorrido todos los filtros, los demas los dejo cumplir la condicion.
      if (folder.isEmpty) return true
      // Cada vez que encuentre un *** dentro del keyword, lo reemplazo con .*
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
      ordenados = (f, getNewName(index, f.getParentFile.getName, f.getName)) ::  ordenados
      index += 1
      filtrados = f :: filtrados
    }
    // Hago la diferencia de los restantes menos los filtrados
    restantes = restantes filterNot filtrados.contains
	}

  private def createFiles(dir: File) : Int = {
    var procesados = 0
    if (dir.mkdir)
      out.append(s"Directorio ${dir.getName} creado.\n")
    else
      throw ErrorCrearCarpeta("No se limpio la carpeta destino")

    out.append(s"Archivos creados:\n")
    // CREO LOS ARCHIVOS CON SUS RESPECTIVOS NOMBRES
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

	private def getNewName (i: Int, prefix: String, filename: String) = {
    // Verifico si debo mostrar el nombre de carpeta de origen de archivo, contenido en su nombre.
    if (folders.filter(_.name == prefix).map(_.show).head)
      getIndex(i,total) + "_" + prefix + "_" + filename
    else
      getIndex(i,total) + "_" + filename
  }

  // Creo el indice de los archivos
	private def getIndex(i: Int, max: Int) = "0" * (max.toString.length - i.toString.length) + i.toString

}
