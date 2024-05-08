# Retail Store Rule Engine
# Overview
This project implements a rule engine for a retail store that qualifies orders' transactions for discounts based on a set of qualifying rules. It automatically calculates the proper discount based on various criteria such as product expiration date, product category, purchase quantity, etc. The discounts are then applied to the orders' final prices, and the results are stored in a database.
# Features
- Qualify orders' transactions for discounts based on predefined rules.
- Calculate the proper discount based on specific conditions such as product expiration date, product category, etc.
- Apply discounts to orders' final prices.
- Log engine events in a log file for monitoring and debugging purposes.
- Write processed orders to a CSV file for further analysis.
- Store processed orders in a database for future reference.
# Usage
# 1) Prerequisites:
- Scala and sbt installed on your system.
- Oracle Database installed and running.
- Ensure that the JDBC driver for Oracle is available in your project's dependencies.
# 2) Setup:
- Clone the project repository to your local machine.
- Configure the database connection parameters in the writeToDatabase function.
- Update the input file path (ordersFilePath) and output file paths (csvFilePath, logsFilePath) in the Project.scala file if necessary.
# 3) Runn the Application
# 4) Viewing Results:
- After running the application, you can find the processed orders in the specified CSV file (output/processedOrders.csv).
- The engine events are logged in the output/logs.csv file for reference.
- Processed orders are also stored in the database for further analysis.
