import scala.swing._
import Swing._
import java.io.File
import scala.util.Try

object core {
	
	def procesar(txtArea: TextArea, path: String){

		var index = 0
		var filesMap = Seq[(File, String)]()
		val new_files = getListOfFiles(path + "\\NEW")
		val old_files = getListOfFiles(path + "\\OLD")

		// 1ro: CREATE_TABLE_%
		for {
			file <- new_files
			if file.getName.contains("CREATE_TABLE")
		} yield {
			println( genNewNameFile(index, "NEW", file.getName ))
			filesMap :+ (file, genNewNameFile(index, "NEW", file.getName))
			index = index + 1
		}

		// 2do: CREATE_INDEX_%
		for {
			file <- new_files
			if file.getName.contains("CREATE_INDEX")
		} yield {
			println( genNewNameFile(index, "NEW", file.getName ))
			filesMap :+ (file, genNewNameFile(index, "NEW", file.getName))
			index = index + 1
		}

		// 3ro: ALTER_TABLES_%
		for {
			file <- new_files
			if file.getName.contains("ALTER_TABLE")
		} yield {
			println( genNewNameFile(index, "NEW", file.getName ))
			filesMap :+ (file, genNewNameFile(index, "NEW", file.getName))
			index = index + 1
		}

		// 4to: DELETES DE METADATA VIEJA %
		for {
			file <- old_files
			if file.getName.contains(".Deletes.")
		} yield {
			println( genNewNameFile(index, "OLD", file.getName ))
			filesMap :+ (file, genNewNameFile(index, "OLD", file.getName))
			index = index + 1
		}

		// 5to: DELETES DE METADATA VIEJA
		for {
			file <- new_files
			if file.getName.contains(".Deletes.")
		} yield {
			println( genNewNameFile(index, "NEW", file.getName ))
			filesMap :+ (file, genNewNameFile(index, "NEW", file.getName))
			index = index + 1
		}

		// 6to: INSERTS DE METADATA NUEVA
		for {
			file <- new_files
			if file.getName.contains(".Inserts.")
		} yield {
			println( genNewNameFile(index, "NEW", file.getName ))
			filesMap :+ (file, genNewNameFile(index, "NEW", file.getName))
			index = index + 1
		}

		// 7to: COMPILAR PACKAGES E INSERTAR DATOS A TABLAS
		for {
			file <- new_files
			if !file.getName.contains("CREATE_TABLE") && !file.getName.contains("CREATE_INDEX") && !file.getName.contains("ALTER_TABLE") && !file.getName.contains(".Deletes.") && !file.getName.contains(".Inserts.")
		} yield {
			println( genNewNameFile(index, "NEW", file.getName ))
			filesMap :+ (file, genNewNameFile(index, "NEW", file.getName))
			index = index + 1
		}

		println(s"${index.toString} fueron acomodados y enumerados")

	}

	def getListOfFiles(dir: String):List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
        d.listFiles.filter(_.isFile).toList
    } else {
        List[File]()
    }
	}

	def genStringIndex(i: Int) = String.valueOf(i).length() match {
		case 1 => "00" + i.toString()
		case 2 => "0" + i.toString()
		case _ => i.toString()
	}

	def genNewNameFile(i: Int, prefix: String, filename: String) = genStringIndex(i) + "_" + prefix + "_" + filename
}


object componentes {
	
	val lbInputPath = new Label("Ingrese la Ruta que contienen la carpeta OLD y NEW:")
	
	var txtPath = new TextField() {
		columns = 20
		maximumSize = new Dimension(250, 30)
	}
	
	val btnProcesar = new Button(){
		maximumSize = new Dimension(100, 30)
		action = new Action("Procesar"){
			def apply(){ core.procesar(txtAreaOutput, txtPath.text) }
		}
	}
	
	var txtAreaOutput = new TextArea(350, 100){
		maximumSize = new Dimension(400, 100)
	}
}


class UI extends MainFrame {
  title = "Procesador de Pasajes Engage"
  preferredSize = new Dimension(500, 300)
  resizable = false
  contents = new BoxPanel(Orientation.Vertical){
  	contents += new BorderPanel { 
  		add(componentes.lbInputPath, BorderPanel.Position.West) 
	  	add(componentes.txtPath, BorderPanel.Position.South) 
  		maximumSize = new Dimension(350, 30)
  	}
  	contents += VStrut(5)
  	contents += Swing.Glue
  	contents += componentes.btnProcesar
  	contents += componentes.txtAreaOutput
  	preferredSize = new Dimension(450, 300)
	  border = Swing.EmptyBorder(10, 10, 10, 10)
  }
  for (e <- contents)
      e.xLayoutAlignment = 0.0
}

object app {
  def main(args: Array[String]) {
    val ui = new UI
    ui.visible = true
    println("End of main function")
  }
}