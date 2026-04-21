import com.typesafe.config.ConfigFactory

object Config {
  val config = ConfigFactory.load()

  val dbUrl: String = config.getString("database.url")
  val dbUser: String = config.getString("database.user")
  val dbPassword: String = config.getString("database.password")

  val logPath: String = config.getString("paths.logPath")
  val dataPath: String = config.getString("paths.dataPath")

  val oracleDriver: String = config.getString("oracleDriver.driver")

  val transDateCol:     Int = config.getInt("columns.transDateCol")
  val productNameCol:   Int = config.getInt("columns.productNameCol")
  val expiryDateCol:    Int = config.getInt("columns.expiryDateCol")
  val quantityCol:      Int = config.getInt("columns.quantityCol")
  val unitPriceCol:     Int = config.getInt("columns.unitPriceCol")
  val channelCol:       Int = config.getInt("columns.channelCol")
  val paymentMethodCol: Int = config.getInt("columns.paymentMethodCol")
}
