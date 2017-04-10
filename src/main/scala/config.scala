package config_app

import errores_app._

import com.typesafe.config.{ConfigFactory, Config}
import scala.util.{Try, Success, Failure}
import java.io.File

object ConfigFile {

	def configFile = Try(ConfigFactory.parseFile(new File("application.config")).getConfig("app")) match {
		case Success(s) => s
		case Failure(e) => throw ErrorCargarConfiguracion("No se pudo parsear el archivo de configuracion")
	}

	def config = Try(ConfigFactory.load(configFile)) match {
		case Success(s) => s
		case Failure(e) => throw ErrorCargarConfiguracion("Error al cargar el parseo de la configuracion\n" + e.getMessage)
	}

	def getFolder(m : Sistema = Organizacion) : String = Try(config.getString("output_folder")) match {
		case Success(s) => s
		case Failure(e) => {
			val msg = s"No se recupero el nombre de la carpeta de ${m.getClass.getName} en la configuracion\n" + e.getMessage
			throw ErrorObtenerDatoConfig(msg)
		}
	}

	def getFilters : List[FileKeyword] = {
		import scala.collection.JavaConversions._
		Try(config.getConfigList("filters").map(FileKeyword(_)).toList) match {
			case Success(s) => s
			case Failure(e) => throw ErrorObtenerDatoConfig("Error al recuperar la lista de filtros de la configuracion\n" + e.getMessage)
		}
	}

	def getFileType : String = Try(config.getString("file_type")) match {
		case Success(s) => if (s.isEmpty.unary_!) s else throw ErrorObtenerDatoConfig("ErrorObtenerDatoConfig: es un dato vacio.")
		case Failure(e) => throw ErrorObtenerDatoConfig("No se pudo recuperar tal valor.")
	}

	def getTool : String = Try(config.getString("tool")) match {
		case Success(s) => if (s.isEmpty.unary_!) s else throw ErrorObtenerDatoVacio("Es un dato vacio.")
		case Failure(e) => throw ErrorObtenerDatoConfig("No se pudo recuperar tal valor.")
	}

	def getFolderList : List[Folder] = {
		import scala.collection.JavaConversions._
		Try(config.getConfigList("folders").map(Folder(_)).toList) match {
			case Success(s) => s
			case Failure(e) => throw ErrorObtenerDatoConfig("Error al recuperar la lista de carpetas de la configuracion\n" + e.getMessage)
		}
	}

	def getLinkHelp : String = Try(config.getString("link_help")) match {
		case Success(s) => if (s.isEmpty.unary_!) s else throw ErrorObtenerDatoConfig("ErrorObtenerDatoConfig: es un dato vacio.")
		case Failure(e) => throw ErrorObtenerDatoConfig("No se pudo recuperar tal valor.")
	}

}


trait Sistema
case object Organizacion extends Sistema
case object Secuenciacion extends Sistema
case object Codificacion extends Sistema

case class FileKeyword (params: Config) {
	val keyword: String = params.getString("keyword")
	val folder : String = params.getString("folder")
	val match_pos : String = params.getString("pos")
}

case class Folder (params: Config) {
	val name : String = params.getString("name")
	val show : Boolean = params.getString("show").toBoolean
}
