import MainRules.{applyRules, calculateDiscount, finalPrice, parseRow}

import scala.util.{Failure, Success, Try}
import Log.{error, info, warn}
import HelperFunctions.readFile

import scala.collection.parallel.CollectionConverters._
import scala.collection.parallel.ForkJoinTaskSupport

object Main extends App {

  val logMSG = Config.logPath

  // 2. Pipeline
  def pipeline(allRows: List[String]): List[(String, Double, Double)] = {
    // allRows.map(row).applyRules.calculateDiscounts.finalPrice

    val forkJoinPool = new java.util.concurrent.ForkJoinPool(6)

    val parRows = allRows.par
    parRows.tasksupport = new ForkJoinTaskSupport(forkJoinPool)

    def safeProcess(row: String): Option[(String, Double, Double)] = {
      try {
        val parsed = parseRow(row)
        val discounts = applyRules(parsed)
        val avgDiscount = calculateDiscount(discounts)
        val totalPrice = finalPrice(parsed, avgDiscount)

        //Log.log(s"Processed ${parsed.productName}: Discount $avgDiscount%, Final Price $totalPrice")

        Some(parsed.productName, avgDiscount, totalPrice)
      } catch {
        case _: Exception => None
      }
    }

    parRows.flatMap(safeProcess).toList

  }

  // Calling Pipeline
  info(logMSG, "Start Processing New Orders!!!")

  val startTime = System.currentTimeMillis()

  val finalResults = readFile(Config.dataPath)
  match {
    case Success(lines)
      if lines.nonEmpty =>
      info(logMSG, "File has been read successfully")
      pipeline(lines.tail)
    case Success(_) =>
      warn(logMSG, "File is empty.")
      List()
    case Failure(e) =>
      error(logMSG, s"Failed to read file: ${e.getMessage}")
      List()
  }

  val endTime = System.currentTimeMillis()

  println(s"TOTAL PROCESSING TIME = ${(endTime - startTime) / 1000.0} seconds")

  // Insert Batches To DB
  if (finalResults.nonEmpty) {

    val startInsert = System.currentTimeMillis()

    Try(finalResults
      .grouped(100000)
      .foreach(batch => DB_Connection.insertBatch(batch)))

    match {
      case Success(_) => info(logMSG, s"Successfully inserted ${finalResults.size} records.")
        val endInsert = System.currentTimeMillis()

        println(s"DB INSERT TIME = ${(endInsert - startInsert) / 1000.0} seconds")

      case Failure(e) => error(logMSG, s"Database error : ${e.getMessage}")

    }
  }
}