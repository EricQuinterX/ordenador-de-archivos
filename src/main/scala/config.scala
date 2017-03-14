package config_app

import core_app._
import modulo_app._

import com.typesafe.config.{ConfigFactory, Config}
import scala.util.{Try, Success, Failure}
import java.io.File

object ConfigFile {

	val configFile = ConfigFactory.parseFile(new File("application.conf")).getConfig("app")
	val config = ConfigFactory.load(configFile)

	def getFolder(m : Sistema) : String = {
		val destino = m match {
			case Organizador => Try(config.getString("destino_organizador"))
			case Secuenciador => Try(config.getString("destino_secuenciador"))
			case Codificador => Try(config.getString("destino_codificador"))
		}
		destino.getOrElse(throw new ErrorCargarConfig("Error cargar nombre de la carpeta del config"))
	}

	def getPriorityList : List[FileKeyword] = {
		import scala.collection.JavaConversions._
		Try(config.getConfigList("priority").map(FileKeyword(_)).toList).getOrElse(throw new ErrorCargarConfig("Error cargar orden de ejecucion del config"))
	}
}

trait Sistema
case object Organizador extends Sistema
case object Secuenciador extends Sistema
case object Codificador extends Sistema

case class FileKeyword (params: Config) {
	val keyword: String = Try (params.getString("keyword")).getOrElse(".")
	val folder : String = Try (params.getString("folder")).getOrElse("NEW")
}
