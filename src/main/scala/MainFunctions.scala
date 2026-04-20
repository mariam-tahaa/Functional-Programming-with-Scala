import scala.io.{Codec, Source}
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.logging.Logger

object MainFunctions extends App {

  val logger = Logger.getLogger("MainFunctions")

  // Read file
  def readFile(fileName: String, codec: String = Codec.default.toString): List[String] = {
    Source.fromFile(fileName, codec).getLines().toList
  }

  // Read Data
  val file = readFile("src/main/resources/TRX1000.csv")
  //logger.info("File loaded")
  val data = file.tail

  // Split rows
  def splitRow(row: String): Array[String] = {
    row.split(",")
  }

  // 1- Get Transaction Date
  def getTransDate(row : String): LocalDate = {
    LocalDate.parse(splitRow(row)(0).split("T")(0))
  }

  // 2- Get Expiry Date
  def getExpiryDate(row: String): LocalDate = {
    LocalDate.parse(splitRow(row)(2))
  }

  // 3- Get Product Name
  def getProductName(row: String): String = {
    splitRow(row)(1).toLowerCase
  }

  // 4- Get Price
  def getPrice(row: String): Double = {
    splitRow(row)(4).toDouble
  }

  // 5- Get Quantity
  def getQuantity(row: String): Int = {
    splitRow(row)(3).toInt
  }

  // 6- Compare two dates and get the Days between them
  def compareDates(f1: String => LocalDate, f2: String => LocalDate, row: String): Long = {
    // Take getTransDate & getExpiryDate To Get Difference Between Them

    ChronoUnit.DAYS.between(f1(row), f2(row))
  }

  /////////////////////////// Rules ///////////////////////////

  // Function to qualify   fun(order, bool)
  // Function to calculate fun(order, double)

  // Qualify to Rule1
  def Q_expiryRule(row: String): Boolean = {

    val daysLeft = compareDates(getTransDate, getExpiryDate, row)

    if (daysLeft >= 0 && daysLeft < 30)
      true
    else
      false
  }

  // Calculate Rule1
  def C_expiryRule(row: String): Double = {
    val daysLeft = compareDates(getTransDate, getExpiryDate, row)

    if(Q_expiryRule(row))
      30 - daysLeft
    else
      0.00
  }

  ////////////////

  // Qualify to Rule2
  def Q_specialDateRule(row: String): Boolean = {

    val specialDate = getTransDate(row)

    if (specialDate.getDayOfMonth == 23 && specialDate.getMonthValue == 3)
      true
    else
      false
  }

  // Calculate Rule2
  def C_specialDateRule(row: String): Double = {
    if (Q_specialDateRule(row))
      50
    else
      0.00
  }

  ////////////////

  // Qualify to Rule3
  def Q_productNameRule(row: String): Boolean = {
    val name = getProductName(row)
    if (name.contains("wine") || name.contains("cheese"))
      true
    else
      false
  }

  // Calculate Rule3
  def C_productNameRule(row: String): Double = {
    val name = getProductName(row)

    if (name.contains("cheese"))
      10
    else if (name.contains("wine"))
      5
    else
      0.0
  }

  ////////////////

  // Qualify to Rule4
  def Q_productQuantityRule(row: String): Boolean = {
    val q = getQuantity(row)
    q > 5
  }

  // Calculate Rule4
  def C_productQuantityRule(row: String): Double = {
    val q = getQuantity(row)

    q match {
      case x if x <= 9  => 5
      case x if x <= 14 => 7
      case _            => 10
    }
  }

  /////////////////////////// Combine All Rules ///////////////////////////

  def allRules(q: String => Boolean, c: String => Double): String => Double = {
    row =>
      if (q(row))
        c(row)
      else
        0.0
  }

  val rule1 = allRules(Q_expiryRule, C_expiryRule)
  val rule2 = allRules(Q_specialDateRule, C_specialDateRule)
  val rule3 = allRules(Q_productNameRule, C_productNameRule)
  val rule4 = allRules(Q_productQuantityRule, C_productQuantityRule)

  val rules: List[String => Double] = List(rule1, rule2, rule3, rule4)

  def applyRules(row: String): List[Double] = {
    rules.map(rule => rule(row))
  }

  def calculateDiscount(discounts: List[Double]): Double = {
    val top2 = discounts.filter(_ > 0).sorted(Ordering[Double].reverse).take(2)

    if (top2.isEmpty)
      0.00
    else
      top2.sum / top2.length
  }

  def finalPrice(row: String , discount: Double): Double = {
    val originalPrice = getPrice(row)

    val finalPrice = originalPrice * (1 - discount / 100)

    finalPrice
  }

  def pipeline(allRows: List[String]): List[(String , Double , Double)] = {
    // allRows.map(row).applyRules.calculateDiscounts.finalPrice
    allRows.map{row =>
      val discount = applyRules(row)

      println(s"DEBUG: Raw row: $row")
      println(s"DEBUG: Applied rules results: $discount")

      val avgDiscount = calculateDiscount(discount)

      println(s"DEBUG: AVGDiscount: $avgDiscount")

      val totalPrice = finalPrice(row, avgDiscount)

      println(s"DEBUG: TotalPrice: $totalPrice")

      (getProductName(row) , avgDiscount , totalPrice)
    }

  }


  val results = pipeline(data)

  DB_Connection.insertAll(results)

}
