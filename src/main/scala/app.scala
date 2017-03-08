package app_procesador

import com.typesafe.config.{ConfigFactory, Config}
import scala.swing._
import scala.swing.event._
import Swing.{HStrut, VStrut, EmptyBorder}
import scala.util.{Try, Success, Failure}
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
				case _ =>
					if (chkOrdenar.selected) Core(ui).ordenar
					if (chkCodificar.selected) Core(ui).encodeAnsi
					if (!chkOrdenar.selected && !chkCodificar.selected) Dialog.showMessage(null, "Elija una funcion", title="Advertencia")
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

case class FileKeyword (params: Config) {
	val keyword: String = Try (params.getString("keyword")).getOrElse("")
	val folder : String = Try (params.getString("folder")).getOrElse("NEW")
}

object ConfigFile {

	val configFile = ConfigFactory.parseFile(new File("application.conf")).getConfig("app")
	val config = ConfigFactory.load(configFile)

	def getVersion : String = Try(config.getString("version")) match {
		case Success(s) => s
		case Failure(_) => throw new ErrorCargarConfig("Hubo error al cargar la version de la configuracion")
	}

	def getFoldername : String = Try(config.getString("folder")) match {
		case Success(s) => s
		case Failure(_) => throw new ErrorCargarConfig("Error cargar nombre de la carpeta del config")
	}

	def getExecOrderList : List[FileKeyword] = {
		import scala.collection.JavaConversions._
		val lista_orden = Try ( config.getConfigList("priority").map(FileKeyword(_)).toList )
		lista_orden match {
			case Success(xs) => xs
			case Failure(_) => throw new ErrorCargarConfig("Error cargar orden de ejecucion del config")	
		}
	}
}

case class Core (gui: UI) {

	var filesystem  : FileSystemFilter = _
	var folderResult: String = _

	def ordenar() = {
		val resultado = Try {
			folderResult = ConfigFile.getFoldername
			gui.txtAreaOutput.text = ""
			blockComponents()
			val new_files = getListOfFiles(path + "\\NEW")
			val old_files = getListOfFiles(path + "\\OLD")
			val all_files = (old_files ::: new_files).sorted
			var i = 0
			val configPriority = ConfigFile.getExecOrderList// :: FileKeyword(new Config)
			
			filesystem = FileSystemFilter(all_files)

			for (i <- 0 until configPriority.size)
				filesystem = filesystem.start( configPriority(i).keyword, configPriority(i).folder )
			// proceso el resto
			filesystem = filesystem.start(".Sql", "NEW")

			createAndCopyFiles()
			gui.txtAreaOutput.append(s"${filesystem.index.toString} fueron acomodados y enumerados \n")
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
}


case class FileSystemFilter(remainingFiles: List[File], 
														index: Int = 0, 
														filemap: List[(File, String)] = Nil) {

	def start (word: String, foldername: String): FileSystemFilter = {
		var lista: List[(File, String)] = Nil
		var	i = index
		def criteria(x: File) = x.getParentFile.getName == foldername && x.getName.contains(word)
		for {
			file <- remainingFiles.filter( criteria(_))
		} yield {
			val p = genNewNameFile(i, foldername, file.getName )
			lista = lista :+ ((file, p ))
			i += 1
		}
		val procesados = remainingFiles.filter(criteria(_))
		println(s"Procesados con '$word': ${procesados.size}")
		var paraProcesar = remainingFiles filterNot procesados.contains
		println(s"Restantes para procesar: ${paraProcesar.size}")
		copy(index = i, filemap = filemap ::: lista, remainingFiles = paraProcesar)
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
/*
case class ValidationProcess (amountProcessed: Int) {
	val countOldFiles = getListOfFiles(path + "\\OLD").size
	val countNewFiles = getListOfFiles(path + "\\NEW").size
	val total = countNewFiles + countOldFiles
	amountProcessed == total
}*/