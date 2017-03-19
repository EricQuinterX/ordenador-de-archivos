package errores_app

//EXCEPCIONES QUE PUEDEN EXISTIR
case class ErrorGuiApp(msg: String) extends Exception(msg)
case class ErrorCrearCarpeta(msg: String) extends Exception(msg)
case class ErrorCrearArchivo(msg: String) extends Exception(msg)
case class ErrorCopiarArchivos(msg: String) extends Exception(msg)
case class ErrorObtenerDatoVacio(msg: String) extends Exception(msg)
case class ErrorObtenerDatoConfig(msg: String) extends Exception(msg)
case class ErrorCargarConfiguracion(msg: String) extends Exception(msg)
