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
}

class Organizador ( flags : Funciones, ruta : String, out : TextArea) extends Modulo {
  var index: Int = 0
  var restantes : List[File] = Nil
  var ordenados : List[(File, String)] = Nil
  val carpeta_destino : String = Try(ConfigFile.getFolder(Organizador)).getOrElse(throw new ErrorGetFolderName("No se recupero el nombre de la carpeta desde el application.config"))

  def startFunction = {
    println("-------------------------")
    val configPriority : List[FileKeyword] = Try(ConfigFile.getPriorityList).getOrElse(throw new ErrorGetPriorityList("No se recupero la lista "))
    val PRIORITY_SIZE = configPriority.size
    var i : Int = 0
    restantes = (getListOfFiles(ruta +"\\OLD") ::: getListOfFiles(ruta +"\\NEW")).sorted
    for (i <- 0 to PRIORITY_SIZE) {
      i match {
        case PRIORITY_SIZE => filtrar(".", "NEW") // llego al ultimo, ejecuto los restantes
        case _ => filtrar(configPriority(i).keyword, configPriority(i).folder)
      }
    }
    createFiles
    out.append(s"$index archivos fueron organizados\n")
  }

	private def filtrar (word: String, folder: String): Unit = {
		def criteria(x: File) = x.getParentFile.getName == folder && x.getName.contains(word)
    val filtrados = restantes.filter(criteria(_)).map{ file =>
			ordenados = ordenados.::((file, getNewName(index, folder, file.getName)))
			index += 1
      file
    }
    restantes = restantes filterNot filtrados.contains
	}

  private def createFiles = flags match {
    case Funciones(_, true, _) => new Secuenciador(flags, carpeta_destino, out)
    case _ =>
      var newFilesList : List[File] = Nil
      if (cleanDirAndCreate(new File(ruta + "\\" + carpeta_destino)).unary_!) throw new ErrorCreateFolder("No se pudo limpiar la carpeta del organizador")
      // CREO LOS ARCHIVOS SQL CON SUS RESPECTIVOS NOMBRES
      for ((src, name) <- ordenados.sortBy(_._2)){
        val destin = new File(s"$ruta\\$carpeta_destino\\$name")
        newFilesList = newFilesList.::(destin)
        val fos = new FileOutputStream(destin)
        fos.getChannel.transferFrom(new FileInputStream(src).getChannel, 0, Long.MaxValue)
        fos.close // IMPORTANTE CERRAR EL ARCHIVO
        out.append(s"Archivo creado: $name\n")
      }
      // CONVIENTO A ANSI TODOS LOS ARCHIVOS
      import java.nio.file.{Paths,Files}
      import java.nio.ByteBuffer
      import java.nio.charset.Charset

      for (f <- newFilesList.sortBy(_._2)){
        Path p = Paths.get(f.getPath);
        ByteBuffer bb = ByteBuffer.wrap(Files.readAllBytes(p));
        CharBuffer cb = Charset.forName("UTF-8").decode(bb);
        bb = Charset.forName("windows-1252").encode(cb);
        Files.write(p, bb.array());

      }

  }

  private def create

	private def getNewName (i: Int, prefix: String, filename: String) = prefix match {
		case "OLD" => getIndex(i) + "_" + prefix + "_" + filename
		case "NEW" => getIndex(i) + "_" + filename // a los archivos del /NEW no muestro este prefijo
	}

	private def getIndex(i: Int) = i.toString.length match {
		case 1 => "00" + i.toString
		case 2 => "0" + i.toString
		case _ => i.toString
	}

  private def cleanDirAndCreate(destino: File) : Boolean = {
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

  private def convertToAnsi(file: File){
    import java.nio.file.{Paths,Files}
    import java.nio.ByteBuffer
    import java.nio.charset.Charset
    Path p = Paths.get("file.txt");
    ByteBuffer bb = ByteBuffer.wrap(Files.readAllBytes(p));
    CharBuffer cb = Charset.forName("windows-1252").decode(bb);
    bb = Charset.forName("UTF-8").encode(cb);
    Files.write(p, bb.array());
  }
}

class Codificador (algo: Funciones, r: String, out: TextArea) extends Modulo {
  def startFunction : Unit = {}
}

class Secuenciador (algo: Funciones, r: String, out: TextArea) extends Modulo {
  def startFunction : Unit = {}
}
