import scala.io.{Codec, Source}
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import scala.util.{Try, Using}

object HelperFunctions {
  // Read file
  def readFile(fileName: String, codec: String = Codec.default.toString): Try[List[String]] = {
    Using(Source.fromFile(fileName, codec)) {
      source => source.getLines().toList
    }
  }

  // Split rows
  def splitRow(row: String): Array[String] = {
    row.split(",")
  }


  // 1- Get Transaction Date
  def getTransDate(row : String): LocalDate = {
    LocalDate.parse(splitRow(row)(Config.transDateCol).split("T")(0))
  }

  // 2- Get Expiry Date
  def getExpiryDate(row: String): LocalDate = {
    LocalDate.parse(splitRow(row)(Config.expiryDateCol))
  }

  // 3- Get Product Name
  def getProductName(row: String): String = {
    splitRow(row)(Config.productNameCol).toLowerCase
  }

  // 4- Get Price
  def getPrice(row: String): Double = {
    splitRow(row)(Config.unitPriceCol).toDouble
  }

  // 5- Get Quantity
  def getQuantity(row: String): Int = {
    splitRow(row)(Config.quantityCol).toInt
  }

  // 6- Compare two dates and get the Days between them
  def compareDates(f1: String => LocalDate, f2: String => LocalDate, row: String): Long = {
    // Take getTransDate & getExpiryDate To Get Difference Between Them

    ChronoUnit.DAYS.between(f1(row), f2(row))
  }
}
