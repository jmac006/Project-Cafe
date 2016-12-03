/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 * 
 * Made by: Justin Mac and Jonathan Tan
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.math.*;
import java.text.DecimalFormat;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Cafe {

	//login info for later use
	private static String authorizedUser = null;
	private static String userType = null;
   
	// Decimal formatter
	private static DecimalFormat priceFormat = new DecimalFormat("#0.00");

	// reference to physical database connection.
	private Connection _connection = null;

	// handling the keyboard inputs through a BufferedReader
	// This variable can be global for convenience.
	static BufferedReader in = new BufferedReader(
		new InputStreamReader(System.in));

	/**
	* Creates a new instance of Cafe
	*
	* @param hostname the MySQL or PostgreSQL server hostname
	* @param database the name of the database
	* @throws java.sql.SQLException when failed to make a connection.
	*/
	public Cafe (String dbname, String dbport) throws SQLException {

	System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://127.0.0.1:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");

			// obtain a physical connection
			this._connection = DriverManager.getConnection(url);
			System.out.println("Done");
		}catch (Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
			System.out.println("Make sure you started postgres on this machine");
			System.exit(-1);
		}//end catch
	}//end Cafe

	/**
	* Method to execute an update SQL statement.  Update SQL instructions
	* includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	*
	* @param sql the input SQL string
	* @throws java.sql.SQLException when update failed
	*/
	public void executeUpdate (String sql) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
		stmt.close ();
	}//end executeUpdate

	/**
	* Method to execute an input query SQL instruction (i.e. SELECT).  This
	* method issues the query to the DBMS and outputs the results to
	* standard out.
	*
	* @param query the input query string
	* @return the number of rows returned
	* @throws java.sql.SQLException when failed to execute the query
	*/
	public int executeQueryAndPrintResult (String query) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		** obtains the metadata object for the returned result set.  The metadata
		** contains row and column info.
		*/
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;

		// iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
				}
				System.out.println();
				outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}//end executeQuery

	/**
	* Method to execute an input query SQL instruction (i.e. SELECT).  This
	* method issues the query to the DBMS and returns the results as
	* a list of records. Each record in turn is a list of attribute values
	*
	* @param query the input query string
	* @return the query result as a list of records
	* @throws java.sql.SQLException when failed to execute the query
	*/
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		// creates a statement object 
		Statement stmt = this._connection.createStatement (); 
 
		// issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
 
		/* 
		** obtains the metadata object for the returned result set.  The metadata 
		** contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
 
		// iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult

	/**
	* Method to execute an input query SQL instruction (i.e. SELECT).  This
	* method issues the query to the DBMS and returns the number of results
	*
	* @param query the input query string
	* @return the number of rows returned
	* @throws java.sql.SQLException when failed to execute the query
	*/
	public int executeQuery (String query) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		// iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}

	/**
	* Method to fetch the last value from sequence. This
	* method issues the query to the DBMS and returns the current 
	* value of sequence used for autogenerated keys
	*
	* @param sequence name of the DB sequence
	* @return current value of a sequence
	* @throws java.sql.SQLException when failed to execute the query
	*/
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
	
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next())
			return rs.getInt(1);
		return -1;
	}

	/**
	* Method to close the physical connection if it is open.
	*/
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}
		catch (SQLException e){
		// ignored.
		}//end try
	}//end cleanup

   	/**
	* The main execution method
	*
	* @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	*/
	public static void main (String[] args) {
		if (args.length != 2) {
			System.err.println (
			"Usage: " +
			"java [-classpath <classpath>] " +
			Cafe.class.getName () +
			" <dbname> <port>");
		return;
	}//end if

	Greeting();
	Cafe esql = null;
	try{
		// use postgres JDBC driver.
		Class.forName ("org.postgresql.Driver").newInstance ();
		// instantiate the Cafe object and creates a physical
		// connection.
		String dbname = args[0];
		String dbport = args[1];
		esql = new Cafe (dbname, dbport);

		boolean keepon = true;
		while(keepon) {
			// These are sample SQL statements
			System.out.println("MAIN MENU");
			System.out.println("---------");
			System.out.println("1. Create user");
			System.out.println("2. Log in");
			System.out.println("9. < EXIT");
			authorizedUser = null;
			switch (readChoice()){
				case 1: CreateUser(esql); break;
				case 2: authorizedUser = LogIn(esql); break;
				case 9: keepon = false; break;
				default : System.out.println("Unrecognized choice!"); break;
			}//end switch
			if (authorizedUser != null) {
			boolean usermenu = true;
			String user_type = find_type(esql);
			userType = user_type;
			switch (user_type){
		case "Customer": 
		  while(usermenu) {
					System.out.println("MAIN MENU");
					System.out.println("---------");
					System.out.println("1. Browse Menu by ItemName");
					System.out.println("2. Browse Menu by Type");
					System.out.println("3. Add Order");
					System.out.println("4. Update Order");
					System.out.println("5. View Order History");
					System.out.println("6. View Order Status");
					System.out.println("7. Update User Info");
					System.out.println(".........................");
					System.out.println("9. Log out");
					switch (readChoice()){
						case 1: BrowseMenuName(esql); break;
						case 2: BrowseMenuType(esql); break;
						case 3: AddOrder(esql); break;
						case 4: UpdateOrder(esql); break;
						case 5: ViewOrderHistory(esql); break;
						case 6: ViewOrderStatus(esql); break;
						case 7: UpdateUserInfo(esql); break;
						case 9: usermenu = false; break;
						default : System.out.println("Unrecognized choice!"); break;
				}//end switch
		} break;
		case "Employee": 
			while(usermenu) {
					System.out.println("MAIN MENU");
					System.out.println("---------");
					System.out.println("1. Browse Menu by ItemName");
					System.out.println("2. Browse Menu by Type");
					System.out.println("3. Add Order");
					System.out.println("4. Update Order");
					System.out.println("5. View Current Orders");
					System.out.println("6. View Order Status");
					System.out.println("7. Update User Info");
					System.out.println(".........................");
					System.out.println("9. Log out");
					switch (readChoice()){
						case 1: BrowseMenuName(esql); break;
						case 2: BrowseMenuType(esql); break;
						case 3: AddOrder(esql); break;
						case 4: EmployeeUpdateOrder(esql); break;
						case 5: ViewCurrentOrder(esql); break;
						case 6: ViewOrderStatus(esql); break;
						case 7: UpdateUserInfo(esql); break;
						case 9: usermenu = false; break;
						default : System.out.println("Unrecognized choice!"); break;
			}//end switch
		} break;
		case "Manager ": 
			while(usermenu) {
					System.out.println("MAIN MENU");
					System.out.println("---------");
					System.out.println("1. Browse Menu by ItemName");
					System.out.println("2. Browse Menu by Type");
					System.out.println("3. Add Order");
					System.out.println("4. Update Order");
					System.out.println("5. View Current Orders");
					System.out.println("6. View Order Status");
					System.out.println("7. Update User Info");
					System.out.println("8. Update Menu");
					System.out.println(".........................");
					System.out.println("9. Log out");
					switch (readChoice()){
						case 1: BrowseMenuName(esql); break;
						case 2: BrowseMenuType(esql); break;
						case 3: AddOrder(esql); break;
						case 4: EmployeeUpdateOrder(esql); break;
						case 5: ViewCurrentOrder(esql); break;
						case 6: ViewOrderStatus(esql); break;
						case 7: ManagerUpdateUserInfo(esql); break;
						case 8: UpdateMenu(esql); break;
						case 9: usermenu = false; break;
						default : System.out.println("Unrecognized choice!"); break;
				}//end switch
			} break;
		}//end switch
		}//end if
		}//end while
		}catch(Exception e) {
			System.err.println (e.getMessage ());
		}finally{
		// make sure to cleanup the created table and close the connection.
		try{
			if(esql != null) {
				System.out.print("Disconnecting from database...");
				esql.cleanup ();
				System.out.println("Done\n\nBye !");
			}//end if
		}catch (Exception e) {
			// ignored.
		}//end try
		}//end try
	}//end main

