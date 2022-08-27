package infrastructure.console
import java.io.BufferedReader
import java.io.IOException

class SystemConsole extends Console {

  override def read(): String = {
    var line = ""
    try line = scala.io.StdIn.readLine()
    catch {
      case e: IOException =>
        e.printStackTrace()
    }
    line
  }

  override def print(line: String): Unit = println(line)
}
