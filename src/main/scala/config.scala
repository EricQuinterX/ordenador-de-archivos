package config_app

import core_app._
import com.typesafe.config.{ConfigFactory, Config}
import scala.util.{Try, Success, Failure}
import java.io.File

object ConfigFile {

	val configFile = ConfigFactory.parseFile(new File("application.conf")).getConfig("app")
	val config = ConfigFactory.load(configFile)

	def getFoldername : String = Try(config.getString("destino_organizador")) match {
		case Success(s) => s
		case Failure(_) => throw new ErrorCargarConfig("Error cargar nombre de la carpeta del config")
	}

	def getExecOrderList : List[FileKeyword] = {
		import scala.collection.JavaConversions._
		val lista_orden = Try ( config.getConfigList("priority").map(FileKeyword(_)).toList )
		lista_orden match {
			case Success(xs) => xs
			case Failure(_) => throw new ErrorCargarConfig("Error cargar orden de ejecucion del config")	
		}
	}
}