package config_app

import core_app._
import modulo_app._

import com.typesafe.config.{ConfigFactory, Config}
import scala.util.{Try, Success, Failure}
import java.io.File

object ConfigFile {

	def configFile = Try(ConfigFactory.parseFile(new File("application.conf")).getConfig("app")) match {
		case Success(s) => s
		case Failure(e) => throw new ErrorCargarConfiguracion("Error al parsear la configuracion\n" + e.getMessage)
	}

	def config = Try(ConfigFactory.load(configFile)) match {
		case Success(s) => s
		case Failure(e) => throw new ErrorCargarConfiguracion("Error al cargar el parseo de la configuracion\n" + e.getMessage)
	}

	def getFolder(m : Sistema) : String = {
		val destino = m match {
			case Organizacion => Try(config.getString("destino_organizador"))
			case Secuenciacion => Try(config.getString("destino_secuenciador"))
			case Codificacion => Try(config.getString("destino_codificador"))
		}
		destino match {
			case Success(s) => s
			case Failure(e) => {
				val msg = s"No se recupero el nombre de la carpeta de ${m.getClass.getName} en la configuracion\n" + e.getMessage
				throw new ErrorObtenerDatoConfig(msg)}
		}
	}

	def getPriorityList : List[FileKeyword] = {
		import scala.collection.JavaConversions._
		Try(config.getConfigList("priority").map(FileKeyword(_)).toList) match {
			case Failure(e) => throw new ErrorObtenerDatoConfig("Error al recuperar la lista de filtros de la configuracion\n" + e.getMessage)
			case Success(s) => s
		}
	}
}

trait Sistema
case object Organizacion extends Sistema
case object Secuenciacion extends Sistema
case object Codificacion extends Sistema

case class FileKeyword (params: Config) {
	val keyword: String = Try (params.getString("keyword")).getOrElse(".")
	val folder : String = Try (params.getString("folder")).getOrElse("NEW")
	val match_pos : String = Try (params.getString("pos")).getOrElse("MEDIO")
}
