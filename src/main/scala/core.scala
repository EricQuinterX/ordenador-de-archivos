package core_app

import main_app._
import modulo_app._

import com.typesafe.config.Config
import scala.swing._
import scala.util.{Try, Success, Failure}
import java.io.File

case class Funciones(org : Boolean, sec : Boolean, cod : Boolean)

//EXCEPCIONES QUE PUEDEN EXISTIR
case class ErrorGuiApp(msg: String) extends Exception(msg)
case class ErrorCrearCarpeta(msg: String) extends Exception(msg)
case class ErrorCrearArchivo(msg: String) extends Exception(msg)
case class ErrorCopiarArchivos(msg: String) extends Exception(msg)
case class ErrorObtenerDatoConfig(msg: String) extends Exception(msg)
case class ErrorCargarConfiguracion(msg: String) extends Exception(msg)

class Core (gui: UI) {

	// val flags = Funciones(gui.chkOrganizar.selected, gui.chkSecuenciar.selected, gui.chkCodificar.selected)
	val flags = Funciones(true, false, false)

	def start = {
		try {
			blockComponents
			// val nodo : Modulo = flags match {
			// 	case Funciones(true, _, _) => new Organizador(path, gui.txtAreaOutput)
			// 	case Funciones(false, true, _) => new Secuenciador(path, gui.txtAreaOutput) // NO IMPLEMENTADO
			// 	case Funciones(false, false, true) => new Codificador(path, gui.txtAreaOutput) // NO APLICA ESTA FUNCIONALIDAD
			// }
			new Organizador(path, gui.txtAreaOutput).startFunction
		} catch {
				case ErrorCrearCarpeta(msg) 			 => setGuiLog("ErrorCrearCarpeta: " + msg)
				case ErrorCrearArchivo(msg) 			 => setGuiLog("ErrorGetFolderName: " + msg)
				case ErrorCopiarArchivos(msg) 		 => setGuiLog("ErrorCopiarArchivos: " + msg)
				case ErrorObtenerDatoConfig(msg)	 => setGuiLog("ErrorObtenerDatoConfig: " + msg)
				case ErrorCargarConfiguracion(msg) => setGuiLog("ErrorCargarConfiguracion: " + msg)
		} finally {
			unBlockComponents
		}
	}

	private def path : String = gui.txtInputPath.text

	private def setGuiLog (s: String) : Unit = gui.txtAreaOutput.append(s +"\n")

	// private def resolvePath(path: String): String = path.replaceAllLiterally("\\","\\\\")

	private def blockComponents(): Unit = {
		gui.btnProcesar.enabled = false
		// gui.chkOrganizar.enabled = false
		// gui.chkSecuenciar.enabled = false
		// gui.chkCodificar.enabled = false
		gui.txtAreaOutput.text = ""
	}

	private def unBlockComponents(): Unit = {
		gui.btnProcesar.enabled = true
		// gui.chkOrganizar.enabled = true
		// gui.chkSecuenciar.enabled = true
		// gui.chkCodificar.enabled = true
	}
}
