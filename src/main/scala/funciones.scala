package modulo_app

import core_app._
import config_app._

import java.io.{File, FileInputStream, FileOutputStream, OutputStreamWriter}
import scala.swing.TextArea
import scala.util.{Try, Success, Failure}
import java.util.regex.{Matcher, Pattern}


trait Modulo {

  def startFunction : Unit

  def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory)
      d.listFiles.filter(_.isFile).toList.filter(_.getName.toUpperCase.contains(".SQL"))
    else List[File]()
  }

  def clearAndCreateDir(destino: File) : Boolean = {
    def deleteAll (file: File) : Boolean = {
      if (file.isDirectory && file.listFiles.length > 0)
        file.listFiles.foreach(deleteAll)
      !(file.exists && !file.delete)
    }
    deleteAll(destino) && destino.mkdir
  }
}



class Organizador (ruta : String, out : TextArea, flags : Option[Funciones] = None) extends Modulo {

  var index: Int = 0
  var restantes : List[File] = Nil
  var ordenados : List[(File, String)] = Nil
  val carpeta_destino : String = ConfigFile.getFolder(Organizacion)

  def startFunction = {
    // Cada vez que ejecuta esta funcionalidad, recupero la lista de filtros si es que lo modifico en tiempo de ejecucion
    val configPriority : List[FileKeyword] = ConfigFile.getPriorityList
    val PRIORITY_SIZE = configPriority.size
    // La secuencia empieza siempre en 000
    var i : Int = 0
    // Meto los archivos de las 2 carpeta en una lista
    restantes = (getListOfFiles(ruta +"\\OLD") ::: getListOfFiles(ruta +"\\NEW")).sorted
    // Recorro la lista de filtros de arriba hacia abajo
    for (i <- 0 to PRIORITY_SIZE) {
      if (i == PRIORITY_SIZE)
        // llego al ultimo, ejecuto los restantes
        filtrar(".", "NEW", ".*")
      else
        // Paso los valores de cada filtro de la lista, para que el metodo filtro filtre con esos valores
        filtrar(configPriority(i).keyword, configPriority(i).folder, configPriority(i).match_pos)
    }
    // Copio los archivos de las 2 carpetas a un nueva en base a los nuevos nombres de la lista nueva
    createFiles(new File(ruta + "\\" + carpeta_destino))
    out.append(index + " archivos fueron organizados\n")
  }

	private def filtrar (word: String, folder: String, pos: String): Unit = {
    // Funcion que compara el nombre de la carpeta "NEW/OLD", el patron "keyword" y la posicion "INICIO/MEDIO"
    def criteria(x: File) : Boolean = {
      val sameFolder = x.getParentFile.getName == folder
      val regex = word.replaceAllLiterally("***", ".*") + ".*"
      val patron = Pattern.compile(regex)
      val matcher = patron.matcher(x.getName)
      if (matcher.find) {
        val position = matcher.start match {
          case 0 => "INICIO"
          case n => "MEDIO"
        }
        sameFolder && position == pos
      } else false
    }
    // La lista de los archivos filtrados para luego sacar una diferencia
    var filtrados: List[File] = Nil
    for (f <- restantes.filter(criteria).sortBy(_.getName)){
      // Inserto en la lista el nuevo nombre del archivo
      ordenados = ordenados.::((f, getNewName(index, folder, f.getName)))
      index += 1
      filtrados = filtrados.::(f)
    }
    // Hago la diferencia de los restantes menos los filtrados
    restantes = restantes filterNot filtrados.contains
	}

  private def createFiles(dir: File) = {
    Try(clearAndCreateDir(dir)) match {
      case Success(s) => if (!s) throw new ErrorCrearCarpeta("No se limpio la carpeta ARCHIVOS_ORGANIZADOS")
      case Failure(e) => out.append(e.getMessage)
    }
    // CREO LOS ARCHIVOS SQL CON SUS RESPECTIVOS NOMBRES
    for ((src, name) <- ordenados.sortBy(_._2)){
      val destin = new File(dir.getPath + "\\" + name)
      val fos = new FileOutputStream(destin)
      fos.getChannel.transferFrom(new FileInputStream(src).getChannel, 0, Long.MaxValue)
      fos.close // IMPORTANTE CERRAR EL ARCHIVO
      out.append("Archivo creado: " + name + "\n")
    }
  }

	private def getNewName (i: Int, prefix: String, filename: String) = prefix match {
		case "OLD" => getIndex(i) + "_" + prefix + "_" + filename
		case "NEW" => getIndex(i) + "_" + filename // a los archivos del /NEW no muestro este prefijo
	}

  // Creo el indice de los archivos
	private def getIndex(i: Int) = i.toString.length match {
		case 1 => "00" + i.toString
		case 2 => "0" + i.toString
		case _ => i.toString
	}
}


// NO SE IMPLEMENTARA HASTA QUE SEA FACTIBLE, YA QUE EXISTE UN SECUENCIADOR
class Secuenciador (ruta: String, out: TextArea, algo: Option[Funciones] = None) extends Modulo {
  def startFunction : Unit = {
  }
}


// NO SE USARA ESTA CLASE, YA QUE NO ES 100% SEGURO LA DESCODIFICACION Y GRABA CARACTERES RAROS EN ANSI
// EL USUARIO DEBE PASAR CADA ARCHIVO EN ANSI, CON EL NOTEPAD++
class Codificador (ruta: String, out: TextArea, algo: Option[Funciones] = None) extends Modulo {

  def startFunction : Unit = {
    import java.nio.file.{Paths,Files}
    import java.nio.ByteBuffer
    import java.nio.charset.Charset
    val dest = new File(new File(ruta).getParent + "\\" + ConfigFile.getFolder(Codificacion))
    Try(clearAndCreateDir(dest)) match {
      case Success(s : Boolean) => if (!s) throw new ErrorCrearCarpeta("No se limpio la carpeta ARCHIVOS_ORGANIZADOS")
      case Failure(e) => out.append(e.getMessage)
    }
    out.append("Archivos Codificados a Ansi:\n")
    for (file <- getListOfFiles(ruta).sortBy(_.getName)){
      val codFile = new File(dest.getPath + "\\" + file.getName)
      val encoding = detectEncoding(file)
      if (codFile.createNewFile.unary_!) throw new ErrorCrearArchivo(s"No se creo el archivo ${file.getName} en la carpeta ${dest.getName}")
      val pathOrig = Paths.get(file.getPath)
      val pathDest = Paths.get(codFile.getPath)
      var bb = ByteBuffer.wrap(Files.readAllBytes(pathOrig))
      val cb = Charset.forName(encoding).decode(bb)
      bb = Charset.forName("ISO-8859-1").encode(cb)
      Files.write(pathDest, bb.array);
      out.append(s"${file.getName} => [encode='$encoding' --> decode='ISO-8859-1(ANSI)']\n")
    }
  }

  private def detectEncoding(f: File) : String = {
    import com.ibm.icu.text.CharsetDetector
    var charset = "ISO-8859-1"
    val fin = new FileInputStream(f.getPath)
    var fileContent = new Array[Byte](f.length.toInt)
    fin.read(fileContent)
    val data =  fileContent
    val detector = new CharsetDetector().setText(data)
    val cm = detector.detect
    if (cm != null){
      if (cm.getConfidence > 50) {charset = cm.getName}
    }
    fin.close
    charset
  }
}
