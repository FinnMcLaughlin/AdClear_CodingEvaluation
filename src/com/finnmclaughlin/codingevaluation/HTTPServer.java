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

	/* Main that creates localhost HttpServer using port 8085,
	 sets the handler function to handle requests sent to the server,
	 and starts server */
	public static void main(String[] args) throws IOException {
		
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8085), 0);
			HttpContext context = server.createContext("/");
		    context.setHandler(arg0 -> {
				try {
					handleRequest(arg0);
				} catch (JSONException e) {
					//e.printStackTrace();
				}
			});
		    HttpContext testContext = server.createContext("/test");
		    testContext.setHandler(HTTPServer::endpointHandleRequest);
		    HttpContext hourlyStats_getNameContext = server.createContext("/test/names");
		    hourlyStats_getNameContext.setHandler(HTTPServer::hourlyStatsRequestHandle);;
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
	
	/* Function to handle the endpoint for customer request statistics based on a specified date */
	private static void endpointHandleRequest(HttpExchange exchange) throws IOException {		
		String parameterString = exchange.getRequestURI().getQuery();
		System.out.println("Parametrs: " + parameterString);
		
		Map<String, String> params = new HashMap<String, String>();
		String usageString = "Usage: localhost:<port>/test?id=<customerID>&date=<yyyy-mm-dd>";
		String response = usageString;
		
		if(parameterString != null) {
			params = ServerAPI.formatRequestBody(parameterString);
			String param_custID = params.get("id");
			String param_date = params.get("date");
			
			if(param_custID != null && param_date != null) {				
				try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "password"))
				{
					String reqQuery = "SELECT * FROM hourly_stats INNER JOIN customer on (hourly_stats.customer_id = customer.id)"
							+ " WHERE hourly_stats.customer_id=" + param_custID
							+ " AND time::date = \'" + param_date + "\';";
					
					Statement selectStmt = connection.createStatement();
			        ResultSet selectResult = selectStmt.executeQuery(reqQuery);
			        
			        response = "";
			        while(selectResult.next()) {
			        	response = response + "(" + selectResult.getString("time") + ") " + selectResult.getString("name") + " -> Valid Requests: " + selectResult.getString("request_count") + " | Invalid Requests: " + selectResult.getString("invalid_count") + "&&&";
			        }
				}
				catch(Exception e) {
					System.out.println(e);
				}
			}
			else {
				response = usageString;
			}			
		}
		
		System.out.println("Response: " + response);
		
		exchange.sendResponseHeaders(200, response.length());
	    OutputStream os = exchange.getResponseBody();
	    os.write(response.getBytes());
	    os.close();
	}
		
	/* Function to handle the requests sent to the server */
	private static void handleRequest(HttpExchange exchange) throws IOException, JSONException {		
		
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
			
			System.out.println(buf.toString());
			
			Map<String, String> params = new HashMap<String, String>();
			params = ServerAPI.formatJSON(buf.toString());
			
//			Map<String, String> params = new HashMap<String, String>();
//			params = formatRequestBody(requestBodyString);
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
	
	
	private static void hourlyStatsRequestHandle(HttpExchange exchange) throws IOException {		
		String response = ServerAPI.getQueryResult("customer", null, "name", true);
				
		exchange.sendResponseHeaders(200, response.getBytes().length);
	    OutputStream os = exchange.getResponseBody();
	    os.write(response.getBytes());
	    os.close();
	}
}
