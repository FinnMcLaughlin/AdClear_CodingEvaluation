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
		    //server.stop(0);
		}
		catch(Exception e){
			System.out.println(e);
		}	
		
	}
	
	/* Function to format the request body string into a parameter/value Hashmap */
	private static Map<String, String> formatRequestBody(String reqBody){
		Map<String, String> info = new HashMap<String, String>();
		
		//Gets the parameter count based on the number of '&' characters found
		//in the request body string
		int paramCount = reqBody.length() - reqBody.replace("&", "").length() + 1;
		System.out.println("Paramter Count: " + paramCount);
		
		//Check to ensure there are no null values within or at the end of the
		//request body string. If so, inserts "null" into the specified location
		//as to enable easier distinction for later
		reqBody = reqBody.replace("=&", "=null&");
		
		if(reqBody.charAt(reqBody.length()-1) == '=') {
			reqBody = reqBody.concat("null");
		}
		
		//Extracts the parameters and values and stores them in the Hashmap
		for(int index=0; index < paramCount; index++) {					
			String key = reqBody.split("&")[index].split("=")[0];
			String value = reqBody.split("&")[index].split("=")[1];
			info.put(key, value);
			
//			System.out.println("\nKey: " + key + "  |  Value: " + value);
		}
		
		return info;
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
			
			exchange.sendResponseHeaders(200, requestBodyString.getBytes().length);//response code and length
		    OutputStream os = exchange.getResponseBody();
		    os.write(requestBodyString.getBytes());
		    os.close();
			
			System.out.println(buf.toString());
			
			Map<String, String> info = new HashMap<String, String>();
			info = formatRequestBody(requestBodyString);
			System.out.println("CustomerID: " + info.get("customerID"));
			System.out.println("TagID: " + info.get("tagID"));
			System.out.println("UserID: " + info.get("userID"));
			System.out.println("RemoteIP: " + info.get("remoteIP"));
			System.out.println("Timestamp: " + info.get("timestamp"));
			
		}
		else {
			System.out.println("Not POST Request");
		}
		
		
	   /*System.out.println("Establishing Connection to Database...");	
	   try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres",
                    "postgres", "password")) {
		 
			System.out.println("Connection Established");
			
			Statement stmt = connection.createStatement();
	        ResultSet rs = stmt.executeQuery( "SELECT * FROM Customer;" );
	        String response = "";
	
	        while ( rs.next() ) {
	        	System.out.println("Name: " + rs.getString("name") + " | Active: " + rs.getInt("active"));
	        	response = response + "Name: " + rs.getString("name") + " | Active: " + rs.getInt("active") + "\n";
	        }
	        
		    exchange.sendResponseHeaders(200, response.getBytes().length);//response code and length
			    OutputStream os = exchange.getResponseBody();
			    os.write(response.getBytes());
			    os.close();
	 
	        }catch (Exception e) {
	            System.out.println("Connection failure.");
	        e.printStackTrace();
        }*/
	 }
}
