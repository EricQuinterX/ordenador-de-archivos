package core_app

import main_app._
import modulo_app._
import errores_app._

import com.typesafe.config.Config
import scala.swing._
import scala.util.{Try, Success, Failure}
import java.io.File


class Core (gui: UI) {

	def start = {
		try {
			blockComponents
			new Ordenador(gui.txtInputPath.text, gui.txtAreaOutput).startFunction
		} catch {
				case ErrorCrearCarpeta(msg) 			 => setGuiLog("ErrorCrearCarpeta: " + msg)
				case ErrorCrearArchivo(msg) 			 => setGuiLog("ErrorGetFolderName: " + msg)
				case ErrorCopiarArchivos(msg) 		 => setGuiLog("ErrorCopiarArchivos: " + msg)
				case ErrorObtenerDatoVacio(msg)    => setGuiLog("ErrorObtenerDatoVacio: " + msg)
				case ErrorObtenerDatoConfig(msg)	 => setGuiLog("ErrorObtenerDatoConfig: " + msg)
				case ErrorCargarConfiguracion(msg) => setGuiLog("ErrorCargarConfiguracion: " + msg)
		} finally {
			unBlockComponents
		}
	}

	private def setGuiLog (s: String) : Unit = gui.txtAreaOutput.append(s +"\n")

	private def blockComponents(): Unit = {
		gui.btnProcesar.enabled = false
		gui.txtAreaOutput.text = ""
	}

	private def unBlockComponents(): Unit = {
		gui.btnProcesar.enabled = true
	}
}
