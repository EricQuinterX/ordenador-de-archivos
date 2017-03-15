package modulo_app

import core_app._
import config_app._

import java.io.{File,FileInputStream,FileOutputStream}
import scala.swing.TextArea
import scala.util.Try

trait Modulo {

  def startFunction : Unit

  def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory)
      d.listFiles.filter(_.isFile).toList
    else List[File]()
  }

  def clearAndCreateDir(destino: File) : Boolean = {
    def deleteAll (file: File) : Boolean = {
      if (file.isDirectory){
        file.listFiles.foreach(deleteAll)
      }
      !(file.exists && !file.delete)
    }
    val delete = deleteAll(destino)
    val create = Try(destino.mkdir).getOrElse(false)
    delete && create
  }
}



class Organizador (ruta : String, out : TextArea, flags : Option[Funciones] = None) extends Modulo {
  var index: Int = 0
  var restantes : List[File] = Nil
  var ordenados : List[(File, String)] = Nil
  val carpeta_destino : String = Try(ConfigFile.getFolder(Organizador)).getOrElse(throw new ErrorGetFolderName("No se recupero el nombre de la carpeta desde el application.config"))

  def startFunction = {
    val configPriority : List[FileKeyword] = Try(ConfigFile.getPriorityList).getOrElse(throw new ErrorGetPriorityList("No se recupero la lista "))
    val PRIORITY_SIZE = configPriority.size
    var i : Int = 0
    restantes = (getListOfFiles(ruta +"\\OLD") ::: getListOfFiles(ruta +"\\NEW")).sorted
    for (i <- 0 to PRIORITY_SIZE) {
      if (i == PRIORITY_SIZE)
        filtrar(".", "NEW") // llego al ultimo, ejecuto los restantes
      else
        filtrar(configPriority(i).keyword, configPriority(i).folder)
    }
    createFiles(new File(ruta + "\\" + carpeta_destino))
    out.append(index + " archivos fueron organizados\n")
    flags.get match {
      case Funciones(_, true, _) => new Secuenciador(carpeta_destino, out, Some(flags)).startFunction
      case _ => new Codificador(carpeta_destino, out) // PASO A ANSI TODOS LOS ARCHIVOS
    }
  }

	private def filtrar (word: String, folder: String): Unit = {
		def criteria(x: File) = x.getParentFile.getName == folder && x.getName.contains(word)
    var filtrados: List[File] = Nil
    for (f <- restantes.filter(criteria).sortBy(_.getName){
      ordenados = ordenados.::((file, getNewName(index, folder, file.getName)))
      index += 1
      filtrados = filtrados :: f
    }
    restantes = restantes filterNot filtrados.contains
	}

  private def createFiles(dir: File) = {
    if (clearAndCreateDir(dir).unary_!) throw new ErrorCreateFolder("No se pudo limpiar la carpeta del organizador")
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

	private def getIndex(i: Int) = i.toString.length match {
		case 1 => "00" + i.toString
		case 2 => "0" + i.toString
		case _ => i.toString
	}
}


class Codificador (ruta: String, out: TextArea, algo: Option[Funciones] = None) extends Modulo {

  def startFunction : Unit = {
    import java.nio.file.{Paths,Files}
    import java.nio.ByteBuffer
    import java.nio.charset.Charset
    val dest = new File(ruta.getParent + "\\" + ConfigFile.getFolder(Codificador))
    if (clearAndCreateDir(dest).unary_!) throw new ErrorCreateFolder("No se limpio la carpeta ARCHIVOS_CODIFICADOS_ANSI")
    for (file <- getListOfFiles(ruta).sortBy(_.getName)){
      Path p = Paths.get(file.getPath);
      Path dest = Paths.get(dest.getPath)
      ByteBuffer bb = ByteBuffer.wrap(Files.readAllBytes(p));
      CharBuffer cb = Charset.forName("UTF-8").decode(bb);
      bb = Charset.forName("windows-1252").encode(cb);
      Files.write(dest, bb.array());
    }
  }
}

class Secuenciador (ruta: String, out: TextArea, algo: Option[Funciones] = None) extends Modulo {
  def startFunction : Unit = {

  }
}
