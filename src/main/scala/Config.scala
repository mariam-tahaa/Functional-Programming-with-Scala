import com.typesafe.config.ConfigFactory

object Config {
  val config = ConfigFactory.load()

  val dbUrl: String = config.getString("database.url")
  val dbUser: String = config.getString("database.user")
  val dbPassword: String = config.getString("database.password")

  val logPath: String = config.getString("paths.logPath")
  val dataPath: String = config.getString("paths.dataPath")
}
