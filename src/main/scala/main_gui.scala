package main_app

import core_app._
import scala.swing._
import scala.swing.event._
import Swing.{HStrut, VStrut, EmptyBorder}
import javax.swing.BorderFactory
import javax.swing.border._
import java.awt.Font
import java.io.File


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
  val lbNote = new Label("*: salida en ansi")
	val txtInputPath = new TextField(45)
	val chkOrganizar = new CheckBox("Organizar *")
  val chkSecuenciar = new CheckBox("Secuenciar *")
  val chkCodificar = new CheckBox("Codificar solo en Ansi")
	val txtAreaOutput = new TextArea(10,55){
		editable = false
		font = new Font("Console",Font.PLAIN,10)
	}
	val scrollTxtArea = new ScrollPane(txtAreaOutput){
		verticalScrollBarPolicy = ScrollPane.BarPolicy.AsNeeded
		border = BorderFactory.createTitledBorder("Detalles")
	}
	val btnProcesar = new Button("Aplicar")
	val btnHelp = new Button("Ayuda")
	val btnConfig = new Button("Config")

  listenTo(chkOrganizar, chkSecuenciar, chkCodificar, btnProcesar)
  reactions += {
    case ButtonClicked(`chkOrganizar`) => chkCodificar.selected = false
    case ButtonClicked(`chkSecuenciar`) => chkCodificar.selected = false
    case ButtonClicked(`chkCodificar`) =>
      chkOrganizar.selected = false
      chkSecuenciar.selected = false
    case ButtonClicked(`btnProcesar`) => txtInputPath.text match {
      case "" => Dialog.showMessage(null,	"Ingrese la ruta por favor.", title="You pressed me")
      case _ =>
        if (chkOrganizar.selected || chkCodificar.selected || chkSecuenciar.selected)
          new Core(ui).start
        else
          Dialog.showMessage(null, "Elija una funcion", title="Advertencia")
    }
    // case ButtonClicked(`btnConfig`) =>
    //   import sys.process._
    //   val cmd = "RUNDLL32.EXE SHELL32.DLL,OpenAs_RunDLL " + new File("application.conf").getName
    //   cmd.!
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
      contents += new BoxPanel(Orientation.Vertical){
        border = BorderFactory.createTitledBorder("Funciones")
  			contents += chkOrganizar
  			contents += VStrut(3)
        contents += chkSecuenciar
        contents += VStrut(3)
        contents += chkCodificar
      }
      contents += new BoxPanel(Orientation.Vertical){
        contents += new BoxPanel(Orientation.Horizontal){
          contents += HStrut(10)
          contents += btnProcesar
          contents += HStrut(20)
          contents += btnConfig
          contents += HStrut(5)
          contents += btnHelp
        }
        contents += VStrut(5)
        contents += new FlowPanel(FlowPanel.Alignment.Left)(){
          contents += HStrut(10)
          contents += lbNote
        }
      }
		}
		contents += scrollTxtArea
		border = EmptyBorder(10, 10, 10, 10)
  }
}
