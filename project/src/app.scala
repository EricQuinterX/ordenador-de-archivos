import scala.swing._
import Swing._
import java.io.File
import scala.util.Try
import java.awt.{Font}

case class FileKeyword (word: String, folder: String)

case class StructFile(remainingFiles: List[File] = Nil, 
											componente: TextArea, 
											index: Int = 0, 
											filemap: Seq[(File, String)] = Nil){
	
	def applyProcess(word: String, foldername: String): StructFile = {
		var lista: Seq[(File, String)] = Nil
		var i = index

		for {
			file <- remainingFiles.filter( _.getParentFile.getName == foldername )
			if file.getName.contains(word)
		} yield {
			val p = genNewNameFile(i, foldername, file.getName )
			println( p )
			componente.append(p + "\n")
			lista :+ (file, p )
			i += 1
		}
		var resto = remainingFiles.filter( f => !f.getName.contains(word) || !(f.getParent == foldername) )
		copy(index = i, filemap = filemap ++: lista, remainingFiles = resto)
	}

	def genNewNameFile(i: Int, prefix: String, filename: String) = genStringIndex(i) + "_" + prefix + "_" + filename

	def genStringIndex(i: Int) = String.valueOf(i).length() match {
		case 1 => "00" + i.toString()
		case 2 => "0" + i.toString()
		case _ => i.toString()
	}
}

object Core {
	
	val priority : Map[Int, FileKeyword] = Map (
		0 -> FileKeyword(".Deletes.","OLD"),
		1 -> FileKeyword("CREATE_TABLE","NEW"),
		2 -> FileKeyword("CREATE_INDEX","NEW"),
		3 -> FileKeyword("ALTER_TABLE","NEW"),
		4 -> FileKeyword("ALTER_INDEX","NEW"),
		5 -> FileKeyword(".Deletes.","NEW"),
		6 -> FileKeyword(".Inserts.","NEW"),
		7 -> FileKeyword("FUNCITON","NEW"),
		8 -> FileKeyword("PROCEDURE","NEW"),
		9 -> FileKeyword("PACKAGE","NEW")
	)

	def procesar(txtArea: TextArea, path: String){

		val new_files = getListOfFiles(path + "\\NEW")
		val old_files = getListOfFiles(path + "\\OLD")
		val all_files = (new_files ::: old_files).sorted
		var filesystem = StructFile(all_files, txtArea)
		var i: Int = 0

		for (i <- 0 until priority.size)
			filesystem = filesystem.applyProcess( priority(i).word, priority(i).folder )

		def createAndCopyFiles(name: String) {
			import sys.process._
			val sym = s"cd $path && mkdir $name" //corregir el path xq en el cmd jode el simple \
			println(sym)
			sym.!
			for {
				(file, newNameFile) <- filesystem.filemap
			} yield {
				s"copy $file $path\\$newNameFile".! //corregir el path xq en el cmd jode el simple \
				txtArea.append(s"Archivo $newNameFile fue creado\n")
			}
		}

		createAndCopyFiles("ARCHIVOS_BBDD")

		txtArea.append(s"${filesystem.index.toString} fueron acomodados y enumerados \n")
	}



	def getListOfFiles(dir: String):List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
        d.listFiles.filter(_.isFile).toList
    } else {
        List[File]()
    }
	}
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
			def apply(){ Core.procesar(txtAreaOutput, txtPath.text) }
		}
	}

	var txtAreaOutput = new TextArea(450,200){
		maximumSize = new Dimension(500, 100)
		editable = false
		font = new Font("Console",Font.PLAIN,10)
	}
	
	var scrollTxtArea = new ScrollPane(txtAreaOutput){
		verticalScrollBarPolicy = ScrollPane.BarPolicy.AsNeeded
	}
}


class UI extends MainFrame {
  title = "Procesador de Pasajes Engage"
  preferredSize = new Dimension(600, 300)
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
  	contents += componentes.scrollTxtArea
  	preferredSize = new Dimension(500, 300)
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