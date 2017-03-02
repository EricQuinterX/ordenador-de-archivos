package project_Engage

import scala.swing._
import Swing._
import java.io.File

class UI extends MainFrame {
  title = "Procesador de Pasajes Engage"
  preferredSize = new Dimension(500, 200)
  contents = new BoxPanel(Orientation.Vertical){
  	contents += new Label("Seleccione la ruta del backup:")
  	contents += new TextField(1)
  	contents += new BorderPanel {
  		add(new Button("Procesar"), BorderPanel.Position.West)
  	}
  	contents += VStrut(10)
  	contents += new BorderPanel {
  		add(new Button("Salir"), BorderPanel.Position.East)
  	}
  	border = Swing.EmptyBorder(10, 10, 10, 10)
  }
}

object app {
  def main(args: Array[String]) {
    val ui = new UI
    ui.visible = true
    println("End of main function")
  }
}