import MainRules.{applyRules, calculateDiscount, finalPrice}
import scala.util.{Failure, Success, Try}
import Log.{error, info, warn}
import HelperFunctions.{readFile, getProductName}

object Main extends App {

  val logMSG = Config.logPath

  def pipeline(allRows: List[String]): List[(String , Double , Double)] = {
    // allRows.map(row).applyRules.calculateDiscounts.finalPrice
    allRows.flatMap{row =>
      Try {
        val discount = applyRules(row)
        val avgDiscount = calculateDiscount(discount)
        val totalPrice = finalPrice(row, avgDiscount)
        val productName  = getProductName(row)

        info(logMSG, s"Processed $productName: Discount $avgDiscount%, Final Price $totalPrice")
        (productName, avgDiscount, totalPrice)
      }
      match {
        case Success(res) => Some(res)
        case Failure(e) =>
          error(logMSG, s"Error processing row [$row]: ${e.getMessage}")
          None
      }

    }

  }

  // Calling pipeline
  info(logMSG, "Start Processing New Orders!!!")

  val finalResults = readFile(Config.dataPath) match {
    case Success(lines) if lines.nonEmpty => pipeline(lines.tail)
    case Success(_) =>
      warn(logMSG, "File is empty.")
      List()
    case Failure(e) =>
      error(logMSG, s"Failed to read file: ${e.getMessage}")
      List()
  }

  if (finalResults.nonEmpty) {
    Try(DB_Connection.insertAll(finalResults)) match {
      case Success(_) => info(logMSG, s"Successfully inserted ${finalResults.size} records.")
      case Failure(e) => error(logMSG, s"Database error : ${e.getMessage}")
    }
  }

}
