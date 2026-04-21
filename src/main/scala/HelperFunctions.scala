import scala.io.{Codec, Source}
import java.time.LocalDate
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

  // 6- Get PaymentMethod
  def getPaymentMethod(row: String): String = {
    splitRow(row)(Config.paymentMethodCol)
  }

  // 7- Get Channel Type
  def getChannel(row: String): String = {
    splitRow(row)(Config.channelCol)
  }
  
}
