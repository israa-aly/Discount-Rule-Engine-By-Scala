import java.io.{File, FileOutputStream, PrintWriter}
import scala.io.Source
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.sql.{Connection, DriverManager, PreparedStatement}
object Project extends App {
  // Define paths for input and output files
  val ordersFilePath = "src/main/resources/orders.csv"
  val csvFilePath = "src/main/output/processedOrders.csv"
  val logsFilePath = "src/main/output/logs.csv"
  // Read lines from the orders file and drop the header
  val lines = Source.fromFile(ordersFilePath).getLines().drop(1).toList
  // Create writers for the CSV and log files
  val csvWriter = new PrintWriter(new FileOutputStream(new File(csvFilePath), true))
  val logWriter = new PrintWriter(new FileOutputStream(new File(logsFilePath), true))
  // Log an info message
  writeLog("   Log Level: Event  Message:Starting app", logWriter)
  writeLog("   Log Level: Event   Message:Opening orders.csv", logWriter)
  // Define the date formatter for parsing dates
  val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  // Define the list of rules as a tuple of condition and calculation functions
  val rulesList: List[(String => Boolean, String => Double)] = List(
    (quaExpireDate, calExpireDate),
    (quaCheeseAndWine, calCheeseAndWine),
    (quaMarch, calMarch),
    (quaQty, calQty),
    (quaChannel, calChannel),
    (quaPayMethod, calPayMethod)
  )

  // Apply rules to the list of orders
  val processedOrders = applyRules(lines)

  // Write processed orders to the CSV file
  writeToCSV(processedOrders, csvWriter)

  // Write processed orders to the database
  writeToDatabase(processedOrders)

  // Close the CSV writer
  csvWriter.close()

  // Close the log writer
  logWriter.close()

  //Qualifications and Calculations Functions that represent the rules
  def quaExpireDate(order: String): Boolean = {
    val transDate = LocalDate.parse(order.substring(0, 10), dateFormatter)
    val expireDate = LocalDate.parse(order.split(",")(2), dateFormatter)
    ChronoUnit.DAYS.between(transDate, expireDate) <= 30
  }

  def calExpireDate(order: String): Double = {
    val transDate = LocalDate.parse(order.substring(0, 10), dateFormatter)
    val expireDate = LocalDate.parse(order.split(",")(2), dateFormatter)
    val remainingDays = ChronoUnit.DAYS.between(transDate, expireDate)
    if (remainingDays <= 29 && remainingDays >= 1) 30 - remainingDays else 0
  }

  // Define a function to check if an order contains Cheese or Wine
  def quaCheeseAndWine(order: String): Boolean = {
    val product = order.split(",")(1)
    product.startsWith("Cheese") || product.startsWith("Wine")
  }

  // Define a function to calculate the discount based on Cheese or Wine
  def calCheeseAndWine(order: String): Double = {
    val product = order.split(",")(1)
    if (product.startsWith("Cheese")) 10 else if (product.startsWith("Wine")) 5 else 0
  }

  // Define a function to check if an order's transaction date is on March 23rd
  def quaMarch(order: String): Boolean = order.substring(6, 10) == "03-23"

  // Define a function to calculate the discount for orders on March 23rd
  def calMarch(order: String): Double = 50

  // Define a function to check if an order's quantity is greater than 5
  def quaQty(order: String): Boolean = order.split(",")(3).toInt > 5

  // Define a function to calculate the discount based on quantity
  def calQty(order: String): Double = {
    val qty = order.split(",")(3).toInt
    if (qty >= 6 && qty <= 9) 5 else if (qty >= 10 && qty <= 14) 7 else 10
  }

  // Define a function to check if an order was made through the App channel
  def quaChannel(order: String): Boolean = order.split(",")(5) == "App"

  // Define a function to calculate the discount based on the channel
  def calChannel(order: String): Double = math.ceil(order.split(",")(3).toInt.toDouble / 5) * 5

  // Define a function to check if an order's payment method is Visa
  def quaPayMethod(order: String): Boolean = order.split(",")(6) == "Visa"

  // Define a function to calculate the discount for Visa payments
  def calPayMethod(order: String): Double = 5

