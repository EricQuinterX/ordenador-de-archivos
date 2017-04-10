package main_app

import core_app._
import config_app._

import scala.swing._
import scala.swing.event._
import Swing.{HStrut, VStrut, EmptyBorder}
import javax.swing.{BorderFactory, ImageIcon}
import javax.swing.border._
import java.awt.{Font, Desktop, Color}
import scala.util.Try
import java.io.File
import java.net.URI


object app {
  def main(args: Array[String]) {
    val ui = new UI
    ui.pack()
    ui.peer.setLocationRelativeTo(null)
    ui.visible = true
  }
}


class UI extends MainFrame {

	val version = "1.0"
	val ui = this

	// Componentes
	val lbPath = new Label("Ruta:")
	val txtInputPath = new TextField(45)
  val lbDestino = new Label("Destino:")
  val txtDestino = new TextField(20){
    font = new Font("Console",Font.PLAIN,12)
    foreground = Color.gray
    text = ConfigFile.getFolder()
  }
	val txtAreaOutput = new TextArea(15,54){
		editable = false
		font = new Font("Console",Font.PLAIN,11)
	}
	val scrollTxtArea = new ScrollPane(txtAreaOutput){
		verticalScrollBarPolicy = ScrollPane.BarPolicy.AsNeeded
		border = BorderFactory.createTitledBorder("Detalles")
	}
	val btnProcesar = new Button("Organizar")
	val btnConfig   = new Button("Config")
  val btnHelp     = new Button("Ayuda")

  // Eventos
  listenTo(btnProcesar, btnConfig, btnHelp, txtDestino)
  reactions += {
    case ButtonClicked(`btnProcesar`) => txtInputPath.text match {
      case "" => Dialog.showMessage(null,	"Ingrese la ruta por favor.", title="Advertencia")
      case _ => new Core(ui).start
    }
    case ButtonClicked(`btnConfig`) =>
      val path = new File("application.config").getPath
      Try(Runtime.getRuntime.exec("cmd.exe /C start " + path)).getOrElse(
        txtAreaOutput.append("ErrorCargarConfiguracion: No se puede abrir el archivo."))
    case ButtonClicked(`btnHelp`) => Try(Desktop.getDesktop().browse(new URI(ConfigFile.getLinkHelp))).getOrElse(
      txtAreaOutput.append("ErrorGuiApp: No se puede abrir el vinculo al Repositorio\n")
    )
    case EditDone(`txtDestino`) => if (txtDestino.text.isEmpty) txtDestino.text = ConfigFile.getFolder()
  }

	// Propiedades
  title = s"Ordenador de Archivos v$version"
  resizable = false
  iconImage = new ImageIcon(getClass.getResource("/icon_app.png")).getImage

  //Contenidos
  contents = new BoxPanel(Orientation.Vertical) {
		contents += new FlowPanel(FlowPanel.Alignment.Left)(){
			contents += lbPath
			contents += HStrut(4)
			contents += txtInputPath
		}
    contents += new BoxPanel(Orientation.Horizontal){
      contents += new FlowPanel(FlowPanel.Alignment.Left)(){
        contents += lbDestino
        contents += HStrut(3)
        contents += txtDestino
      }
      contents += new FlowPanel(FlowPanel.Alignment.Right)(){
        contents += btnProcesar
        contents += HStrut(5)
        contents += btnConfig
        contents += HStrut(5)
        contents += btnHelp
      }
    }
		contents += scrollTxtArea
		border = EmptyBorder(10, 10, 10, 10)
  }
}
