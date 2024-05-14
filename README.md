# Discount Rule Engine
# Problem Statement
A huge retail store wants a rule engine that qualifies orders’ transactions to discounts based
on a set of qualifying rules. And automatically calculates the proper discount based on some
calculation rules
# Programming Approach
The core logic is written in a pure functional manner.
Functional programming offers several advantages over imperative programming paradigms:
- Immutability
- Pure Functions
- Higher-order Functions
- Function Composition
# Problem Solving Approach

 
![Capture](https://github.com/israa-aly/Discount-Rule-Engine-By-Scala/assets/68852141/05ee34f9-1f21-46f7-ab86-abdf8d864c1b)

# Features
- Qualify orders' transactions for discounts based on predefined rules.
- Calculate the proper discount based on specific conditions such as product expiration date, product category, etc.
- Apply discounts to orders' final prices.
- Log engine events in a log file for monitoring and debugging purposes.
- Write processed orders to a CSV file for further analysis.
- Store processed orders in a database for future reference.
# Coding Steps
1) Read lines from the orders file and drop the header
2) Creating two Functions for each rule : qua_ruleName and cal_ruleName
   qua_ruleName : to check if the order meets the specified rule
   cal_ruleName: to calculate the discount based on the associated rule
3) Define a list of rules as a tuple of condition and calculation functions
4) Define a function to apply the rules to a list of orders and return processed orders:
- applyRules function takes the lines as a list of string
- iterates over each line
- applies the rules list for it 
- returns the corresponding discount if a rule applied
- returns the discounts as a list of double
-  Then it will take the top 2 discounts and calculates the avg of the two and returns 
the discount
- alculates the final price 
- Returns a list of processed lines
5) Processed Orders: call the apply rules function and return the result 
6) Define writeToDatabase function that will take the processed orders and insert them 
in a table at oracle data base 
 Load the Oracle JDBC driver
 Establish a connection to the database
 Prepare the SQL insert statement
 Create a prepared statement for batch insertion
 Iterate over the processed orders and add them to the batch
 Execute the batch insertion
 Close the prepared statement and the database connection
7) Define a writeToCSV Function that will write the processed orders to a csv file 
8) Define a function to write log messages during the running of the rule engine and 
write to a csv file
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
