import java.sql.{Connection, DriverManager, PreparedStatement}

object DB_Connection {

  // 1- Load Oracle Driver
  Class.forName(Config.oracleDriver)

  // 2. Create Connection
  def getConnection(): Connection = {
    DriverManager.getConnection(
      Config.dbUrl,
      Config.dbUser,
      Config.dbPassword
    )
  }

  // 2. Insert orders as batches to DB
  def insertBatch(results: List[(String, Double, Double)]): Unit = {

    val conn = getConnection()

    val sql =
      """
        INSERT INTO transactions_order
        (product_name, discount, final_price)
        VALUES (?, ?, ?)
      """

    val stmt = conn.prepareStatement(sql)

    try {

      conn.setAutoCommit(false)

      results.foreach {
        case (product, discount, finalPrice) =>
          stmt.setString(1, product)
          stmt.setDouble(2, discount)
          stmt.setDouble(3, finalPrice)
          stmt.addBatch()
      }

      stmt.executeBatch()
      conn.commit()

    } catch {
      case e: Exception =>
        conn.rollback()
        throw e
    } finally {
      stmt.close()
      conn.close()
    }
  }
}