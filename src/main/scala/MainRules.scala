import java.time.temporal.ChronoUnit
import HelperFunctions.{ getQuantity,
                         getTransDate, getExpiryDate,
                         getProductName, getPrice,
                         getPaymentMethod, getChannel}


object MainRules {

  // Parse Row Data From String To Its Actual DataType
  case class ParsedRow(
                        transDate: java.time.LocalDate,
                        expiryDate: java.time.LocalDate,
                        productName: String,
                        quantity: Int,
                        price: Double,
                        paymentMethod: String,
                        channel: String
                      )

  // Extract Row From Data And Convert It Into It's DataType
  def parseRow(row: String): ParsedRow = {
    ParsedRow(
      getTransDate(row),
      getExpiryDate(row),
      getProductName(row),
      getQuantity(row),
      getPrice(row),
      getPaymentMethod(row),
      getChannel(row)
    )
  }

  // Class Contains Qualifier and Calculate Rules

  // Function to qualify   fun(order, bool)
  // Function to calculate fun(order, double)
  case class Rule(
                   qualify: ParsedRow => Boolean,
                   calculate: ParsedRow => Double
                 )
  {
    def apply(p: ParsedRow): Double =
      if (qualify(p)) calculate(p) else 0.0
  }


  // Rule1 expiryDate
  val expiryRule = Rule (
    qualify = p => {
      val daysLeft = ChronoUnit.DAYS.between(p.transDate, p.expiryDate)
      daysLeft > 0 && daysLeft < 30
    },
    calculate = p => {
      val daysLeft = ChronoUnit.DAYS.between(p.transDate, p.expiryDate)
      30 - daysLeft
    }
  )


  // Rule2 specialDate
  val specialDateRule = Rule(
    qualify = p =>
      p.transDate.getDayOfMonth == 23 &&
      p.transDate.getMonthValue == 3,

    calculate = _ => 50
  )


  // Rule3 productName
  val productNameRule = Rule(
  qualify = p =>
      p.productName.contains("wine") ||
      p.productName.contains("cheese"),

    calculate = p =>
      if (p.productName.contains("cheese")) 10
      else if (p.productName.contains("wine")) 5
      else 0
  )


  // Rule4 Quantity
  val quantityRule = Rule(
    qualify = p => p.quantity > 5,

    calculate = p => p.quantity match {
      case x if x <= 9  => 5
      case x if x <= 14 => 7
      case _            => 10
    }
  )


  // Rule5 paymentMethod
  val visaRule = Rule(
    qualify = p => p.paymentMethod == "Visa",
    calculate = _ => 5
  )


  // Rule6 channel Type (APP)
  val appRule = Rule(
    qualify = p => p.channel == "App",

    calculate = p => {
      val q = p.quantity
      ((q - 1) / 5 + 1) * 5.0
    }
  )

  /////////////////////////// Combine All Rules ///////////////////////////

  val rules: List[Rule] = List(
    expiryRule,
    specialDateRule,
    productNameRule,
    quantityRule,
    visaRule,
    appRule
  )

  def applyRules(p: ParsedRow): List[Double] =
    rules.map(_ (p))

  /////////////////////////// Calculate Discount & Get Top 2 ///////////////////////////

  def calculateDiscount(discounts: List[Double]): Double = {
    val top2 = discounts.filter(_ > 0).sorted(Ordering[Double].reverse).take(2)

    if (top2.isEmpty)
      0.00
    else
      top2.sum / top2.length
  }

  /////////////////////////// Calculate Final Price ///////////////////////////

  def finalPrice(p: ParsedRow, discount: Double): Double =
    p.price * (1 - discount / 100)

}