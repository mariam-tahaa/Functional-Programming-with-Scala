import HelperFunctions.{ getQuantity, compareDates,
                         getTransDate, getExpiryDate,
                         getProductName, getPrice,
                         getPaymentMethod, getChannel
                       }

object MainRules {

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
      0.00
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

  ////////////////

  // Qualify to Rule5
  def Q_paymentVisaRule(row: String): Boolean = {

    val paymentMethod = getPaymentMethod(row)

    if (paymentMethod == "Visa")
      true
    else
      false
  }

  // Calculate Rule5
  def C_paymentVisaRule(row: String): Double = {
    if (Q_paymentVisaRule(row))
      5
    else
      0.00
  }

  ////////////////

  // Qualify to Rule6
  def Q_AppRule(row: String): Boolean = {

    val channelType = getChannel(row)

    if (channelType == "App")
      true
    else
      false
  }

  // Calculate Rule6
  def C_AppRule(row: String): Double = {
    val q = getQuantity(row)
    if (Q_AppRule(row)) {
       val disc = ((q - 1) / 5 + 1) * 5.00
       disc
    } else
      0.00
  }

  /////////////////////////// Combine All Rules ///////////////////////////

  def allRules(q: String => Boolean, c: String => Double): String => Double = {
    row =>
      if (q(row))
        c(row)
      else
        0.00
  }

  val rule1 = allRules(Q_expiryRule, C_expiryRule)
  val rule2 = allRules(Q_specialDateRule, C_specialDateRule)
  val rule3 = allRules(Q_productNameRule, C_productNameRule)
  val rule4 = allRules(Q_productQuantityRule, C_productQuantityRule)
  val rule5 = allRules(Q_paymentVisaRule, C_paymentVisaRule)
  val rule6 = allRules(Q_AppRule, C_AppRule)


  val rules: List[String => Double] = List(rule1, rule2, rule3, rule4, rule5, rule6)


  def applyRules(row: String): List[Double] = {
    rules.map(rule => rule(row))
  }

  /////////////////////////// Calculate Discount & Get Top 2 ///////////////////////////

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

}