/*
* Custom functions
*
*
* Takes in double value and rounds value up based on specified precision
* @return new double
*/
	public static double RoundUp(double value, int precision)
	{
		if (precision < 0)
		{
			throw new IllegalArgumentException();
		}
		else
		{
			BigDecimal result = new BigDecimal(value);
			result = result.setScale(precision, RoundingMode.HALF_UP);   
			return result.doubleValue();
		}
	}
   
	/*
	* Determines if authorizedUser is a customer
	* @return true if authorizedUser is an customer, false otherwise
	**/
	public static boolean IsCustomer()
	{
		return IsEqual(userType, "Customer");
	}
   
	/*
	* Determines if authorizedUser is an employee
	* @return true if authorizedUser is an employee, false otherwise
	**/
	public static boolean IsEmployee()
	{
		return IsEqual(userType, "Employee");
	}
   
	/*
	* Determines if authorizedUser is a manager
	* @return true if authorizedUser is a manager, false otherwise
	**/   
	public static boolean IsManager()
	{
		return IsEqual(userType, "Manager");
	}

	/*
	* Determines if item with passed in itemName exists in database
	* @param itemName
	* @return true if itemName exists, false otherwise
	**/
	public static boolean ItemNameDoesExist(Cafe esql, String itemName)
	{
		try
		{
			String query = String.format("SELECT * FROM Menu WHERE itemName = '%s'", itemName);
			List<List<String>> result = esql.executeQueryAndReturnResult(query);

			return result.size() > 0;
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage());
			return false;
		}
	} 
	/*
	* Gets all order ids from last 24 hours if user is manager or employee
	* @return result list of orderIds, sorted by most recent order to oldest
	**/
	public static List<Integer> GetAllOrderIdsFromPast24Hours(Cafe esql, boolean unpaidOnly)
	{
		if (IsManager() || IsEmployee())
		{
			try
			{
				List<Integer> result = new ArrayList<Integer>();
				String query = "";

				if (unpaidOnly)
				{
					query = String.format("SELECT orderid FROM Orders WHERE timeStampRecieved >= NOW() - '1 day'::INTERVAL AND paid = FALSE;");
				}
				else
				{
					query = String.format("SELECT orderid FROM Orders WHERE timeStampRecieved >= NOW() - '1 day'::INTERVAL;");
				}
				List<List<String>> orderIdList = esql.executeQueryAndReturnResult(query);
			   
				if (orderIdList.size() > 0)
				{
					for(List<String> orderId : orderIdList)
					{
						int currOrderId = Integer.parseInt(orderId.get(0));
						result.add(currOrderId);
					}
				}
			   
				Collections.sort(result);
				//~ Collections.reverse(result);
			   
				return result;
			}
			catch (Exception e)
			{
				System.err.println (e.getMessage ());
				return null;
			}
		}
		else
		{
			System.out.println("Error: Access denied. Must be employee or manager level.\n");
			return null;
		}
	}
   
	/*
	* Gets last 5 order ids of orders made by user
	*   If unpaidOnly = true, only gets last 5 unpaid orders
	*   Else gets any last 5 orders
	* @param unpaidOnly
	* @return result list of orderIds, sorted by most recent order to oldest
	**/
	public static List<Integer> GetRecentOrderIds(Cafe esql, boolean unpaidOnly)
	{
		if (authorizedUser != null)
		{
			try
			{
				List<Integer> result = new ArrayList<Integer>();
				String query = "";
				if (unpaidOnly)
				{
					query = String.format("SELECT orderid FROM Orders WHERE login = '%s' AND paid = false", authorizedUser);
				}
				else
				{
					query = String.format("SELECT orderid FROM Orders WHERE login = '%s'", authorizedUser);
				}
				List<List<String>> orderIdList = esql.executeQueryAndReturnResult(query);

				if (orderIdList.size() > 0)
				{
					for(List<String> orderId : orderIdList)
					{
						int currOrderId = Integer.parseInt(orderId.get(0));
						result.add(currOrderId);
					}
					//push back all the order ID's and pop the front until you have the 5 most recent order IDs
					Collections.sort(result);
					while (result.size() > 5)
					{
						result.remove(0);
					}
					//~ Collections.reverse(result);
				}
				return result;
			}
			catch (Exception e)
			{
				System.err.println (e.getMessage ());
				return null;
			}
		}
		else
		{
			System.out.println("HELLO!");
			return null;
		}
	}
   
   /*
	* Prints order summaries of all orders in last 24 hours
	*   If unpaidOnly = true, only prints unpaid orders
	*   Else prints all orders
	* @return
	**/   
   public static void PrintAllOrderHistoryFromPast24Hours(Cafe esql, boolean unpaidOnly)
   {
	   List<Integer> orderIdList = GetAllOrderIdsFromPast24Hours(esql, unpaidOnly);
	   
	   if (orderIdList != null)
	   {
		   System.out.println("--------------------------------");
		   System.out.println("Order History from last 24 hours");
		   System.out.println("--------------------------------");
		   for(Integer currOrderId : orderIdList)
		   {
			   PrintOrderSummary(esql, currOrderId);
		   }
	   }        
	   return;
   }

   /*
	* Prints order summaries of recent orders
	*   If unpaidOnly = true, only prints recent unpaid orders
	*   Else prints all recent orders
	* @return
	**/
	public static void PrintRecentOrderHistory(Cafe esql, boolean unpaidOnly)
	{
		List<Integer> orderIdList = GetRecentOrderIds(esql, unpaidOnly);
		if (orderIdList != null)
		{
			System.out.println("--------------------");
			System.out.println("Recent Order History");
			System.out.println("--------------------");
		   
			for(Integer currOrderId : orderIdList)
			{
				PrintOrderSummary(esql, currOrderId);
			}
		}        
		return;
	}
   
	/*
	* Gets the most recent order id
	*   If unpaidOnly = true, gets most recent unpaid order id
	*   Else gets most recent order id
	* @return orderId
	**/
	public static Integer GetMostRecentOrderId(Cafe esql, boolean unpaidOnly)
	{
		List<Integer> orderIdList = GetRecentOrderIds(esql, unpaidOnly);
	   
		return orderIdList.get(0);
	}
	
	/*
	* Prints item status information by itemName and orderId
	* @param orderId, itemName
	**/	
	public static void PrintItemStatusInformation(Cafe esql, int orderId, String itemName)
	{
		try
		{
			String query = String.format("SELECT * FROM ItemStatus WHERE orderid = '%d' AND itemName = '%s';", orderId, itemName);
			List<List<String>> items = esql.executeQueryAndReturnResult(query);
			
			if (items.size() > 0)
			{
				for(List<String> item : items)
				{
					String status = item.get(3).trim();
					String comment = item.get(4).trim();
					String timestamp = item.get(2).trim();
					double price = GetItemPrice(esql, itemName);
					
					System.out.println(itemName);
					System.out.println("--------------------");
					System.out.println("Comment: " + comment);
					System.out.println("Status: " + status);
					System.out.println("Price: $" + priceFormat.format(price));
					System.out.println("Last updated: " + timestamp + "\n");
				}
			}
			else
			{
				System.out.printf("\nError: '%s' not found in order.\n", itemName);
			}
			return;
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return;
		}
	}
	
	/*
	* Prints item menu information by itemName
	* @param itemName
	**/
	public static void PrintItemMenuInformation(Cafe esql, String itemName)
	{
		try
		{
			String query = String.format("SELECT * FROM Menu WHERE itemName = '%s';", itemName);
			List<List<String>> itemsList = esql.executeQueryAndReturnResult(query);
		   
			if (itemsList.size() > 0)
			{
				for(List<String> items : itemsList)
				{
					String name = items.get(0).trim();
					String price = items.get(2).trim();
					String description = items.get(3).trim();
					String imageUrl = items.get(4).trim();
					double realPrice = Double.parseDouble(price);
					realPrice = RoundUp(realPrice, 2);

					System.out.println(name);
					System.out.println("--------------------");
					System.out.println("Description: " + description);
					System.out.println("Price: $" + priceFormat.format(realPrice) + "\n");
				}
			}
			else
			{
				System.out.printf("\nError: '%s' not found.\n", itemName);
			}
			return;
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return;
		}
	}
	
	/*
	* Determines if order is paid for
	* @param orderId
	* @return Return true if paid for, false otherwise
	**/
	public static boolean DidPayOrder(Cafe esql, int orderId)
	{
		try
		{
			String query = String.format("SELECT paid FROM Orders WHERE orderid = '%d'", orderId);
			List<List<String>> results = esql.executeQueryAndReturnResult(query);
			
			String result = results.get(0).get(0);
			
			return IsEqual(result, "t");
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			
			return false;
		}
	}
	
	/*
	* Sets order paid to true or false depending on boolean didPay
	* @param orderId, didPay
	**/	
	public static void SetOrderPaid(Cafe esql, int orderId, boolean didPay)
	{
		try
		{
			String query;
			if (didPay)
			{
				query = String.format("UPDATE Orders SET paid = 'TRUE' WHERE orderid = '%d'", orderId);
			}
			else
			{
				query = String.format("UPDATE Orders SET paid = 'FALSE' WHERE orderid = '%d'", orderId);
			}
			esql.executeUpdate(query);
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return;
		}		
	}
	
	/*
	* Gets all ItemStatus corresponding to orderId
	* @param orderId
	* @return List<List<String>> of ItemStatus
	**/	
	public static List<List<String>> GetAllOrderItemStatuses(Cafe esql, int orderId)
	{
		try
		{
			String query = String.format("SELECT * FROM ItemStatus WHERE orderid = '%d';", orderId);
			
			return esql.executeQueryAndReturnResult(query);
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return null;
		}    		
	}
	
	/*
	* Sets ItemStatus status based on ready boolean
	* 	If ready = true; set status to "Ready"
	* 	else set status to "In progress"
	* @param orderId, itemName, ready
	**/
	public static void SetItemStatusStatus(Cafe esql, int orderId, String itemName, boolean ready)
	{
		try
		{
			String timestamp = GetCurrentTimestamp();
			
			String query;
			if (ready)
			{
				query = String.format("UPDATE ItemStatus SET lastUpdated = '%s', status = 'Ready' WHERE orderid = '%d' AND itemName = '%s'", timestamp, orderId, itemName);
			}
			else
			{
				query = String.format("UPDATE ItemStatus SET lastUpdated = '%s', status = 'In progress' WHERE orderid = '%d' AND itemName = '%s'", timestamp, orderId, itemName);
			}
			esql.executeUpdate(query);
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return;			
		}
	}
	
	/*
	* Determines if an order is ready (AKA all ItemStatus statuses are "Ready")
	* @param orderId
	* @return True if ready, false otherwise
	**/
	public static boolean IsOrderReady(Cafe esql, int orderId)
	{
		List<List<String>> itemStatuses = GetAllOrderItemStatuses(esql, orderId);
		
		for(List<String> itemStatus : itemStatuses)
		{
			String status = itemStatus.get(3);
			
			if (!IsEqual(status, "Ready"))
			{
				return false;
			}
		}
		return true;
	}
	
	/*
	* Gets the Paid status, timeStamp, and Total of an Order
	* @param orderId
	* @return List<String> containing Paid status, timeStamp, total
	**/	
	public static List<String> GetPaidTimestampReceivedAndTotalOfOrder(Cafe esql, int orderId)
	{
		try
		{
			String query = String.format("SELECT paid, timeStampRecieved, total FROM Orders WHERE orderid = '%d'", orderId);
			List<List<String>> results = esql.executeQueryAndReturnResult(query);
			
			if (results.size() > 0)
			{
				String paidStatus = results.get(0).get(0);
				if (IsEqual(paidStatus, "t"))
				{
					paidStatus = "Paid";
				}
				else
				{
					paidStatus = "Unpaid";
				}
				
				String timestamp = results.get(0).get(1);
				String total = results.get(0).get(2);
				
				List<String> result = new ArrayList<String>();
				result.add(paidStatus);
				result.add(timestamp);
				result.add(total);
				
				return result;
			}
			else
			{
				System.out.println("Error: Order #" + orderId + " not found.");
				return null;
			}
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return null;
		}
	}

	/*
	* Prints order summary by orderId
	* @param orderId
	**/
	public static void PrintOrderSummary(Cafe esql, int orderId)
	{
		try
		{
			List<String> orderInfo = GetPaidTimestampReceivedAndTotalOfOrder(esql, orderId);
			
			if (orderInfo != null && orderInfo.size() == 3)
			{
				String paymentStatus = orderInfo.get(0);
				String timestamp = orderInfo.get(1);
				String total = orderInfo.get(2);
				
				double realTotal = Double.parseDouble(total);
				realTotal = RoundUp(realTotal, 2);
				
				String orderStatus;
				if (IsOrderReady(esql, orderId))
				{
					orderStatus = "Ready";
				}
				else
				{
					orderStatus = "Not ready";
				}
				
				System.out.println("----------------------");
				System.out.println("Order Summary");
				System.out.println("----------------------");
				System.out.println("OrderId #" + orderId);
				System.out.println("Total: $" + priceFormat.format(realTotal)); 
				System.out.println("Payment Status: " + paymentStatus);
				System.out.println("Order Status: " + orderStatus);
				System.out.println("Timestamp: " + timestamp + "\n");    
				
				String query = String.format("SELECT * FROM ItemStatus WHERE orderid = '%d'", orderId);
				List<List<String>> orderList = esql.executeQueryAndReturnResult(query);
				 
				if (orderList.size() > 0)
				{
					for(List<String> order : orderList)
					{
						String itemName = order.get(1);
						PrintItemStatusInformation(esql, orderId, itemName);
					}
				}
				 
				return;
			}
			else
			{
				System.out.println("Error: An error occurred trying to print Order #" + orderId + "'s summary.");
				return; 
			}
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return;
		}
	}
   
	/*
	* Outputs messageToUser and returns single line user input
	* @param messageToUser
	* @return string input
	**/
	public static String GetUserInput(String messageToUser)
	{
		System.out.print(messageToUser);
		try
		{
			String input = in.readLine();
			return input;
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return "";
		}
	}
	
	/*
	* Removes empty/blank characters from beginning and end of strings
	* and determines if resulting strings are equal
	* @param source, target
	* @return true if strings are equals, false otherwise
	**/	
	public static boolean IsEqual(String source, String target)
	{
		String a = source.trim();
		String b = target.trim();
		
		return a.equals(b);
	}
	
	/*
	* Determines if order belongs to customer
	* @param orderId
	* @return true if order belongs to customer, false otherwise
	**/
	public static boolean OrderBelongsToCustomer(Cafe esql, int orderId)
	{
		try
		{
			if (authorizedUser != null)
			{
				String query = String.format("SELECT login FROM Orders WHERE orderid = '%d'", orderId);
				List<List<String>> result = esql.executeQueryAndReturnResult(query);
				
				if (result.size() > 0)
				{
					String login = result.get(0).get(0);
					
					return IsEqual(authorizedUser, login);
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return false;
		}		
	}
   
	/*
	* Determines if current customer can edit order.
	* User can edit order...
	*   If user is an authorized user
	*   and if order belongs to user and order is not paid for
	* @param orderId
	* @return null if customer can edit order, string message otherwise
	**/
	public static String CustomerCanEditOrder(Cafe esql, int orderId)
	{
		try
		{
			if (authorizedUser != null)
			{
				String query = String.format("SELECT login, paid FROM Orders WHERE orderid = '%d'", orderId);
				List<List<String>> result = esql.executeQueryAndReturnResult(query);

				if (result.size() > 0)
				{
					String login = result.get(0).get(0);
					String paid = result.get(0).get(1);
						
					if (IsEqual(authorizedUser, login))
					{
						if (!DidPayOrder(esql, orderId))
						{
							return null;
						}
						else
						{
							return String.format("Error: Cannot edit order #%d. Order has already been paid for.\n", orderId);
						}
					}
					else
					{
						return String.format("Error: Cannot edit order #%d. Order does not belong to you.\n", orderId);
					}
				}
				else
				{
					return String.format("Error: Cannot find order #%d. Please check your orderId and try again.\n", orderId);
				}
			}
			else
			{
				return String.format("Error: User not authorized.\n");
			}
		}
		catch(Exception e)
		{
			return e.getMessage();
		}
	}
   
	/*
	* Gets the price of item
	* @param itemName
	* @return item price if found, -1 otherwise
	**/
	public static double GetItemPrice(Cafe esql, String itemName)
	{
		try
		{
			String query = String.format("SELECT price FROM Menu WHERE itemName = '%s'", itemName);
			List<List<String>> price = esql.executeQueryAndReturnResult(query);
		   
			if (price.size() > 0)
			{
				double result = Double.parseDouble(price.get(0).get(0));
				return RoundUp(result, 2);
			}
			else
			{
				return -1;
			}
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return -1;
		}
	}
   
	/*
	* Gets the total of order
	* @param orderId
	* @return order total if found, -1 otherwise
	**/   
	public static double GetOrderTotal(Cafe esql, int orderId)
	{
		try
		{
			String query = String.format("SELECT total FROM Orders WHERE orderid = '%d'", orderId);
			List<List<String>> total = esql.executeQueryAndReturnResult(query);
		   
			if (total.size() > 0)
			{
				double result = Double.parseDouble(total.get(0).get(0));
				return RoundUp(result, 2);
			}
			else
			{
				return -1;
			}
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return -1;
		}       
	}
	
	/*
	* Gets the comment of an ItemStatus
	* @param orderId, itemName
	* @return String comment if found, null otherwise
	**/   
	public static String GetItemStatusComment(Cafe esql, int orderId, String itemName)
	{
		try
		{
			String query = String.format("SELECT comments FROM ItemStatus WHERE orderid = '%d' AND itemName = '%s';", orderId, itemName);
			List<List<String>> result = esql.executeQueryAndReturnResult(query);
		   
			if (result.size() > 0)
			{
				return result.get(0).get(0).trim();
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			return e.getMessage();
		}    
	}
   
	/*
	* Set an order's total
	* @param orderId, total
	**/      
	public static void SetOrderTotal(Cafe esql, int orderId, double total)
	{
		double prevOrderTotal = GetOrderTotal(esql, orderId);
		
		if (prevOrderTotal != total)
		{
			try
			{
				String query = String.format("UPDATE Orders SET total = '%f' WHERE orderid = '%d'", total, orderId);
				esql.executeUpdate(query);
				  
				return;
			}
			catch (Exception e)
			{
				System.err.println (e.getMessage ());
				return;
			}
		}
	}
   
	/*
	* Inserts a new order into database and returns the orderId
	* @param 
	* @return OrderId if successful, -1 otherwise
	**/  
	public static Integer CreateNewOrder(Cafe esql)
	{
		if (authorizedUser != null)
		{
			try
			{
				String timestamp = GetCurrentTimestamp();
				 
				String query = "INSERT INTO ORDERS (login, paid, timeStampRecieved, total) VALUES ('" + authorizedUser + "', 'FALSE', '" + timestamp + "', '0.0');";
				esql.executeUpdate(query);
				 
				return GetMostRecentOrderId(esql, true);
			}
			catch (Exception e)
			{
				System.err.println (e.getMessage ());
				return -1;
			}
		}
		else
		{
			System.out.println("Error: User not authorized.\n");
			return -1;
		}
	}
	
	/*
	* Gets the current timestamp
	**/    
	public static String GetCurrentTimestamp()
	{
		Date date = new Date();
		long t = date.getTime();
		java.sql.Timestamp timestamp = new java.sql.Timestamp(t);
		
		return timestamp.toString();
	}
   
	/*
	* Adds Item to Order
	* @param orderId, itemName, comment
	**/      
	public static void AddItemStatusToOrder(Cafe esql, int orderId, String itemName, String comment)
	{
		if (ItemNameDoesExist(esql, itemName))
		{
			String timestamp = GetCurrentTimestamp();
			 
			try
			{
				String query = String.format("INSERT INTO ItemStatus (orderid, itemName, lastUpdated, status, comments) VALUES ('%d','%s','%s','%s','%s')", orderId, itemName, timestamp, "In progress", comment);
				esql.executeUpdate(query);
				 
				if (ItemStatusDoesExist(esql, orderId, itemName))
				{
					double itemPrice = GetItemPrice(esql, itemName);
					double newTotal = GetOrderTotal(esql, orderId) + itemPrice;
					
					SetOrderTotal(esql, orderId, newTotal);
					
					System.out.printf("\nSuccess: %s has been added to your order.\n", itemName);
				}
				else
				{
					System.out.printf("\nError: Could not add %s to your order.\n", itemName);
				}
				return;
			}
			catch (Exception e)
			{
				System.err.println (e.getMessage ());
				return;
			}
		}
		else
		{
			System.out.println("Error: " + itemName + " could not be found.\n");
			return;
		}
	}
   
	/*
	* Removes Item from Order
	* @param orderId, itemName
	**/      
	public static void RemoveItemStatusFromOrder(Cafe esql, int orderId, String itemName)
	{
		if (ItemStatusDoesExist(esql, orderId, itemName))
		{
			try
			{
				String query = String.format("DELETE FROM ItemStatus WHERE orderid = '%d' AND itemName = '%s'", orderId, itemName);
				esql.executeUpdate(query);

				if (!ItemStatusDoesExist(esql, orderId, itemName))
				{
					double itemPrice = GetItemPrice(esql, itemName);
					double newTotal = GetOrderTotal(esql, orderId) - itemPrice;
					
					SetOrderTotal(esql, orderId, newTotal);
					
					System.out.printf("\nSuccess: '%s' has been removed from your order.\n", itemName);
				}
				else
				{
					System.out.printf("\nError: Could not remove '%s' from your order.\n", itemName);
				}
				return;
			}
			catch (Exception e)
			{
				System.err.println (e.getMessage ());
				return;
			}
		}
		else
		{
			System.out.println("Error: " + itemName + " could not be found.\n");
			return;
		}
	}
   
   /*
	* Cascade deletes an order
	*   Deletes all ItemStatus rows associated with order then deletes order row
	* @param orderId
	* @return true if successfully deleted order, false otherwise
	**/      
	public static boolean CascadeDeleteOrder(Cafe esql, int orderId)
	{
		if (DeleteItemStatusByOrderId(esql, orderId))
		{	
			try
			{
				String query = String.format("DELETE FROM Orders WHERE orderid = '%d'", orderId);
				esql.executeUpdate(query);
				 
				return !OrderDoesExist(esql, orderId);
			}
			catch (Exception e)
			{
				System.err.println (e.getMessage ());
				return false;
			}
		}
		else
		{
			return false;
		}
	}
   
	/*
	* Checks if order exists by orderId
	* @param orderId
	* @return true if order exists, false otherwise
	**/      
	public static boolean OrderDoesExist(Cafe esql, int orderId)
	{
		try
		{
			String query = String.format("SELECT CAST(CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END AS BIT) FROM Orders WHERE orderid = '%d';", orderId);
			List<List<String>> results = esql.executeQueryAndReturnResult(query);
			
			String result = results.get(0).get(0);
			
			return IsEqual(result, "1");
		}
		catch (Exception e)
		{
				System.err.println (e.getMessage ());
				return false;
		}
	}
   
	/*
	* Checks if item statuses do exist by orderId
	* @param orderId
	* @return true if item statuses exist, false otherwise
	**/         
	public static boolean OrderHasItemStatuses(Cafe esql, int orderId)
	{
		try
		{
			String query = String.format("SELECT CAST(CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END AS BIT) FROM ItemStatus WHERE orderid = '%d';", orderId);
			List<List<String>> results = esql.executeQueryAndReturnResult(query);
			 
			String result = results.get(0).get(0);
			
			return IsEqual(result, "1");
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return false;
		}
	}
   
	/*
	* Checks if item status does exist for orderId and itemName
	* @param orderId, itemName
	* @return true if item statuses exist, false otherwise
	**/          
	public static boolean ItemStatusDoesExist(Cafe esql, int orderId, String itemName)
	{
		try
		{
			String query = String.format("SELECT CAST(CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END AS BIT) FROM ItemStatus WHERE orderid = '%d' AND itemName = '%s';", orderId, itemName);
			List<List<String>> results = esql.executeQueryAndReturnResult(query);
		   
			String result = results.get(0).get(0);
			
			return IsEqual(result, "1");
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return false;
		}    
	}

	/*
	* Deletes item status by order id
	* @param orderId
	* @return true if successfully deleted item status, false otherwise
	**/      
	public static boolean DeleteItemStatusByOrderId(Cafe esql, int orderId)
	{
		try
		{
			String query = String.format("DELETE FROM ItemStatus WHERE orderid = '%d'", orderId);
			esql.executeUpdate(query);
			   
			return !OrderHasItemStatuses(esql, orderId);
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return false;
		}
	}
	
	/*
	* Sets an ItemStatus's comment
	* @param orderId, itemName, comment
	**/      
	public static void SetItemStatusComment(Cafe esql, int orderId, String itemName, String comment)
	{
		try
		{
			String timestamp = GetCurrentTimestamp();
			
			String query = String.format("UPDATE ItemStatus SET lastUpdated = '%s', comments = '%s' WHERE orderid = '%d' AND itemName = '%s';", timestamp, comment, orderId, itemName);
			esql.executeUpdate(query);
			  
			return;
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return;
		}
	}
	
	/*
	* Tries to set an ItemStatus's comment
	* @param orderId, itemName
	**/   	
	public static void TrySetItemStatusComment(Cafe esql, int orderId, String itemName)
	{
		if (ItemStatusDoesExist(esql, orderId, itemName))
		{
			String oldComment = GetItemStatusComment(esql, orderId, itemName);
			
			System.out.println("Current Comment: ");
			System.out.println(oldComment + "\n");
			
			String newComment = GetUserInput("Enter new comment ('q' to cancel): ");
			
			if (IsEqual(newComment, "q"))
			{
				return;
			}
			else if (!IsEqual(oldComment, newComment))
			{
				SetItemStatusComment(esql, orderId, itemName, newComment);
			}
			return;
		}
		else
		{
			System.out.printf("\nError: '%s' not found in order.\n", itemName);
			return;
		}
	}

	/*
	* End custom functions
	**/
	
	/*
	 * START NEW CUSTOM FUNCTIONS
	 **/
	 
	/*
	* Gets user's password
	* @param login
	* @return string password
	**/   		 
	public static String GetUserPassword(Cafe esql, String login)
	{
		try
		{
			String query = String.format("SELECT password FROM Users WHERE login = '%s'", login);
			List<List<String>> results = esql.executeQueryAndReturnResult(query);
		   
			if (results.size() > 0)
			{
				return results.get(0).get(0).trim();
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return null;
		}
	}
	
	/*
	* Gets user's phone number
	* @param login
	* @return string phone number
	**/ 	
	public static String GetUserPhoneNumber(Cafe esql, String login)
	{
		try
		{
			String query = String.format("SELECT phoneNum FROM Users WHERE login = '%s'", login);
			List<List<String>> results = esql.executeQueryAndReturnResult(query);
		   
			if (results.size() > 0)
			{
				return results.get(0).get(0).trim();
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return null;
		}
	}	
	
	/*
	* Gets user's favitems
	* @param login
	* @return string favitems
	**/ 	
	public static String GetUserFavItems(Cafe esql, String login)
	{
		try
		{
			String query = String.format("SELECT favItems FROM Users WHERE login = '%s'", login);
			List<List<String>> results = esql.executeQueryAndReturnResult(query);
		   
			if (results.size() > 0)
			{
				return results.get(0).get(0).trim();
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return null;
		}
	}
	
	/*
	* Gets user's information (phone number + favItems)
	* @param login
	* @return List<String> phone number, favItems
	**/ 
	public static List<String> GetUserInformation(Cafe esql, String login)
	{
		try
		{
			String query = String.format("SELECT * FROM Users WHERE login = '%s'", login);
			List<List<String>> results = esql.executeQueryAndReturnResult(query);
		   
			if (results.size() > 0)
			{
				String phoneNumber = results.get(0).get(1);
				String favItems = results.get(0).get(3);
				
				List<String> result = new ArrayList<String>();
				result.add(phoneNumber);
				result.add(favItems);
				
				return result;
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return null;
		}
	}
	
	/*
	* Prints the users information (Account type, phone number, favorite items)
	* @param login
	**/ 	
	public static void PrintUserInformation(Cafe esql, String login)
	{
		List<String> userInfo = GetUserInformation(esql, login);
		
		if (userInfo != null && userInfo.size() > 0)
		{
			System.out.println("--------------------");
			System.out.println("   " + login + " Profile");
			System.out.println("--------------------");
			
			String phoneNumber = userInfo.get(0);
			String favItems = userInfo.get(1);
			String type = GetUserType(esql, login);
			
			System.out.println("Account type: " + type);
			System.out.println("Phone number: " + phoneNumber);
			System.out.println("Favorite items: " + favItems);
		}        
		return;		
	}
	
	/*
	* Sets user's phone number
	* @param login, phone number
	**/ 	
	public static void SetUserPhoneNumber(Cafe esql, String login, String phoneNumber)
	{
		String prevPhoneNumber = GetUserPhoneNumber(esql, login);
		
		if (!IsEqual(prevPhoneNumber, phoneNumber))
		{
			try
			{
				String query = String.format("UPDATE Users SET phoneNum = '%s' WHERE login = '%s'", phoneNumber, login);
				esql.executeUpdate(query);
				  
				return;
			}
			catch (Exception e)
			{
				System.err.println (e.getMessage ());
				return;
			}
		}
	}
	
	/*
	* Tries to set user's phone number
	* @param login
	* @return true if successful, false otherwise
	**/ 	
	public static boolean TrySetUserPhonenumber(Cafe esql, String login)
	{
		String currentPhoneNumber = GetUserPhoneNumber(esql, login);
		String newPhoneNumber = GetUserInput("Enter new phone number: ");
		
		if (!IsEqual(currentPhoneNumber, newPhoneNumber))
		{
			SetUserPhoneNumber(esql, login, newPhoneNumber);
		}
		
		return IsEqual(GetUserPhoneNumber(esql, login), newPhoneNumber);
	}
	
	/*
	* Sets user's favItems
	* @param login, favItems
	**/
	public static void SetUserFavItems(Cafe esql, String login, String favItems)
	{
		String prevFaveItems = GetUserFavItems(esql, login);
		
		if (!IsEqual(prevFaveItems, favItems))
		{
			try
			{
				String query = String.format("UPDATE Users SET favItems = '%s' WHERE login = '%s'", favItems, login);
				esql.executeUpdate(query);
				  
				return;
			}
			catch (Exception e)
			{
				System.err.println (e.getMessage ());
				return;
			}
		}
	}
	
	/*
	* Set user's password
	* @param login, password
	**/ 	
	public static void SetUserPassword(Cafe esql, String login, String password)
	{
		String prevPassword = GetUserPassword(esql, login);
		
		if (!IsEqual(prevPassword, password))
		{
			try
			{
				String query = String.format("UPDATE Users SET password = '%s' WHERE login = '%s'", password, login);
				esql.executeUpdate(query);
				  
				return;
			}
			catch (Exception e)
			{
				System.err.println (e.getMessage ());
				return;
			}
		}
	}
	
	/*
	* Tries to set user's password. If isCustomer, asks to enter old password first before changing
	* @param login, isCustomer
	* @return true if successful, false otherwise
	**/ 		
	public static boolean TrySetUserPassword(Cafe esql, String login, boolean isCustomer)
	{
		String currentPassword = GetUserPassword(esql, login);
		boolean canEdit = true;
		
		if (isCustomer)
		{
			String oldPassword = GetUserInput("Enter old password: ");
			
			if (!IsEqual(currentPassword, oldPassword))
			{
				System.out.println("Error: Incorrect password.");
				canEdit = false;
			}
			else
			{
				canEdit = true;
			}
		}
		
		if (canEdit)
		{
			String newPassword = GetUserInput("Enter new password: ");
			
			if (!IsEqual(newPassword, currentPassword))
			{
				SetUserPassword(esql, login, newPassword);
			}
			
			return IsEqual(GetUserPassword(esql, login), newPassword);
		}
		return false;
	}
	
	/*
	* Tries to set user's favItems
	* @param login
	* @return true if successful, false otherwise
	**/ 			
	public static boolean TrySetUserFavItems(Cafe esql, String login)
	{
		String currentFavItems = GetUserFavItems(esql, login);
		String newFavItems = GetUserInput("Enter new favorite items: ");
		
		if (!IsEqual(currentFavItems, newFavItems))
		{
			SetUserFavItems(esql, login, newFavItems);
		}
		
		return IsEqual(GetUserFavItems(esql, login), newFavItems);
	}		
	
	/*
	* Gets user's type
	* @param login
	* @return user type
	**/ 			
	public static String GetUserType(Cafe esql, String login)
	{
		try
		{
			String query = String.format("SELECT type FROM Users WHERE login = '%s'", login);
			List<List<String>> results = esql.executeQueryAndReturnResult(query);
		   
			if (results.size() > 0)
			{
				return results.get(0).get(0).trim();
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return null;
		}
	}
	
	/*
	* Sets user type to customer
	* @param login
	**/ 				
	public static void SetUserTypeToCustomer(Cafe esql, String login)
	{
		String prevType = GetUserType(esql, login);
		
		if (!IsEqual(prevType, "Customer"))
		{
			try
			{
				String query = String.format("UPDATE Users SET type = '%s' WHERE login = '%s'", "Customer", login);
				esql.executeUpdate(query);
				  
				return;
			}
			catch (Exception e)
			{
				System.err.println (e.getMessage ());
				return;
			}
		}
	}	
	
	/*
	* Sets user type to manager
	* @param login
	**/ 	
	public static void SetUserTypeToManager(Cafe esql, String login)
	{
		String prevType = GetUserType(esql, login);
		
		if (!IsEqual(prevType, "Manager "))
		{
			try
			{
				String query = String.format("UPDATE Users SET type = '%s' WHERE login = '%s'", "Manager ", login);
				esql.executeUpdate(query);
				  
				return;
			}
			catch (Exception e)
			{
				System.err.println (e.getMessage ());
				return;
			}
		}
	}
	
	/*
	* Sets user type to employee
	* @param login
	**/ 	
	public static void SetUserTypeToEmployee(Cafe esql, String login)
	{
		String prevType = GetUserType(esql, login);
		
		if (!IsEqual(prevType, "Employee"))
		{
			try
			{
				String query = String.format("UPDATE Users SET type = '%s' WHERE login = '%s'", "Employee ", login);
				esql.executeUpdate(query);
				  
				return;
			}
			catch (Exception e)
			{
				System.err.println (e.getMessage ());
				return;
			}
		}
	}	

	/*
	* Tries to set user type
	* 	If type == 0, set to customer
	* 	If type == 1, set to employee
	* 	If type == 2, set to manager
	* @param login, type
	* @return true if successful, false otherwise
	**/ 
	public static boolean TrySetUserType(Cafe esql, String login, int type)
	{
		String prevType = GetUserType(esql, login);
		
		if (type == 0)
		{
			SetUserTypeToCustomer(esql, login);
			
			return IsEqual(GetUserType(esql, login), "Customer");
		}
		else if (type == 1)
		{
			SetUserTypeToEmployee(esql, login);
			
			return IsEqual(GetUserType(esql, login), "Employee");
		}
		else if (type == 2)
		{
			SetUserTypeToManager(esql, login);
			
			return IsEqual(GetUserType(esql, login), "Manager ");
		}
		else
		{
			return false;
		}
	}
	
	/*
	* Determines if login name exists
	* @param login
	* @return true if exists, false otherwise
	**/ 
	public static boolean LoginDoesExist(Cafe esql, String login)
	{
		try
		{
			String query = String.format("SELECT * FROM Users WHERE login = '%s'", login);
			int userNum = esql.executeQuery(query);
			
			return userNum > 0;	
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());	
			return false;
		}
	}
	
	/*
	* Determines if Item statuses exist by itemName
	* @param itemName
	* @return true if exists, false otherwise
	**/ 	
	public static boolean ItemStatusDoesExist(Cafe esql, String itemName)
	{
		try
		{
			String query = String.format("SELECT CAST(CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END AS BIT) FROM ItemStatus WHERE itemName = '%s';", itemName);
			List<List<String>> results = esql.executeQueryAndReturnResult(query);
		   
			String result = results.get(0).get(0);
			
			return IsEqual(result, "1");
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return false;
		}    		
	}
	
	/*
	* Gets all order ids with ItemStatuses using itemName
	* @param itemName
	* @return List<Integer> orderIds
	**/ 	
	public static List<Integer> GetAllOrderIdWithItemName(Cafe esql, String itemName)
	{
		try
		{
			List<Integer> result = new ArrayList<Integer>();
			
			String query = String.format("SELECT orderid FROM ItemStatus WHERE itemName = '%s'", itemName);
			List<List<String>> orderIds = esql.executeQueryAndReturnResult(query);
			
			if (orderIds.size() > 0)
			{
				for(List<String> orderId : orderIds)
				{
					int currOrderId = Integer.parseInt(orderId.get(0));
					result.add(currOrderId);
				}
			}
			return result;
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage ());
			return null;
		}
	}
	
	/*
	* Subtracts amount from order total
	* @param orderId, amount
	**/ 
	public static void SubtractFromOrderTotal(Cafe esql, int orderId, double amount)
	{
		double currTotal = GetOrderTotal(esql, orderId);
		double newTotal = currTotal - amount;
		
		SetOrderTotal(esql, orderId, newTotal);
	}
	
	/*
	* Deletes item status by item name
	* @param itemName
	* @return true if successful, false otherwise
	**/	
	public static boolean DeleteItemStatusByItemName(Cafe esql, String itemName)
	{
		try
		{
			List<Integer> orderIds = GetAllOrderIdWithItemName(esql, itemName);
			double itemPrice = GetItemPrice(esql, itemName);
			
			for(int orderId : orderIds)
			{
				SubtractFromOrderTotal(esql, orderId, itemPrice);
			}
			
			String query = String.format("DELETE FROM ItemStatus WHERE itemName = '%s'", itemName);
			esql.executeUpdate(query);
			   
			return !ItemStatusDoesExist(esql, itemName);
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return false;
		}
	}	
	
	/*
	* Cascade deletes item from menu by itemName
	* 	Removes all itemStatus with itemName
	* 	Then removes item from menu
	* @param itemName
	* @return true if successful, false otherwise
	**/		
	public static void CascadeDeleteItemFromMenu(Cafe esql, String itemName)
	{
		if (DeleteItemStatusByItemName(esql, itemName))
		{
			try
			{
				String query = String.format("DELETE FROM Menu WHERE itemName = '%s';", itemName);
				esql.executeUpdate(query);
			}
			catch (Exception e)
			{
				System.err.println (e.getMessage ());	
				return;
			}
		}
	}
	
	/*
	* Add new item to menu
	* @param itemName, price, description, imageURL
	**/		
	public static void AddItemToMenu(Cafe esql, String itemName, String type, double price, String description, String imageURL)
	{
		try
		{
			String query = String.format("INSERT INTO Menu (itemName, type, price, description, imageURL) VALUES ('%s','%s','%f','%s','%s')", itemName, type, price, description, imageURL);
			esql.executeUpdate(query);
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());	
			return;
		}
	}
	
	/*
	* Gets item type on menu
	* @param itemName
	**/		
	public static String GetMenuItemType(Cafe esql, String itemName)
	{
		try
		{
			String query = String.format("SELECT type FROM Menu WHERE itemName = '%s'", itemName);
			List<List<String>> results = esql.executeQueryAndReturnResult(query);
		   
			if (results.size() > 0)
			{
				return results.get(0).get(0).trim();
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return null;
		}		
	}
	
	/*
	* Sets item type on menu
	* @param itemName, type
	**/		
	public static void SetMenuItemType(Cafe esql, String itemName, String type)
	{
		String prevType = GetMenuItemType(esql, itemName);
		
		if (!IsEqual(prevType, type))
		{
			try
			{
				String query = String.format("UPDATE Menu SET type = '%s' WHERE itemName = '%s'", type, itemName);
				esql.executeUpdate(query);
				  
				return;
			}
			catch (Exception e)
			{
				System.err.println (e.getMessage ());
				return;
			}
		}
	}
	
	public static void SetMenuItemPrice(Cafe esql, String itemName, double price)
	{
		Double prevPrice = GetItemPrice(esql, itemName);
		
		if (prevPrice != price)
		{
			try
			{
				String query = String.format("UPDATE Menu SET price = '%f' WHERE itemName = '%s'", price, itemName);
				esql.executeUpdate(query);
				  
				return;
			}
			catch (Exception e)
			{
				System.err.println (e.getMessage ());
				return;
			}
		}
	}
	
	public static String GetMenuItemDescription(Cafe esql, String itemName)
	{
		try
		{
			String query = String.format("SELECT description FROM Menu WHERE itemName = '%s'", itemName);
			List<List<String>> results = esql.executeQueryAndReturnResult(query);
		   
			if (results.size() > 0)
			{
				return results.get(0).get(0).trim();
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return null;
		}		
	}
		
	public static void SetMenuItemDescription(Cafe esql, String itemName, String description)
	{
		String prevDescription = GetMenuItemDescription(esql, itemName);
		
		if (!IsEqual(prevDescription, description))
		{
			try
			{
				String query = String.format("UPDATE Menu SET description = '%s' WHERE itemName = '%s'", description, itemName);
				esql.executeUpdate(query);
				  
				return;
			}
			catch (Exception e)
			{
				System.err.println (e.getMessage ());
				return;
			}
		}		
	}
	
	public static String GetMenuItemImageURL(Cafe esql, String itemName)
	{
		try
		{
			String query = String.format("SELECT imageURL FROM Menu WHERE itemName = '%s'", itemName);
			List<List<String>> results = esql.executeQueryAndReturnResult(query);
		   
			if (results.size() > 0)
			{
				return results.get(0).get(0).trim();
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return null;
		}		
	}
	
	public static void SetMenuItemImageUrl(Cafe esql, String itemName, String imageUrl)
	{
		String prevImageUrl = GetMenuItemImageURL(esql, itemName);
		
		if (!IsEqual(prevImageUrl, imageUrl))
		{
			try
			{
				String query = String.format("UPDATE Menu SET imageURL = '%s' WHERE itemName = '%s'", imageUrl, itemName);
				esql.executeUpdate(query);
				  
				return;
			}
			catch (Exception e)
			{
				System.err.println (e.getMessage ());
				return;
			}
		}		
	}
	
	public static boolean TryRemoveItemFromMenu(Cafe esql)
	{
		String itemName = GetUserInput("Enter name of item to remove: ");
	
		if (ItemNameDoesExist(esql, itemName))
		{
			CascadeDeleteItemFromMenu(esql, itemName);
			
			return !ItemNameDoesExist(esql, itemName);
		}
		else
		{
			System.out.println("Error: Item name not found.");
			return false;
		}		
	}
	
	public static boolean TryAddItemToMenu(Cafe esql)
	{
		String itemName = GetUserInput("Enter new item name: ");
	
		if (!ItemNameDoesExist(esql, itemName))
		{
			String type = GetUserInput("Enter item type: ");
			String price = GetUserInput("Enter item price: ");
			
			double realPrice;
			try
			{
				realPrice = Double.parseDouble(price);
				String description = GetUserInput("Enter item description: ");
				String imageUrl = GetUserInput("Enter image url: ");
				
				AddItemToMenu(esql, itemName, type, realPrice, description, imageUrl);
				
				return ItemNameDoesExist(esql, itemName);				
			}
			catch (NumberFormatException e)
			{
				System.out.println("Error: Invalid price entered. Must be in format #.##");
				return false;
			}
		}
		else
		{
			System.out.println("Error: Item name already exists.");
			return false;
		}
	}
	
	public static boolean TrySetMenuItemType(Cafe esql, String itemName)
	{
		String currItemType = GetMenuItemType(esql, itemName);
		
		System.out.println("Current item type: " + currItemType);
		
		String newType = GetUserInput("Enter new type ('q' to return): ");
		
		if (IsEqual(newType, "q"))
		{
			return false;
		}
		else
		{		
			if (!IsEqual(currItemType, newType))
			{
				SetMenuItemType(esql, itemName, newType);
			}
		}
		
		return IsEqual(GetMenuItemType(esql, itemName), newType);
	}
	
	public static boolean TrySetMenuItemPrice(Cafe esql, String itemName)
	{
		double curr = GetItemPrice(esql, itemName);
		double realPrice = 0;
		System.out.println("Current item price: $" + curr);
		
		String newPrice = GetUserInput("Enter new price ('q' to return): ");
		
		if (IsEqual(newPrice, "q"))
		{
			return false;
		}
		else 
		{
			try
			{
				realPrice = Double.parseDouble(newPrice);
				
				if (curr != realPrice)
				{
					SetMenuItemPrice(esql, itemName, realPrice);
				}
			}
			catch (NumberFormatException e)
			{
				System.out.println("Error: Invalid price entered. Must be in format #.##");
				return false;
			}
		}
		
		return GetItemPrice(esql, itemName) == realPrice;
	}
	
	public static boolean TrySetMenuItemDescription(Cafe esql, String itemName)
	{
		String curr = GetMenuItemDescription(esql, itemName);
		
		System.out.println("Current item description: " + curr);
		
		String newDescription = GetUserInput("Enter new description ('q' to return): ");
		
		if (IsEqual(newDescription, "q"))
		{
			return false;
		}
		else
		{				
			if (!IsEqual(curr, newDescription))
			{
				SetMenuItemDescription(esql, itemName, newDescription);
			}
		}
		
		return IsEqual(GetMenuItemDescription(esql, itemName), newDescription);
	}
	
	public static boolean TrySetMenuItemImageURL(Cafe esql, String itemName)
	{
		String curr = GetMenuItemImageURL(esql, itemName);
		
		System.out.println("Current item image url: " + curr);
		
		String newURL = GetUserInput("Enter new image url ('q' to return): ");
		
		if (IsEqual(newURL, "q"))
		{
			return false;
		}
		else
		{				
			if (!IsEqual(curr, newURL))
			{
				SetMenuItemImageUrl(esql, itemName, newURL);
			}
		}		

		return IsEqual(GetMenuItemImageURL(esql, itemName), newURL);
	}	
	
	public static void UpdateMenuItemInformation(Cafe esql)
	{
		String itemName = GetUserInput("Enter item name: ");
		boolean isUpdating = true;
		
		if (ItemNameDoesExist(esql, itemName))
		{
			while (isUpdating)
			{
				System.out.println("\nWhat would you like to do?");
				System.out.println("..........................");
				System.out.println("1. Update item type");
				System.out.println("2. Update item price");
				System.out.println("3. Update item description");
				System.out.println("4. Update item imageURL");
				System.out.println("5. Print item menu information");
				System.out.println("...");
				System.out.println("9. Return to previous menu.");
				
				switch(readChoice())
				{
					case 1:
					if (TrySetMenuItemType(esql, itemName))
					{
						System.out.println("Success: Item type has been updated.");
					}
					else
					{
						System.out.println("Error: Item type was not updated.");
					}
					break;
					
					case 2:
					if (TrySetMenuItemPrice(esql, itemName))
					{
						System.out.println("Success: Item price has been updated.");
					}
					else
					{
						System.out.println("Error: Item price was not updated.");
					}
					break;
					
					case 3:
					if (TrySetMenuItemDescription(esql, itemName))
					{
						System.out.println("Success: Item description has been updated.");
					}
					else
					{
						System.out.println("Error: Item description was not updated.");
					}
					break;		
					
					case 4:
					if (TrySetMenuItemImageURL(esql, itemName))
					{
						System.out.println("Success: Item image url has been updated.");
					}
					else
					{
						System.out.println("Error: Item image url was not updated.");
					}
					break;												
					
					case 5:
					PrintItemMenuInformation(esql, itemName);
					break;
					
					case 9:
					isUpdating = false;
					break;
						
					default: 
					System.out.println("Unrecognized choice!"); 
					break;
				}			
			}
			return;
		}
		else
		{
			System.out.println("Error: " + itemName + " not found.");
			return;
		}
	}
	 
	/*
	 * END NEW CUSTOM FUNCTIONS
	 **/

	public static void Greeting(){
		System.out.println(
		"\n\n*******************************************************\n" +
		"              User Interface                         \n" +
		"*******************************************************\n");
	}//end Greeting

	/*
	* Reads the users choice given from the keyboard
	* @int
	**/
	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}
			catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		} while (true);
		return input;
	}//end readChoice

	/*
	* Creates a new user with privided login, passowrd and phoneNum
	**/
	public static void CreateUser(Cafe esql){
		try{
			System.out.print("\tEnter user login: ");
			String login = in.readLine();
			System.out.print("\tEnter user password: ");
			String password = in.readLine();
			System.out.print("\tEnter user phone: ");
			String phone = in.readLine();
			 
			String type="Customer";
			String favItems="";
			String query = String.format("INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", login, phone, password, favItems, type);

			esql.executeUpdate(query);
			System.out.println ("User successfully created!");
		}
		catch(Exception e){
			System.err.println (e.getMessage ());
		}
	}//end
   
	/*
	* Check log in credentials for an existing user
	* @return User login or null is the user does not exist
	**/
	public static String LogIn(Cafe esql)
	{
		try
		{
			System.out.print("\tEnter user login: ");
			String login = in.readLine();
			System.out.print("\tEnter user password: ");
			String password = in.readLine();

			String query = String.format("SELECT * FROM Users WHERE login = '%s' AND password = '%s'", login, password);
			int userNum = esql.executeQuery(query);
			
			if (userNum > 0)
			{
				System.out.print("\n\nSuccessfully logged in\n\n");
				return login;
			}
			else
			{
				System.out.print("\n\nCannot login. Try again.\n\n");
				return null;
			}
		}
		catch(Exception e)
		{
			System.err.println (e.getMessage ());
			return null;
		}
	}//end

	/*
	* Find the user type for an existing user
	* @return User type or null if user contains illegal type or does not exist.
	**/
	public static String find_type(Cafe esql)
	{
		// Your code goes here.
		// ...
		// X    
		try
		{
			if (authorizedUser != null)
			{
				String query = String.format("SELECT type FROM Users WHERE login = '%s'", authorizedUser);
				List<List<String>> types = esql.executeQueryAndReturnResult(query);
			   
				if (types.size() > 0)
				{
					String userType = types.get(0).get(0);
					if (IsEqual(userType, "Employee") || IsEqual(userType, "Manager") || IsEqual(userType, "Customer"))
					{
						if(userType.equals("Manager ")) { //delete later
							return "Manager ";
					}	
					return userType;
					}
					else
					{
						System.out.println("Error: Illegal user type stored in database.\n");
						return null;
					}
				}
				else
				{
					System.out.printf("\nError: '%s' not found in database.\n", authorizedUser);
					return null;
				}
			}
			else
			{
				System.out.println("Error: User not authorized.\n");
				return null;
			}
		}
		catch (Exception e)
		{
				System.err.println (e.getMessage ());
				return null;
		}
	}

	/*
	* Outputs to screen, menu item information by itemName if itemName does exist
	* @return null
	**/
	public static void BrowseMenuName(Cafe esql)
	{
		// Your code goes here.
		// ...
		// X
		try
		{
			String itemName = GetUserInput("Enter item name: ");
			PrintItemMenuInformation(esql, itemName);
			return;
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return;
		}
	}

	/*
	* Prints to screen all menu item information corresponding to type if type does exist
	* @return null
	**/
	public static void BrowseMenuType(Cafe esql)
	{
		// Your code goes here.
		// ...
		// X
		try
		{
			String itemType = GetUserInput("Enter item type: ");
			String query = String.format("SELECT * FROM Menu WHERE type = '%s'", itemType);
			List<List<String>> items = esql.executeQueryAndReturnResult(query);
		  
			if (items.size() > 0)
			{
				for(List<String> item : items)
				{
					String itemName = item.get(0);
					PrintItemMenuInformation(esql, itemName);
				}
			}
			else
			{
				System.out.printf("\nError: '%s' type not found.\n", itemType);
			}
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return;
		}
	}

	/*
	* Creates new order and adds items to order
	* Outputs order summary afterwards
	* @return orderId if order created, -1 otherwise
	**/
	public static Integer AddOrder(Cafe esql)
	{
		// Your code goes here.
		// ...
		// X    
		try
		{
			if (authorizedUser != null)
			{
				System.out.println("\n-----------------");
				System.out.println("Creating Order...");
				 
				int orderId = CreateNewOrder(esql);
				if (orderId >= 0)
				{
					System.out.println("#" + orderId);
					System.out.println("-----------------");
					boolean isOrdering = true;
					
					while(isOrdering)
					{
						String itemName = GetUserInput("Enter item name ('q' to complete order): ");
						 
						if (itemName.equals("q"))
						{
							isOrdering = false;
						}
						else
						{
							String userMessage = "Additional comments for " + itemName + "? ";
							String comment = GetUserInput(userMessage);
							
							AddItemStatusToOrder(esql, orderId, itemName, comment);
						}
					}
					
					if (GetOrderTotal(esql, orderId) > 0.0)
					{
						PrintOrderSummary(esql, orderId);
						return orderId;
					}
					else
					{
						CascadeDeleteOrder(esql, orderId);
						return -1;
					}
				}
				else
				{
					return -1;
				}
			}
			else
			{
				System.out.println("Error: User not authorized.\n");
				return -1;
			}
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage ());
			return -1;
		}
	}//end 

	/*
	* Allows user to add/remove items, set item status comments, or cancel their order
	* Outputs new order summary when finished if order is not cancelled
	* @return null
	**/
	public static void UpdateOrder(Cafe esql)
	{
		if (authorizedUser != null)
		{
			int orderId = Integer.parseInt(GetUserInput("Enter order identification #: "));
			String resultMessage = CustomerCanEditOrder(esql, orderId);
		   
			if (resultMessage != null)
			{
				System.out.println(resultMessage);
				return;
			}
			else
			{
				boolean isUpdating = true;
				boolean didCancelOrder = false;
					 
				while (isUpdating)
				{
					System.out.println("\nWhat would you like to do?");
					System.out.println("..........................");
					System.out.println("1. Add an item to order");
					System.out.println("2. Remove an item from order");
					System.out.println("3. Change an item comment");
					System.out.println("...");
					System.out.println("7. View current order");
					System.out.println("8. Cancel order");
					System.out.println("9. Finished updating");
						 
					switch(readChoice())
					{
						case 1:
						String itemNameToAdd = GetUserInput("Enter name of item to add: ");
						String userMessage = "Additional comments for " + itemNameToAdd + "? ";
						String comment = GetUserInput(userMessage);
						
						AddItemStatusToOrder(esql, orderId, itemNameToAdd, comment);
						break;
						 
						case 2:
						String itemNameToRemove = GetUserInput("Enter name of item to remove: ");
						RemoveItemStatusFromOrder(esql, orderId, itemNameToRemove);
						break;
							
						case 3:
						String itemNameToComment = GetUserInput("Enter name of item to comment on: ");
						TrySetItemStatusComment(esql, orderId, itemNameToComment);
						break;
						
						case 7:
						PrintOrderSummary(esql, orderId);
						break;
						
						case 8:
						if (CascadeDeleteOrder(esql, orderId))
						{
							System.out.println("Success: Your order has been cancelled.\n");
							didCancelOrder = true;
							isUpdating = false;
						}
						else
						{
							System.out.println("Error: Your order could not be cancelled at this time.\n");
						}
						break;
						
						case 9:
						isUpdating = false;
						break;
						 
						default: 
						System.out.println("Unrecognized choice!"); 
						break;
					}
				}
				
				if (!didCancelOrder)
				{
					if (GetOrderTotal(esql, orderId) <= 0.0)
					{
						CascadeDeleteOrder(esql, orderId);
					
						System.out.println("You have no items on your order, your order has been cancelled.");
					}
					else
					{
						PrintOrderSummary(esql, orderId);
					}
				}
				return;
			}
		}
		else
		{
			System.out.println("Error: User not authorized.\n");
			return;
		}
	}

	/*
	* Allows employee to add/remove items, set item status comments, set order paid status, or cancel an order
	* Outputs new order summary when finished if order is not cancelled
	* @return null
	**/
	public static void EmployeeUpdateOrder(Cafe esql)
	{
		// Your code goes here.
		// ...
		// X
		if (authorizedUser != null)
		{
			int orderId = Integer.parseInt(GetUserInput("Enter order identification #: "));

			boolean isUpdating = true;
			boolean didCancelOrder = false;
					 
			while (isUpdating)
			{
				System.out.println("\nWhat would you like to do?");
				System.out.println("..........................");
				System.out.println("1. Add an item to order");
				System.out.println("2. Remove an item from order");
				System.out.println("3. Change an item comment");
				System.out.println("4. Set paid status to \"Paid\"");
				System.out.println("5. Set paid status to \"Unpaid\"");
				System.out.println("...");
				System.out.println("7. View current order");
				System.out.println("8. Cancel order");
				System.out.println("9. Finished updating");
						 
				switch(readChoice())
				{
					case 1:
					String itemNameToAdd = GetUserInput("Enter name of item to add: ");
					String userMessage = "Additional comments for " + itemNameToAdd + "? ";
					String comment = GetUserInput(userMessage);
					
					AddItemStatusToOrder(esql, orderId, itemNameToAdd, comment);
					break;
					 
					case 2:
					String itemNameToRemove = GetUserInput("Enter name of item to remove: ");
					RemoveItemStatusFromOrder(esql, orderId, itemNameToRemove);
					break;
						
					case 3:
					String itemNameToComment = GetUserInput("Enter name of item to comment on: ");
					TrySetItemStatusComment(esql, orderId, itemNameToComment);
					break;
					
					case 4:
					SetOrderPaid(esql, orderId, true);
					
					if (DidPayOrder(esql, orderId))
					{
						System.out.println("Success: Order #" + orderId + " paid status has been set to \"Paid\".");
					}
					else
					{
						System.out.println("Error: Could not set order #" + orderId + " paid status to \"Unpaid\"");
					}
					break;
					
					case 5:
					SetOrderPaid(esql, orderId, false);
					if (!DidPayOrder(esql, orderId))
					{
						System.out.println("Success: Order #" + orderId + " paid status has been set to unpaid.");
					}
					else
					{
						System.out.println("Error: Could not set order #" + orderId + " paid status to unpaid");
					}
					break;						
					
					case 7:
					PrintOrderSummary(esql, orderId);
					break;
					
					case 8:
					if (CascadeDeleteOrder(esql, orderId))
					{
						System.out.println("Success: Your order has been cancelled.\n");
						didCancelOrder = true;
						isUpdating = false;
					}
					else
					{
						System.out.println("Error: Your order could not be cancelled at this time.\n");
					}
					break;
					
					case 9:
					isUpdating = false;
					break;
					 
					default: 
					System.out.println("Unrecognized choice!"); 
					break;
				}
			}
				
			if (!didCancelOrder)
			{
				if (GetOrderTotal(esql, orderId) <= 0.0)
				{
					CascadeDeleteOrder(esql, orderId);
					
					System.out.println("There are no items on the order, the order has been cancelled.");
				}
				else
				{
					PrintOrderSummary(esql, orderId);
				}
			}
			return;
		}
		else
		{
			System.out.println("Error: User not authorized.\n");
			return;
		}		
	}//end

	/*
	* Prints user's most recent unpaid orders
	* @return null
	**/
	public static void ViewOrderHistory(Cafe esql)
	{
		// Your code goes here.
		// ...
		// X
		PrintRecentOrderHistory(esql, false);
		return;
	}//end	

	public static void UpdateUserInfo(Cafe esql)
	{
		// Your code goes here.
		// ...
		// X
		if (authorizedUser != null)
		{
			boolean isUpdating = true;
				
			while(isUpdating)
			{
				System.out.println("\nWhat would you like to do?");
				System.out.println("..........................");
				System.out.println("1. Update password");
				System.out.println("2. Update phone number");
				System.out.println("3. Update favorite items");
				System.out.println("4. View current profile");
				System.out.println("...");
				System.out.println("9. Return to main menu");
								 
				switch(readChoice())
				{
					case 1:
					if (TrySetUserPassword(esql, authorizedUser, true))
					{
						System.out.println("Success: The password has been updated.");
					}
					else
					{
						System.out.println("Error: The password was not updated.");
					}
					break;
					
					case 2:
					if (TrySetUserPhonenumber(esql, authorizedUser))
					{
						System.out.println("Success: The phone number has been updated.");
					}
					else
					{
						System.out.println("Error: The phone number was not updated.");
					}
					break;
								 
					case 3:
					if (TrySetUserFavItems(esql, authorizedUser))
					{
						System.out.println("Success: The favorite items have been updated.");
					}
					else
					{
						System.out.println("Error: The favorite items were not updated.");
					}				
					break;
					
					case 4:
					PrintUserInformation(esql, authorizedUser);
					break;
					
					case 9:
					isUpdating = false;
					break;
					
					default: 
					System.out.println("Unrecognized choice!"); 
					break;
				}
			}
		}
		else
		{
			System.out.println("Error: You are not authorized.");
		}
		return;
	}//end

	public static void ManagerUpdateUserInfo(Cafe esql)
	{
		// Your code goes here.
		// ...
		// ...
		if (authorizedUser != null)
		{
			String login = GetUserInput("Enter login to update: ");
			
			if (LoginDoesExist(esql, login))
			{
				boolean isUpdating = true;
				
				while(isUpdating)
				{
					System.out.println("\nWhat would you like to do?");
					System.out.println("..........................");
					System.out.println("1. Update password");
					System.out.println("2. Update phone number");
					System.out.println("3. Update favorite items");
					System.out.println("4. Set user type to customer");
					System.out.println("5. Set user type to employee");
					System.out.println("6. Set user type to manager");
					System.out.println("7. View current profile");
					System.out.println("...");
					System.out.println("9. Return to main menu");
									 
					switch(readChoice())
					{
						case 1:
						if (TrySetUserPassword(esql, login, false))
						{
							System.out.println("Success: Your password has been updated.");
						}
						else
						{
							System.out.println("Error: Your password was not updated.");
						}
						break;
						
						case 2:
						if (TrySetUserPhonenumber(esql, login))
						{
							System.out.println("Success: Your phone number has been updated.");
						}
						else
						{
							System.out.println("Error: Your phone number was not updated.");
						}
						break;
									 
						case 3:
						if (TrySetUserFavItems(esql, login))
						{
							System.out.println("Success: Your favorite items have been updated.");
						}
						else
						{
							System.out.println("Error: Your favorite items were not updated.");
						}				
						break;
						
						case 4:
						if (TrySetUserType(esql, login, 0))
						{
							System.out.println("Success: User type updated to \"Customer\"");
						}
						else
						{
							System.out.println("Error: User type was not updated.");
						}
						break;
						
						case 5:
						if (TrySetUserType(esql, login, 1))
						{
							System.out.println("Success: User type updated to \"Employee\"");
						}
						else
						{
							System.out.println("Error: User type was not updated.");
						}
						break;		
						
						case 6:
						if (TrySetUserType(esql, login, 2))
						{
							System.out.println("Success: User type updated to \"Manager\"");
						}
						else
						{
							System.out.println("Error: User type was not updated.");
						}
						break;	
						
						case 7:
						PrintUserInformation(esql, login);
						break;
						
						case 9:
						isUpdating = false;
						break;
						
						default: 
						System.out.println("Unrecognized choice!"); 
						break;
					}
				}
			}
			else
			{
				System.out.println("Error: " + login + " not found.");
			}				
		}
		else
		{
			System.out.println("Error: You are not authorized.");
		}
		return;		
	}//end

	public static void UpdateMenu(Cafe esql)
	{
		if (IsManager())
		{
			boolean isUpdating = true;
				
			while(isUpdating)
			{
				System.out.println("\nWhat would you like to do?");
				System.out.println("..........................");
				System.out.println("1. Add item to menu");
				System.out.println("2. Remove item from menu");
				System.out.println("3. Update item in menu");
				System.out.println("...");
				System.out.println("9. Return to main menu");
				
				switch(readChoice())
				{
					case 1:
					if (TryAddItemToMenu(esql))
					{
						System.out.println("Success: Item has been added.");
					}
					else
					{
						System.out.println("Error: Item was not added.");
					}
					break;
					
					case 2:
					if (TryRemoveItemFromMenu(esql))
					{
						System.out.println("Success: Item has been removed.");
					}
					else
					{
						System.out.println("Error: Item was not removed.");
					}
					break;
					
					case 3:
					UpdateMenuItemInformation(esql);
					break;
					
					case 9:
					isUpdating = false;
					break;
						
					default: 
					System.out.println("Unrecognized choice!"); 
					break;
				}
			}			
		}
		else
		{
			System.out.println("Error: Must be manager level.");
		}
		return;
	}//end

	/*
	* Prints all orders from the past 24 hours
	* @return
	**/
	public static void ViewOrderStatus(Cafe esql){
		// Your code goes here.
		// ...
		// X
		int orderId = Integer.parseInt(GetUserInput("Enter order identification #: "));
		
		if (OrderBelongsToCustomer(esql, orderId) || IsEmployee() || IsManager())
		{
			PrintOrderSummary(esql, orderId);
		}
		else
		{
			System.out.printf("\nError: Cannot view order #%d.\n", orderId);
		}
	}//end

	/*
	* Prints all orders from the past 24 hours that are unpaid
	* @return
	**/
	public static void ViewCurrentOrder(Cafe esql)
	{
		// Your code goes here.
		// ...
		// X
		System.out.println("\nWhat would you like to do?");
		System.out.println("..........................");
		System.out.println("1. View unpaid orders from last 24 hours");
		System.out.println("2. View all orders from last 24 hours");
		System.out.println("...");
		System.out.println("9. Return to main menu");
						 
		switch(readChoice())
		{
			case 1:
			PrintAllOrderHistoryFromPast24Hours(esql, true);
			break;
			
			case 2:
			PrintAllOrderHistoryFromPast24Hours(esql, false);
			break;
						 
			case 9:
			return;
			
			default: 
			System.out.println("Unrecognized choice!"); 
			break;
		}	
		return;
	}//end

	public static void Query6(Cafe esql){
		// Your code goes here.
		// ...
		// ...
	}//end Query6

}//end Cafe
