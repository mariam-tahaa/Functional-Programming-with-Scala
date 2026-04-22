import MainRules.{applyRules, calculateDiscount, finalPrice, parseRow}
import HelperFunctions.readFile
import scala.util.{Failure, Success, Try}
import Log.{error, info, warn}

import scala.collection.parallel.CollectionConverters._
import scala.collection.parallel.ForkJoinTaskSupport

object Main extends App {

  // Dynamic thread count based on available cores
  def withParallelism[A](parallelism: Int)(f: ForkJoinTaskSupport => A): A = {
    val pool    = new java.util.concurrent.ForkJoinPool(parallelism)
    val support = new ForkJoinTaskSupport(pool)
    try f(support)
    finally pool.shutdown()
  }

  // use cores for CPU-bound, cores * 2 for I/O-bound workloads
  val cores = Runtime.getRuntime.availableProcessors()


  // Process one row , and catch rows with error without crashing the pipeline
  def safeProcess(row: String): Either[String, (String, Double, Double)] =
    Try {
      val parsed      = parseRow(row)
      val discounts   = applyRules(parsed)
      val avgDiscount = calculateDiscount(discounts)
      val total       = finalPrice(parsed, avgDiscount)
      (parsed.productName, avgDiscount, total)
      // toEither returns => Left(error) , Right(success) : .left.map => Convert error into readable message
    }.toEither.left.map(e => s"Row failed [${e.getClass.getSimpleName}]: ${e.getMessage}")

                  /////////////////////////////////////
  // Main Pipeline : Takes orders , threadpool => Returns : errors , success (productName, totalDiscount, finalPrice)
  def pipeline(allRows: Vector[String], support: ForkJoinTaskSupport): (Vector[String], Vector[(String, Double, Double)]) = {

    // Divide total size of data across available cores
    val chunkSize = (allRows.size / cores).max(1)

    // Split rows into chunks
    val chunks    = allRows.grouped(chunkSize).toVector

    // Convert chunks to parallel collection , To run in parallel
    val parChunks = chunks.par

    // Uses ForkJoin rather than Scala Default configurations
    parChunks.tasksupport = support

    // Each chunk processed sequentially inside, chunks run in parallel
    val results: Vector[Either[String, (String, Double, Double)]] =
      parChunks
        .flatMap(chunk => chunk.map(safeProcess))
        .toVector

                  /////////////////////////////////////
    // FoldLeft : Here acts as accumulator, append error and success at the same pass
    // Not need one pass to collect success , then extra pass again to collect error
    results.foldLeft((Vector.empty[String], Vector.empty[(String, Double, Double)])) {
      case ((errs, succ), Left(e))  => (errs :+ e,  succ)
      case ((errs, succ), Right(r)) => (errs,        succ :+ r)
    }
  }
                  /////////////////////////////////////


                  /////////////////////////////////////
  // 5. Edge of the World — ALL side effects here
  // Logging, DB writes, (timing, printing) => Just For Test
  val logPath = Config.logPath

  info(logPath, "Start Processing New Orders!!!")

  // Pipeline starts
  val processingStartTime = System.currentTimeMillis()

  val (processingErrors, finalResults) = {
    // Read File Of Data => Side effect
    readFile(Config.dataPath) match {

      case Success(lines) if lines.nonEmpty =>
        info(logPath, s"File read successfully — ${lines.size - 1} rows to process")
        withParallelism(cores) { support =>
          pipeline(lines.tail, support)
        }

      case Success(_) =>
        warn(logPath, "File is empty.")
        (Vector.empty[String], Vector.empty[(String, Double, Double)])

      case Failure(e) =>
        error(logPath, s"Failed to read file: ${e.getMessage}")
        (Vector.empty[String], Vector.empty[(String, Double, Double)])
    }
  }
  /////////////////////////////////////

  // Pipeline Ends
  val processingEndTime = System.currentTimeMillis()

  // Collect Errors Per Row
  processingErrors.foreach(e => warn(logPath, s"Row skipped — $e"))

  info(logPath, s"Processing complete — ${finalResults.size} succeeded, ${processingErrors.size} failed")
  println(s"TOTAL PROCESSING TIME = ${(processingEndTime - processingStartTime) / 1000.0} seconds")

  // Insert Orders Into DB As Batches
  if (finalResults.nonEmpty) {
    // To Calc Total Time Of Insertion
    val insertStartTime = System.currentTimeMillis()

    // Dynamic batch size based on result volume
    val batchSize = if (finalResults.size > 1_000_000) 200_000 else 10_000

    // Parallel batch insertion
    Try(
      finalResults
        .grouped(batchSize)
        .toVector
        .par
        .foreach(batch => DB_Connection.insertBatch(batch))
    ) match {

      case Success(_) =>
        val insertEndTime = System.currentTimeMillis()
        info(logPath, s"Successfully inserted ${finalResults.size} records in batches of $batchSize.")
        println(s"DB INSERT TIME = ${(insertEndTime - insertStartTime) / 1000.0} seconds")

      case Failure(e) =>
        error(logPath, s"Database error: ${e.getMessage}")
    }

  } else {
    warn(logPath, "No records to insert.")
  }
}