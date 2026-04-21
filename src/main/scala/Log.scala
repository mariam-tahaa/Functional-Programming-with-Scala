import java.time.LocalDateTime
import java.io.{FileWriter, PrintWriter}

object Log {

  // Log levels
  val INFO  = "INFO"
  val WARN  = "WARN"
  val ERROR = "ERROR"

  def log(path: String, level: String, msg: String): Unit = {
    val timeNow = LocalDateTime.now()

    val writer = new PrintWriter(new FileWriter(path, true))
    try {
      writer.println(s"[$timeNow] [$level] $msg")
    }
    finally {
      writer.close()
    }
  }

  /////////////////////////// Calling Levels ///////////////////////////

  def info(path: String, msg: String): Unit =
    log(path, INFO, msg)

  def warn(path: String, msg: String): Unit =
    log(path, WARN, msg)

  def error(path: String, msg: String): Unit =
    log(path, ERROR, msg)
}