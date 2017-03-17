package main_app

import core_app._

import scala.swing._
import scala.swing.event._
import Swing.{HStrut, VStrut, EmptyBorder}
import javax.swing.BorderFactory
import javax.swing.border._
import java.awt.{Font,Desktop}
import scala.util.Try
import java.io.File
import java.net.URI


object app {
  def main(args: Array[String]) {
    val ui = new UI
    ui.pack()
    ui.visible = true
  }
}

class UI extends MainFrame {

	val version = "1.0"
	val ui = this
	// Componentes
	val lbPath = new Label("Ruta:")
  //val lbNote = new Label("* : salida en ansi")
	val txtInputPath = new TextField(45)
	val chkOrganizar = new CheckBox("Organizar")
  // val chkSecuenciar = new CheckBox("Secuenciar")
  //val chkCodificar = new CheckBox("Codificar solo en Ansi")
	val txtAreaOutput = new TextArea(10,55){
		editable = false
		font = new Font("Console",Font.PLAIN,10)
	}
	val scrollTxtArea = new ScrollPane(txtAreaOutput){
		verticalScrollBarPolicy = ScrollPane.BarPolicy.AsNeeded
		border = BorderFactory.createTitledBorder("Detalles")
	}
	val btnProcesar = new Button("Organizar")
	val btnConfig = new Button("Config")
  val btnHelp = new Button("Ayuda")

  listenTo(btnProcesar, btnConfig, btnHelp)
  reactions += {
    case ButtonClicked(`btnProcesar`) => txtInputPath.text match {
      case "" => Dialog.showMessage(null,	"Ingrese la ruta por favor.", title="You pressed me")
      case _ => new Core(ui).start
    }
    case ButtonClicked(`btnConfig`) => Try(Desktop.getDesktop().edit(new File("application.conf"))).getOrElse(
      throw ErrorGuiApp("ErrorGuiApp: No se puede abrir el archivo")
    )
    case ButtonClicked(`btnHelp`) => Try(Desktop.getDesktop().browse(new URI("https://github.com/EricQuinterX/procesador-de-pasajes"))).getOrElse(
      throw ErrorGuiApp("ErrorGuiApp: No se puede abrir el vinculo al Repositorio")
    )
  }

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
      contents += HStrut(10)
      contents += btnProcesar
      contents += HStrut(100)
      contents += btnConfig
      contents += HStrut(5)
      contents += btnHelp
		}
		contents += scrollTxtArea
		border = EmptyBorder(10, 10, 10, 10)
  }
}
