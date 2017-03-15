package core_app

import main_app._
import modulo_app._

import com.typesafe.config.Config
import scala.swing._
import scala.util.{Try, Success, Failure}
import java.io.File

case class Funciones(org : Boolean, sec : Boolean, cod : Boolean)

//EXCEPCIONES QUE PUEDEN EXISTIR
case class ErrorGetFolderName(msg: String) extends Exception(msg)
case class ErrorGetPriorityList(msg: String) extends Exception(msg)
case class ErrorCreateFolder(msg: String) extends Exception(msg)
case class ErrorCopyFiles(msg: String) extends Exception(msg)
case class ErrorCargarConfig(msg: String) extends Exception(msg)

class Core (gui: UI) {

	val flags = Funciones(gui.chkOrganizar.selected, gui.chkSecuenciar.selected, gui.chkCodificar.selected)

	def start = {
		try {
			blockComponents
			val nodo : Modulo = flags match {
				case Funciones(true, _, _) => new Organizador(path, gui.txtAreaOutput, Some(flags))
				case Funciones(false, true, _) => new Secuenciador(path, gui.txtAreaOutput)
				case Funciones(false, false, true) => new Codificador(path, gui.txtAreaOutput)
			}
			nodo.startFunction
		} catch {
				case e : ErrorCreateFolder => setGuiLog("ErrorCreateFolder: " + e.getMessage)
				case e : ErrorGetPriorityList => setGuiLog("ErrorGetPriorityList: " + e.getMessage)
				case e : ErrorGetFolderName => setGuiLog("ErrorGetFolderName: " + e.getMessage)
		} finally {
			unBlockComponents
		}
	}

	private def path : String = gui.txtInputPath.text

	private def setGuiLog (s: String) : Unit = gui.txtAreaOutput.append(s +"\n")

	private def resolvePath(path: String): String = path.replaceAllLiterally("\\","\\\\")

	private def blockComponents(): Unit = {
		gui.btnProcesar.enabled = false
		gui.chkOrganizar.enabled = false
		gui.chkSecuenciar.enabled = false
		gui.chkCodificar.enabled = false
		gui.txtAreaOutput.text = ""
	}

	private def unBlockComponents(): Unit = {
		gui.btnProcesar.enabled = true
		gui.chkOrganizar.enabled = true
		gui.chkSecuenciar.enabled = true
		gui.chkCodificar.enabled = true
	}
}
