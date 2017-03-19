import org.scalatest.FunSuite
import config_app._
import errores_app._

class Configuracion extends FunSuite {

  test("Deberia devolver el valor de la variable 'tool' del archivo 'application.conf'") {
    // assertThrows[ErrorObtenerDatoVacio] {
    //   ConfigFile.getTool
    // }
    assert(ConfigFile.getTool == "Engage")
  }

  test("Devuelve el tamanio de la lista de filtros") {

    assert(ConfigFile.getFilters.size == 21)

  }

}
