import java.sql.{Connection, DriverManager, PreparedStatement}

object DB_Connection extends App {

  // 1. Load Oracle Driver
  Class.forName("oracle.jdbc.OracleDriver")

  // 2. Create Connection
  def getConnection(): Connection = {

    val url = "jdbc:oracle:thin:@localhost:1521:xe"
    val user = "scala_orders"
    val password = "1234"

    DriverManager.getConnection(url, user, password)
  }

  // 3. Insert one row
  def insertRow(conn: Connection, product: String, discount: Double, finalPrice: Double): Unit = {

    val sql =
      """
        INSERT INTO transactions_result
        (id, product_name, discount, final_price)
        VALUES (transactions_seq.NEXTVAL, ?, ?, ?)
      """

    val stm: PreparedStatement = conn.prepareStatement(sql)

    stm.setString(1, product)
    stm.setDouble(2, discount)
    stm.setDouble(3, finalPrice)

    stm.executeUpdate()
    stm.close()
  }

  // 4. Insert multiple rows
  def insertAll(results: List[(String, Double, Double)]): Unit = {

    val conn = getConnection()

    try {
      results.foreach {
        case (product, discount, finalPrice) => insertRow(conn, product, discount, finalPrice)
      }
    }

    finally {
      conn.close()
    }
  }

}