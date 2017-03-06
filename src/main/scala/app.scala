package app_procesador

import com.typesafe.config.ConfigFactory
import scala.swing._
import scala.swing.event._
import Swing.{HStrut, VStrut, EmptyBorder}
import scala.util.Try
import java.io.File
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.border._

object app {
  def main(args: Array[String]) {
    val ui = new UI
    ui.pack()
    ui.visible = true
  }
}

case class ConfigFile() {
	def getVersion : String = {
		Try(ConfigFactory.load("application.config").getString("foldername")) match {
			case Success(s) => s
			case Failure(_) => throw new ErrorCargarConfig("Hubo error al cargar la version de la configuracion")
		}
	}
	def getFoldername : String = {
		val name_folder = Try(ConfigFactory.load("application.config").getString("foldername")) 
		name_folder match {
			case Success(s) = s
			case Failure(_) = throw new ErrorCargarConfig("Error cargar nombre de la carpeta del config")
		}
	}
	def getExecOrderList : List[String] = {
		import scala.collection.JavaConversions._
		val lista_orden = Try (ConfigFactory.load("application.config").getStringList("execOrder").toList)
		lista_orden match {
			case Success(xs) = xs
			case Failure(_) = throw new ErrorCargarConfig("Error cargar orden de ejecucion del config")	
		}
	}
}

class UI extends MainFrame {

	val version : String = ConfigFile.getVersion
	// Componentes
	val ui = this
	val lbPath = new Label("Ruta:")
	val txtInputPath = new TextField(45)
	val chkOrdenar = new CheckBox("Ordenar pasaje")
	val chkCodificar = new CheckBox("Codificar en ANSI")
	val txtAreaOutput = new TextArea(10,55){
		editable = false
		font = new Font("Console",Font.PLAIN,10)
	}	
	val scrollTxtArea = new ScrollPane(txtAreaOutput){
		verticalScrollBarPolicy = ScrollPane.BarPolicy.AsNeeded
		border = BorderFactory.createTitledBorder("Detalles")
	}
	val btnProcesar = new Button("Aplicar"){
		listenTo(this)
		reactions += {
			case ButtonClicked(_) => txtInputPath.text match {
				case "" => Dialog.showMessage(null,	"Ingrese la ruta por favor.", title="You pressed me")
				case _ => Core(ui).procesar
			}
		}
	}
	val btnHelp = new Button("Ayuda")
	val btnConfig = new Button("Config")
	
	// Propiedades
  title = s"Procesador de Pasajes Engage v$version"
  resizable = false
  peer.setLocationRelativeTo(null)

  //Contenidos
  contents = new BoxPanel(Orientation.Vertical) {
		contents += new FlowPanel(FlowPanel.Alignment.Left)(){
			contents += lbPath
			contents += HStrut(4)
			contents += txtInputPath
		}
		contents += new FlowPanel(FlowPanel.Alignment.Left)(){
			border = BorderFactory.createTitledBorder("Funciones")
			contents += chkOrdenar
			contents += HStrut(5)
			contents += chkCodificar
		}
		contents += new FlowPanel(FlowPanel.Alignment.Left)(){
			contents += scrollTxtArea
			contents += HStrut(5)
			contents += new BoxPanel(Orientation.Vertical){
				contents += btnProcesar
				contents += VStrut(5)
				contents += btnConfig
				contents += VStrut(5)
				contents += btnHelp
			}
		}
		border = EmptyBorder(10, 10, 10, 10)
  }
}

case class ErrorCargarConfig(msg: String, cause: Throwable = null) extends Exception(msg, cause)

case class FileKeyword (word: String, folder: String)