  // Define a function to apply rules to a list of orders and return processed orders
  def applyRules(orders: List[String]): List[String] = {
    writeLog(s"   Log Level: Event   Message:Starting applying rules", logWriter)
    // Process each order
    val processedLines: List[String] = orders.map { line =>
      // Apply each rule and collect the results
      val appliedRules = rulesList.collect {
        case (condition, calculation) if condition(line) => calculation(line)
      }
      // Select the top two discounts
      val topTwo = appliedRules.sorted.takeRight(2)
      // Calculate the overall discount
      val discount = if (topTwo.nonEmpty) {
        if (topTwo.length == 1) {
          topTwo.head / 1.toDouble
        } else {
          topTwo.sum / 2.toDouble
        }
      } else {
        0.0
      }
      // Log whether the order has a discount
      if (discount != 0.0) {
        writeLog(s"   Log Level: Info   Message:This order has a discount= $discount", logWriter)
      } else {
        writeLog("   Log Level: Info   Message:This order has no discounts", logWriter)
      }
      // Calculate the final price after applying the discount
      val qty = line.split(",")(3).toInt
      val unitPrice = line.split(",")(4).toDouble
      val beforeDis = qty * unitPrice
      val finalPrice = beforeDis - (beforeDis * discount * 0.01)
      // Return the processed order line
      line + s",$discount,$finalPrice\n"
    }
    processedLines
  }

  // Define a function to write a log message
  def writeLog(message: String, writer: PrintWriter): Unit = {
    val formattedMessage = s"TimeStamp: ${java.time.LocalDateTime.now()} $message\n"
    writer.write(formattedMessage)
  }

  // Define a function to write orders to a CSV file
  def writeToCSV(orders: List[String], writer: PrintWriter): Unit = {
    orders.foreach(writer.write)
  }

  // Define a function to write orders to a database
  def writeToDatabase(data: List[String]): Unit = {
    // Database connection parameters
    val url = "jdbc:oracle:thin:@//localhost:1521/XE"
    val username = "israa"
    val password = "123"
    // Load the Oracle JDBC driver
    Class.forName("oracle.jdbc.driver.OracleDriver")
    // Establish a connection to the database
    val connection = DriverManager.getConnection(url, username, password)
    writeLog("Log Level: Event   Message:Opened DB connection", logWriter)
    // Prepare the SQL insert statement
    val insertStatement =
      """
        |INSERT INTO orders (order_date, expiry_date,
        |                   product_name, quantity, unit_price, channel, payment_method,
        |                   discount, final_price)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        |""".stripMargin
    // Create a prepared statement for batch insertion
    val preparedStatement = connection.prepareStatement(insertStatement)
    try {
      // Iterate over the processed orders and add them to the batch
      data.foreach { order =>
        val orderData = order.split(",")
        val orderDate = orderData(0).substring(0,10)
        val expiryDate = orderData(2)
        val productName = orderData(1)
        val quantity = orderData(3).toInt
        val unitPrice = orderData(4).toDouble
        val channel = orderData(5)
        val paymentMethod = orderData(6)
        val discount = orderData(7).toDouble
        val finalPrice = orderData(8).toDouble
        preparedStatement.setString(1, orderDate)
        preparedStatement.setString(2, expiryDate)
        preparedStatement.setString(3, productName)
        preparedStatement.setInt(4, quantity)
        preparedStatement.setDouble(5, unitPrice)
        preparedStatement.setString(6, channel)
        preparedStatement.setString(7, paymentMethod)
        preparedStatement.setDouble(8, discount)
        preparedStatement.setDouble(9, finalPrice)
        preparedStatement.addBatch()
      }
      // Execute the batch insertion
      preparedStatement.executeBatch()
    } catch {
      case e: Exception =>
        writeLog("   Log Level: Error  Message:Failed to close preparedStatement", logWriter)
    } finally {
      // Close the prepared statement and the database connection
      if (preparedStatement != null) preparedStatement.close()
      if (connection != null) connection.close()
      writeLog("   Log Level: Info   Message:Successfully inserted into database", logWriter)
      writeLog("   Log Level: Event   Message:Closed DB connection", logWriter)
    }
  }

  writeLog("   Log Level: Event   Message:Closing app", logWriter)
}

