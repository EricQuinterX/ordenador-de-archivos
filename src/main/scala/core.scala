package core_app

import main_app._
import config_app._

import com.typesafe.config.Config
import scala.swing._
import scala.util.{Try, Success, Failure}
import java.io.File


//EXCEPCIONES QUE PUEDEN EXISTIR
case class ErrorGetFolderName(msg: String) extends Exception(msg)
case class ErrorGetPriorityList(msg: String) extends Exception(msg)
case class ErrorCreateFolder(msg: String) extends Exception(msg)
case class ErrorCopyFiles(msg: String) extends Exception(msg)
case class Error(msg: String) extends Exception(msg)
case class ErrorCargarConfig(msg: String, cause: Throwable = null) extends Exception(msg, cause)

case class FileKeyword (params: Config) {
	val keyword: String = Try (params.getString("keyword")).getOrElse("")
	val folder : String = Try (params.getString("folder")).getOrElse("NEW")
}

case class Core (gui: UI) {

	var organizador : SistemaOrganizador  = _
	var codificador	: SistemaCodificador  = _
	var secuenciador: SistemaSecuenciador = _

	var folderResult: String = _

	def organizar() = {
		val resultado = Try {
			folderResult = ConfigFile.getFoldername
			gui.txtAreaOutput.text = ""
			blockComponents()
			val new_files = getListOfFiles(path + "\\NEW")
			val old_files = getListOfFiles(path + "\\OLD")
			val all_files = (old_files ::: new_files).sorted
			var i = 0 
			val configPriority : List[FileKeyword] = ConfigFile.getExecOrderList// :: FileKeyword(new Config)
			val PRIORITY_SIZE = configPriority.size

			organizador = SistemaOrganizador(all_files)

			for (i <- 0 until PRIORITY_SIZE) {
				organizador = i match { 
					// si termino de recorrer la lista, ejecuto el mismo proceso para los demas archivos de la carpeta NEW
					case PRIORITY_SIZE => organizador.start (".", "NEW") 
					case _ => organizador.start (configPriority(i).keyword, configPriority(i).folder)
				}
			}
			createAndCopyFiles()
			gui.txtAreaOutput.append(s"${organizador.index} archivos fueron ordenados \n")
		}
		resultado match {
			case Success(_) => unBlockComponents()
			case Failure(_) => 
				gui.txtAreaOutput.append("Hubo un Error al procesar el pasaje" )
				unBlockComponents()
		}
	}

	def encodeAnsi() {
		Dialog.showMessage(null,	"Aun no se desarrollo la funcionalidad de codificar a Ansi", title="Ansi")
	}

	private def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory)
      d.listFiles.filter(_.isFile).toList
    else
      List[File]()
	}

	private def path : String = gui.txtInputPath.text

	private def blockComponents(): Unit = {
		gui.btnProcesar.enabled = false
		gui.chkOrganizar.enabled = false
		gui.chkCodificar.enabled = false
	}

	private def unBlockComponents(): Unit = {
		gui.btnProcesar.enabled = true
		gui.chkOrganizar.enabled = true
		gui.chkCodificar.enabled = true
	}

	private def createAndCopyFiles() = {
		import sys.process._
		val cmd_mkdir = "cmd /C \"cd " + resolvePath(path) + " && mkdir " + folderResult + "\""
		cmd_mkdir.!
		for {
			(file, newNameFile) <- organizador.ordenados
		} yield {
			var cmd_copy = "cmd /C copy \"" + resolvePath(file.getPath) + "\" \"" + resolvePath(path) + "\\\\" + folderResult + "\\\\" + newNameFile + "\""
			cmd_copy.!
			gui.txtAreaOutput.append(s"Archivo $newNameFile fue creado\n")
		}
	}

	private def resolvePath(path: String): String = path.replaceAllLiterally("\\","\\\\")
}


case class SistemaOrganizador (restantes: List[File], 
											index: Int = 0, 
											ordenados: List[(File, String)] = Nil) {

	def start (word: String, foldername: String): SistemaOrganizador = {
		var lista: List[(File, String)] = Nil
		var	i = index
		def criteria(x: File) = x.getParentFile.getName == foldername && x.getName.contains(word)
		for {
			file <- restantes.filter (criteria(_))
		} yield {
			val p = genNewNameFile(i, foldername, file.getName )
			lista = lista :+ ((file, p ))
			i += 1
		}
		val procesados = restantes.filter(criteria(_))
		val paraProcesar = restantes filterNot procesados.contains
		copy(index = i, ordenados = ordenados ::: lista, restantes = paraProcesar)
	}

	private def genNewNameFile(i: Int, prefix: String, filename: String) = prefix match {
		case "OLD" => genStringIndex(i) + "_" + prefix + "_" + filename
		case "NEW" => genStringIndex(i) + "_" + filename // a los archivos del /NEW no muestro este prefijo
	}

	private def genStringIndex(i: Int) = String.valueOf(i).length() match {
		case 1 => "00" + i.toString()
		case 2 => "0" + i.toString()
		case _ => i.toString()
	}
}

case class SistemaCodificador (x: Int)
case class SistemaSecuenciador (x: Int)