package infrastructure.console

trait Console {
  def read(): String
  def print(line: String)
}
