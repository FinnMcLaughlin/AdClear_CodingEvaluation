package com.finnmclaughlin.codingevaluation;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import com.finnmclaughlin.codingevaluation.ServerAPI;
		
public class HTTPServer {

	/*-
	 * Initialises server and it's various handlers upon initial run, before starting
	 * the server. Creates timer task to be executed every hour, which retrieves the count
	 * of both valid and invalid requests made by customers within that hour and stores the
	 * data in the hourly_stats table 
	 */
	public static void main(String[] args) throws IOException {
		
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8085), 0);
			HttpContext jsonRequests = server.createContext("/");
			jsonRequests.setHandler(arg0 -> {
				try {
					handleRequest(arg0);
				} catch (JSONException e) {
					//e.printStackTrace();
				}
			});
		    
		    HttpContext statsContext = server.createContext("/stats");
		    statsContext.setHandler(HTTPServer::hourlyStatsRequestHandler);
		    HttpContext stats_getNames = server.createContext("/stats/names");
		    stats_getNames.setHandler(HTTPServer::hourlyStatsNamesRequestHandler);
		    server.start();
		    
		    Timer timer = new Timer();
		    TimerTask updateHourlyStats = new TimerTask() {
		    	public void run() {
		    		ServerAPI.updateHourlyStatsTable();
		    	}
		    };
		    
		    timer.schedule (updateHourlyStats, 1000*60*60l, 1000*60*60);
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	
	
	/*-
	 * Request Handler to validate requests made to the server, temporarily storing
	 * information on each request in the logRequest table to be assessed every hour
	 */
	private static void handleRequest(HttpExchange exchange) throws IOException, JSONException {				
		if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
			InputStreamReader inputStream = new InputStreamReader(exchange.getRequestBody(),"utf-8");
			BufferedReader bReader = new BufferedReader(inputStream);
			StringBuilder buf = new StringBuilder(512);
			int b;
			
			while ((b = bReader.read()) != -1) {
			    buf.append((char) b);
			}

			bReader.close();
			inputStream.close();
			
			String requestBodyString = buf.toString();
			
			System.out.println(buf.toString());
			
			Map<String, String> params = new HashMap<String, String>();
			params = ServerAPI.formatJSON(requestBodyString);

			System.out.println("CustomerID: " + params.get("customerID"));
			System.out.println("TagID: " + params.get("tagID"));
			System.out.println("UserID: " + params.get("userID"));
			System.out.println("RemoteIP: " + params.get("remoteIP"));
			System.out.println("Timestamp: " + params.get("timestamp"));
			
			boolean validRequest = ServerAPI.validateRequestBody(params);
			
			exchange.sendResponseHeaders(200, requestBodyString.getBytes().length);
		    OutputStream os = exchange.getResponseBody();
		    os.write(ServerAPI.logRequest(params, validRequest).getBytes());
		    os.close();
		    
			System.out.println("-----------------------------------------");
		}
	 }
	
	
	/*-
	 * Request Handler to return a count of valid and invalid requests made by a specified
	 * customer on a specified date
	 */
	private static void hourlyStatsRequestHandler(HttpExchange exchange) throws IOException {		
		String parameterString = exchange.getRequestURI().getQuery();
		System.out.println("Parametrs: " + parameterString);
		
		String response = "";
		
		if(parameterString != null) {
			Map<String, String> params = new HashMap<String, String>();
			
			params = ServerAPI.formatRequestBody(parameterString);
			String param_custID = params.get("id");
			String param_date = params.get("date");
			
			if(param_custID != null && param_date != null) {
				/*-
				 * If the parameters ID and Date are present as part of the request parameters, a connection is made
				 * to the database, and the request Query is executed, retrieving all instances of the specified customer 
				 * id on the specified date found within the hourly_stats table. A string is created using this retrieved
				 * data and is sent as a response to the client.
				 */
				try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "password"))
				{
					String reqQuery = "SELECT * FROM hourly_stats INNER JOIN customer on (hourly_stats.customer_id = customer.id)"
							+ " WHERE hourly_stats.customer_id=" + param_custID
							+ " AND time::date = \'" + param_date + "\';";
					
					Statement selectStmt = connection.createStatement();
			        ResultSet selectResult = selectStmt.executeQuery(reqQuery);
			        
			        response = "";
			        while(selectResult.next()) {
			        	/*-
			        	 * '\n' was not being registered when sent as part of the response string, "&&&" is added to the
			        	 * end of each iteration to distinguish between each data entry returned from the table
			        	 */
			        	response = response + "(" + selectResult.getString("time") + ") "
			        			+ selectResult.getString("name") + " -> Valid Requests: " + selectResult.getString("request_count") + " | Invalid Requests: "
			        			+ selectResult.getString("invalid_count") + "&&&"; 
			        }
				}
				catch(Exception e) {
					System.out.println(e);
				}
			}
			/*-
			 * Although error checking is being used to validate input on the client class before being sent,
			 * the usage string is necessary if the hourly_stats data is being attempted to be retrieved from
			 * the browser
			 */
			else {
				response = "Usage: localhost:<port>/test?id=<customerID>&date=<yyyy-mm-dd>";
			}			
		}
		
		System.out.println("Response: " + response);
		
		exchange.sendResponseHeaders(200, response.length());
	    OutputStream os = exchange.getResponseBody();
	    os.write(response.getBytes());
	    os.close();
	}
	
	
	/*-
	 * Request Handler to return a list of the customers to the client 
	 */
	private static void hourlyStatsNamesRequestHandler(HttpExchange exchange) throws IOException {		
		String response = ServerAPI.getQueryResult("customer", null, "name", true);
				
		exchange.sendResponseHeaders(200, response.getBytes().length);
	    OutputStream os = exchange.getResponseBody();
	    os.write(response.getBytes());
	    os.close();
	}

}
