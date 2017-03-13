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
  var restantes : List[File] = _
  var ordenados : List[(File, String)] = _
  val carpeta_destino : String = Try(ConfigFile.getFolder(Organizador)).getOrElse(throw new ErrorGetFolderName("No se recupero el nombre de la carpeta desde el application.config"))

  def startFunction = {
    val configPriority = Try(ConfigFile.getPriorityList).getOrElse(throw new ErrorGetPriorityList("No se recupero la lista "))
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
			ordenados = ordenados :+ ((file, getNewName(index, folder, file.getName )))
			index += 1
      file
    }
    restantes = restantes filterNot filtrados.contains
	}

  private def createFiles = {
    var newFilesList : List[File] = Nil
    flags match {
      case Funciones(_, true, _) => new Secuenciador(flags, destino_organizador, out)
      case _ =>
        if (new File(carpeta_destino).mkdir.unary_!) throw new ErrorCreateFolder("No se creo la carpeta del organizador")
        for ((src, name) <- ordenados){
          val destin = new File(s"ruta\\$carpeta_destino\\$name")
          newFilesList = newFilesList :: destin
          new FileOutputStream(destin).getChannel.transferFrom(new FileInputStream(src).getChannel, 0, Long.MaxValue)
          out.append(s"Archivo creado: $name")
        }
        // ordenados.foreach { ((src, name)) =>
        //   val destin = new File(s"ruta\\$carpeta_destino\\$name")
        //   newFilesList = newFilesList ::: destin.toList
        //   new FileOutputStream(destin).getChannel.transferFrom(new FileInputStream(src).getChannel, 0, Long.MaxValue)
        //   out.append(s"Archivo creado: $name")
        // }
    }
  }

  // var destin = new OutputStreamWriter(new FileOutputStream(new File(s"${src.getParent}\\$name")), "Cp1252")
  // var origin = new FileInputStream(src).getChannel;
  // new FileOutputStream(destin).getChannel.transferFrom(origin, 0, Long.MaxValue)

  // val src = new File(args(0))
  // val dest = new File(args(1))
  // var file_output_stream = new FileOutputStream(dest).getChannel()
  // file_output.transferFrom(new FileInputStream(src).getChannel, 0, Long.MaxValue)

  // OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, true), "windows-1252");
  // writer.append(textBody);
  // writer.close();

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

class Codificador (algo: Funciones, r: String, out: TextArea) extends Modulo {
  def startFunction : Unit = {}
}

class Secuenciador (algo: Funciones, r: String, out: TextArea) extends Modulo {
  def startFunction : Unit = {}
}
