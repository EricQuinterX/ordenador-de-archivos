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
	val txtInputPath = new TextField(45)
	val chkOrganizar = new CheckBox("Ordenar pasaje")
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
					if (chkOrganizar.selected) Core(ui).organizar
					if (chkCodificar.selected) Core(ui).encodeAnsi
					if (!chkOrganizar.selected && !chkCodificar.selected) Dialog.showMessage(null, "Elija una funcion", title="Advertencia")
			}
		}
	}
	val btnHelp = new Button("Ayuda")
	val btnConfig = new Button("Config") {
		listenTo(this)
		reactions += {
			case ButtonClicked(_) => 
				import sys.process._
				val cmd = "RUNDLL32.EXE SHELL32.DLL,OpenAs_RunDLL " + new File("application.conf").getName
				cmd.!
		}		
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
			border = BorderFactory.createTitledBorder("Funciones")
			contents += chkOrganizar
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