case class Core (gui: UI) {

	var filesystem 	 : FileSystemFilter = _

	/*
	val priority: Map[Int, FileKeyword] = Map (
		0 -> FileKeyword(".Deletes.","OLD"),
		1 -> FileKeyword(".Deletes.","NEW"),
		2 -> FileKeyword(".Inserts.","NEW"),
		3 -> FileKeyword("CREATE_TABLE","NEW"),
		4 -> FileKeyword("CREATE_INDEX","NEW"),
		5 -> FileKeyword("ALTER_TABLE","NEW"),
		6 -> FileKeyword("ALTER_INDEX","NEW"),
		7 -> FileKeyword("FUNCITON","NEW"),
		8 -> FileKeyword("PROCEDURE","NEW"),
		9 -> FileKeyword("PACKAGE","NEW")
	)*/

	def procesar() = {
		Try {
			// cada vez que haga clic en el boton se debe limpiar el Detalle
			folderResult = getFoldername()
			execOrderList = getExecOrderList()
			gui.txtAreaOutput.text = ""
			blockComponents()
			val new_files = getListOfFiles(path + "\\NEW")
			val old_files = getListOfFiles(path + "\\OLD")
			val all_files = (old_files ::: new_files).sorted
			var i = 0
			val configPriority = ConfigFile.getExecOrderList
			
			filesystem = FileSystemFilter(all_files, gui.txtAreaOutput)

			for (i <- 0 until priority.size)
				filesystem = filesystem.start( priority(i).word, priority(i).folder )
			createAndCopyFiles()
			gui.txtAreaOutput.append(s"${filesystem.index.toString} fueron acomodados y enumerados \n")
		} getOrElse {
			gui.txtAreaOutput.append("Hubo un Error" )
			unBlockComponents()
		}
	}

	private def path : String = gui.txtInputPath.text

	private def blockComponents(): Unit = {
		gui.btnProcesar.enabled = false
		gui.chkOrdenar.enabled = false
		gui.chkCodificar.enabled = false
	}

	private def unBlockComponents(): Unit = {
		gui.btnProcesar.enabled = true
		gui.chkOrdenar.enabled = true
		gui.chkCodificar.enabled = true
	}

	private def createAndCopyFiles() = {
		import sys.process._
		val cmd_mkdir = "cmd /C \"cd " + resolvePath(path) + " && mkdir " + folderResult + "\""
		cmd_mkdir.!
		for {
			(file, newNameFile) <- filesystem.filemap
		} yield {
			var cmd_copy = "cmd /C copy \"" + resolvePath(file.getPath) + "\" \"" + resolvePath(path) + "\\\\" + folderResult + "\\\\" + newNameFile + "\""
			cmd_copy.!
			gui.txtAreaOutput.append(s"Archivo $newNameFile fue creado\n")
		}
	}

	private def resolvePath(path: String): String = path.replaceAllLiterally("\\","\\\\")

	private def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory)
      d.listFiles.filter(_.isFile).toList
    else
      List[File]()
	}
}


case class FileSystemFilter(remainingFiles: List[File], 
														componente: TextArea, 
														index: Int = 0, 
														filemap: List[(File, String)] = Nil) {

	def start (word: String, foldername: String): FileSystemFilter = {
		var lista: List[(File, String)] = Nil
		var	i = index
		for {
			file <- remainingFiles.filter( _.getParentFile.getName == foldername )
			if file.getName.contains(word)
		} yield {
			val p = genNewNameFile(i, foldername, file.getName )
			componente.append(p + "\n")
			lista = lista :+ ((file, p ))
			i += 1
		}
		var resto = remainingFiles.filter( e => !e.getName.contains(word) || !(e.getParent == foldername) )
		copy(index = i, filemap = filemap ::: lista, remainingFiles = resto)
	}

	private def genNewNameFile(i: Int, prefix: String, filename: String) = genStringIndex(i) + "_" + prefix + "_" + filename

	private def genStringIndex(i: Int) = String.valueOf(i).length() match {
		case 1 => "00" + i.toString()
		case 2 => "0" + i.toString()
		case _ => i.toString()
	}
}