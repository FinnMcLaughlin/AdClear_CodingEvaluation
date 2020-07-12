import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
		
public class HTTPServer {

	/* Main that creates localhost HttpServer using port 8085,
	 sets the handler function to handle requests sent to the server,
	 and starts server */
	public static void main(String[] args) throws IOException {
		
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8085), 0);
			HttpContext context = server.createContext("/");
		    context.setHandler(HTTPServer::handleRequest);
		    server.start();
		    
		    Timer timer = new Timer();
		    TimerTask updateHourlyStats = new TimerTask() {
		    	public void run() {
		    		updateHourlyStatsTable();
		    	}
		    };
		    
		    timer.schedule (updateHourlyStats, 1000*60*60l, 1000*60*60);
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
		
	/* Function to handle the requests sent to the server */
	private static void handleRequest(HttpExchange exchange) throws IOException {		
		
		String method = exchange.getRequestMethod();
		InputStreamReader inputStream = new InputStreamReader(exchange.getRequestBody(),"utf-8");
				
		if (method.equalsIgnoreCase("POST")) {
			BufferedReader bReader = new BufferedReader(inputStream);
			int b;
			StringBuilder buf = new StringBuilder(512);
			
			while ((b = bReader.read()) != -1) {
			    buf.append((char) b);
			}

			bReader.close();
			inputStream.close();
			
			String requestBodyString = buf.toString();
			
			exchange.sendResponseHeaders(200, requestBodyString.getBytes().length);
		    OutputStream os = exchange.getResponseBody();
		    os.write(requestBodyString.getBytes());
		    os.close();
			
			System.out.println(buf.toString());
			
			Map<String, String> params = new HashMap<String, String>();
			params = formatRequestBody(requestBodyString);
			System.out.println("CustomerID: " + params.get("customerID"));
			System.out.println("TagID: " + params.get("tagID"));
			System.out.println("UserID: " + params.get("userID"));
			System.out.println("RemoteIP: " + params.get("remoteIP"));
			System.out.println("Timestamp: " + params.get("timestamp"));
			
			boolean validRequest = validateRequestBody(params);
			System.out.println(validRequest);
			logRequest(params, validRequest);
			System.out.println("-----------------------------------------");
		}
		else {
			System.out.println("Not POST Request");
		}
	 }

	
	/* Function to format the request body string into a parameter/value Hashmap */
	public static Map<String, String> formatRequestBody(String reqBody){
		// Formats the given request body string to be further utilised by
		// getting the parameter count, based on the number of & characters in
		// the request body string, ensures that any missing data is explicitly
		// stated, and then extracts the parameters and their values and puts them
		// into a hashmap		
		
		Map<String, String> info = new HashMap<String, String>();
		
		int paramCount = reqBody.length() - reqBody.replace("&", "").length() + 1;
//		System.out.println("Paramter Count: " + paramCount);
		
		reqBody = reqBody.replace("=&", "=null&");
		
		if(reqBody.charAt(reqBody.length()-1) == '=') {
			reqBody = reqBody.concat("null");
		}
		
		for(int index=0; index < paramCount; index++) {					
			String key = reqBody.split("&")[index].split("=")[0];
			String value = reqBody.split("&")[index].split("=")[1];
			info.put(key, value);
			
//			System.out.println("\nKey: " + key + "  |  Value: " + value);
		}
		
		return info;
	}
	
	/* Function to validate the request body data */
	public static boolean validateRequestBody(Map<String, String> params) {
		// Checks whether the data passed through the request body is valid,
		// based on whether it is or isn't malformed, whether the customer 
		// exists in the database, whether the customer holds an active
		// account, and whether or not the IP address and the User Agent
		// is blacklisted		
		
		boolean validRequest = true;
				
		//Malformed JSON
		for(Map.Entry<String, String> parameter : params.entrySet()) {			
			if(parameter.getValue().compareTo("null") == 0) {
				validRequest = false;
				break;
			}
		}
		
		//Not in Customer Table || Disabled Customer
		if(validRequest) {
			String customerCheck = getQueryResult("Customer", "id=" + params.get("customerID"), "active");
			if(customerCheck.compareTo("") == 0 || customerCheck.compareTo("0") == 0) {
				validRequest = false;
			}
		}
		
		//Blacklisted IP
		if(validRequest) {
			String ipCheck = getQueryResult("ip_blacklist", "ip=" + params.get("remoteIP").replace(".", ""), "ip");
			if(ipCheck.compareTo("") != 0) {
				validRequest = false;
			}
		}
		
		//Blacklist User Agent
		if(validRequest) {
			String userAgentCheck = getQueryResult("ua_blacklist", "ua= \'" + params.get("userID") + "\'", "ua");
			if(userAgentCheck.compareTo("") != 0) {
				validRequest = false;
			}
		}
		
		
		return validRequest;
	}
	
	/* Function to connect to the PSQL database and execute queries */
	public static String getQueryResult(String table, String cond, String column) {
		// Executes a SELECT query to the postgresql database based on the table,
		// condition(s) passed into the function, and returns the data returned
		// from the specified column also passed into the function
		
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "password"))
		{
			
			String reqQuery = "SELECT * FROM " + table + " WHERE " + cond + ";";
			
			Statement stmt = connection.createStatement();
	        ResultSet rs = stmt.executeQuery(reqQuery);
	        String response = "";
	
	        while ( rs.next() )
	        {
	        	response = response + rs.getString(column);
	        }	
	        
	        //System.out.println("->" + response);
	        
	        return response;
	
		}
		catch (Exception e) {
	        System.out.println("Connection failure.");
	        e.printStackTrace();
	        
	        return "Connection Failure";
	    }
	}

	/* Function log each request made to the server, based on the customerID and whether the
	   request made was a valid request or not */
	public static void logRequest(Map<String, String> params, boolean validRequest) {
		// Check to see if the customerID exists in the requestLog table
		// If customerID does not exist in the table, then the logQuery is written as an INSERT, and the data is initialised
		// If customerID does exist in the table, then the logQuery is written as an UPDATE, incrementing the specified column data
		// The validRequest boolean which is passed to the function is used to know which column's data is to be incremented when 
		// logging the request
		
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "password"))
		{
			
			String logQuery = "";
			String customerCheck = getQueryResult("requestLog", "customerId=" + params.get("customerID"), "customerId");			
			
			if(customerCheck.compareTo("") == 0) {				
				String custID = params.get("customerID");
								
				int valid_request = 0;
				int invalid_request = 0;
				
				if(validRequest) {
					valid_request = valid_request + 1;
				}
				else {
					invalid_request = invalid_request + 1;
				}
				
				logQuery = "INSERT INTO requestLog VALUES (" + custID + "," + valid_request + "," + invalid_request + ");";
			
			}
			else {				
				String custID = params.get("customerID");
				String update_column = "";
				int update_count = -1;
				
				if(validRequest) {
					update_column = "request_count";
					update_count = Integer.parseInt(getQueryResult("requestLog", "customerId=" + params.get("customerID"), "request_count"));
					System.out.println(update_count);
				}
				else {
					update_column = "invalid_count";
					update_count = Integer.parseInt(getQueryResult("requestLog", "customerId=" + params.get("customerID"), "invalid_count"));
					System.out.println(update_count);
				}
				
				if(update_column.compareTo("") != 0 && update_count >= 0) {
					logQuery = "UPDATE requestLog SET " + update_column + "=" + (update_count + 1) + " WHERE customerID = " + custID + ";";
				}
				
			}
			
			if(logQuery.compareTo("") != 0) {
				PreparedStatement stmt = connection.prepareStatement(logQuery);
		        int rs = stmt.executeUpdate();
		        if(rs > 0) {
		        	System.out.println("Request Log Successful");
		        }
		        else {
		        	System.out.println("Request Log UnSuccessful");
		        }
			}
		}
		catch (Exception e) 
		{
	        System.out.println(e);
	        e.printStackTrace();
	    }
	}
	
	/* Function to update hourly_stats table every hour */
	public static void updateHourlyStatsTable() {
		// Extracts the data from the requestLog table, iterates through it and adds the
		// data to the hourly_stats table with the current timestamp before removing the
		// data from the requetsLog table
		
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		System.out.println(timeStamp);
		
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "password"))
		{
				String reqQuery = "SELECT * FROM requestLog";
				
				Statement selectStmt = connection.createStatement();
		        ResultSet selectResult = selectStmt.executeQuery(reqQuery);
		        
		        while ( selectResult.next() )
		        {
		        	String insertQuery = "INSERT INTO hourly_stats VALUES( nextval('hourly_stats_seq'), " + selectResult.getString("customerID") + ","
		        			+ "cast(\'" + timeStamp + "\' AS TIMESTAMP), " + selectResult.getString("request_count") + "," + selectResult.getString("invalid_count") + ");";
		        
		        	PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
			        int insertResult = insertStmt.executeUpdate();
			        if(insertResult > 0) {
			        	System.out.println("Inserted Customer " + selectResult.getString("customerID") + " successfully into hourly_stats");
			        }
			        
			        String deleteQuery = "DELETE FROM requestLog WHERE customerID = " + selectResult.getString("customerID") +";";
			        PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery);
			        int deleteResult = deleteStmt.executeUpdate();
			        if(deleteResult > 0) {
			        	System.out.println("Customer " + selectResult.getString("customerID") + " successfully removed from requestLog\n");
			        }
		        }   
		}
		catch(Exception e) {
			System.out.println(e);
		}
	}
}
