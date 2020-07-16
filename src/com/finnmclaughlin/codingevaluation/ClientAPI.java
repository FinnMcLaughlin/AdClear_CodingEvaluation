package com.finnmclaughlin.codingevaluation;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner; 

public class ClientAPI {
	
	static String JSON_REQUEST_URL = "http://localhost:8085/";
	static String STATS_REQUEST_URL = "http://localhost:8085/stats";
	static String NAMES_REQUEST_URL = "http://localhost:8085/stats/names";
	
	static String VALID_JSON = "{\"customerID\":2,\"tagID\":2,\"userID\":\"aaaaaaaa-bbbb-cccc-1111-222222222222\",\"remoteIP\":\"123.234.56.78\",\"timestamp\":1500000000}";
	static String MALFORMED_JSON = "{\"customerID\":2,\"tagID\"2,\"userID\":\"aaaaaaaa-bbbb-cccc-1111-222222222222\",\"remoteIP\":\"123.234.56.78\",\"timestamp\":1500000000}";
	static String MISSING_VALUE_JSON = "{\"customerID\":2,\"tagID\": ,\"userID\":\"aaaaaaaa-bbbb-cccc-1111-222222222222\",\"remoteIP\":\"123.234.56.78\",\"timestamp\":1500000000}";
	static String NON_EXIST_CUST_JSON = "{\"customerID\":15,\"tagID\":2,\"userID\":\"aaaaaaaa-bbbb-cccc-1111-222222222222\",\"remoteIP\":\"123.234.56.78\",\"timestamp\":1500000000}";
	static String INACTIVE_CUST_JSON = "{\"customerID\":3,\"tagID\":2,\"userID\":\"aaaaaaaa-bbbb-cccc-1111-222222222222\",\"remoteIP\":\"123.234.56.78\",\"timestamp\":1500000000}";
	static String BLACKLISTED_UA_JSON = "{\"customerID\":2,\"tagID\":2,\"userID\":\"Googlebot\",\"remoteIP\":\"123.234.56.78\",\"timestamp\":1500000000}";
	static String BLACKLISTED_IP_JSON = "{\"customerID\":2,\"tagID\":2,\"userID\":\"aaaaaaaa-bbbb-cccc-1111-222222222222\",\"remoteIP\":\"213.070.64.33\",\"timestamp\":1500000000}";
	
	
	/*-
	 * Function that returns the main client menu text
	 */
	public static String getMenuText() {
		return "\n-----MAIN-----\n"
				+ "Send Request to Server (1)\n"
				+ "Get Hourly Stats from Server (2)\n"
				+ "Exit Program (3)\n"
				+ "What would you like to do? (Enter the digit)";
	}
	
	
	/*-
	 * Function that returns the text prompting the user on which 
	 * type of request is to be sent to the server
	 */
	public static String getRequestOptionsText() {		
		return "\nSend Valid Request-----\n"
				+ "What request would you like to send to the server?\n"
				+ "Valid Request (1)\n"
				+ "Invalid Request (2)\n"
				+ "Custom JSON Request (3)\n"
				+ "Request Type: ";
	}
	
	
	/*-
	 * Function that returns the text prompting the user to input their
	 * custom JSON string
	 */
	public static String getCustomerJSONRequestText() {
		return "\nSend Custom JSON Request-----\n"
				+ "Enter custom JSON to send as request\n"
				+ "JSON: ";
	}
	
	
	/*-
	 * Function that returns the text prompting the user on which invalid
	 * request is to be sent to the server 
	 */
	public static String getInvalidRequestOptionsText(){		
		return "\nSend Invalid Request-----\n"
				+ "What type of invalid request would you like to send?\n"
				+ "Malformed JSON (1)\n"
				+ "Null Value JSON (2)\n"
				+ "Non-Existing Customer (3)\n"
				+ "Inactive Customer (4) \n"
				+ "Blacklisted User Agent (5)\n"
				+ "Blacklisted IP Address (6)\n"
				+ "Request Type: ";
	}
	
	
	/*-
	 * Function that returns the text prompting the user on which customer's
	 * hourly statistics info is to be retrieved from the server.
	 * 
	 * Because it is displaying information received from the server, error
	 * checking is put in place. If getCustomerNames() returns null, then
	 * there was difficulty either reading or writing to the server. If the
	 * length of the message before the first index (i.e. ("1")) is less
	 * than 1, then no data was returned from the server.
	 */
	public static String getHourlyStatsMenuText() throws IOException {
		String customerNames;
		
		if((customerNames = getCustomerNames()) == null) {
			return "\nHourly Statistics-----\n"
					+ "Unable To Connect\n";
		}
		else if(customerNames.split("(1)")[0].length() < 1) {
			return "\nHourly Statistics-----\n"
					+ "No Customers Found in table";
		}
		else {
			return "\nHourly Statistics-----\n"
					+ "Which customer would you like to inquire about?\n"
					+ getCustomerNamesStringFormatted(customerNames)
					+ "Customer ID: ";
		}
		
	}
	
	
	/*-
	 * Function that returns the text prompting the user to input the date
	 * of the data to be retrieved from the server in relation to viewing
	 * hourly statistics
	 */
	public static String getDateMenuText() {	
		return "What date would you like to inquire about?\n";
	}
	
	
	/*-
	 * Function that reads user input and validate that it is of numeric
	 * string value.
	 * 
	 * Most user input read by the client must be of numeric value, as most of
	 * the input consists of choosing between options in the UI menu or inputting a
	 * date value, although these user inputs are read in as strings. This is done
	 * due to the date dropping the initial '0' when being read in as an integer before
	 * being sent to the server to retrieve hourly statistics (i.e. 14-07-2020 became 14-7-2020)
	 * which prevented any retrieval of data from the tables, as the format had then changed.
	 * 
	 * However these inputs are still need to be validated to ensure they are of numeric value,
	 * to prevent any invalid input that might cause any sort of issues, as well as clearing 
	 * any found whitespace within the string    
	 */
	public static String getUserInput(Scanner inputScanner) {
		
		String userInputString = inputScanner.nextLine().replace(" ", "");
				
		if(ifValidInput(userInputString)) {
			return userInputString;
		}
		else {
			System.out.println("\nINVALID INPUT: " + userInputString + "\nMust be integer\n\n");
			return "-1";
		}		 
	}
	
	
	/*-
	 * Function to read in user input of custom JSON strings.
	 * 
	 * This is the one instance of the user input not being a
	 * numeric value string, and so it does not need to be validated.
	 * The server will validate whether the JSON string is malformed
	 * or not.
	 */
	public static String getUserInput_JSON(Scanner inputScanner) {		
		return inputScanner.nextLine();		 
	}
	
	
	/*-
	 * Function that gets the user's date input, validating the
	 * user's input each time.
	 */
	public static String getDateInput(Scanner inputScanner) {
		String date_day;
		String date_month;
		String date_year;				
		
		System.out.println(getDateMenuText());
		
		do {
			System.out.println("(dd) Day: " );
			date_day = getUserInput(inputScanner);
		}
		while(Integer.parseInt(date_day) < 0);
		 
		do {
			System.out.println("(mm) Month: " );
			date_month = getUserInput(inputScanner);
		}
		while(Integer.parseInt(date_month) < 0);
		
		do {
			System.out.println("(yyyy) Year: " );
			date_year = getUserInput(inputScanner);
		}
		while(Integer.parseInt(date_year) < 0);
							
		return date_year + "-" + date_month + "-" + date_day;
	}

	
	/*-
	 * Function to check that the user input string is numeric
	 */
	public static boolean ifValidInput(String userInputString) {
		try {
			Integer.parseInt(userInputString);
			return true;
		}
		catch(Exception e){
			return false;
		}
	}
	
	
	/*-
	 * Function to retrieve the customer's from the server
	 */
	public static String getCustomerNames() throws IOException {		
		try{
			URL url = new URL(NAMES_REQUEST_URL);
			HttpURLConnection con = connectHttpURL(url);
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			String names = readResponse(con);
							
			disconnectHttpURL(con);
			
			return names;
		}
		catch(Exception e) {
			return "Cannot Connect to Server";
		}
	}

	
	/*-
	 * Function to return the customer names, formatted and indexed, for the purpose
	 * of giving the user a list of options to choose from when requesting hourly 
	 * statistic info
	 */
	public static String getCustomerNamesStringFormatted(String customerNames) throws IOException {					
		return formatResponseString(customerNames, true);
	}
	
	
	/*-
	 * Function to retrieve the number of customers present in the customer table, from
	 * the server
	 */
	public static int getCustomerNamesCount() throws IOException{
		String customerNames;
		
		if((customerNames = getCustomerNames()) == null) {
			return -1;
		}
		else {
			return customerNames.split("&&&").length;
		}
	}
	
	
	/*-
	 * Function to return the specified request JSON string to be sent to the server based
	 * on the given index 
	 */
	public static String getPreSetJSON(int index) {
						
		switch(index) {
		/* Valid JSON */
		case 1:
			return VALID_JSON;
		
		/* Malformed JSON */
		case 2:
			return MALFORMED_JSON;
		
		/* Null Value JSON */
		case 3:
			return MISSING_VALUE_JSON;
		
		/* Customer Does not exist */
		case 4:
			return NON_EXIST_CUST_JSON;
		
		/* Inactive Customer */
		case 5:
			return INACTIVE_CUST_JSON;
		
		/* Blacklisted UserAgent */
		case 6:
			return BLACKLISTED_UA_JSON;
		
		/* Blacklist IP Address */
		case 7:
			return BLACKLISTED_IP_JSON;
			
		default: return "";
		}
	}
	
	
	/*-
	 * Function to send a pre-set JSON to the server. The pre-set JSON is picked by
	 * the user, and the index of which is sent to getPreSetJSON(), which returns
	 * the JSON string before sending it to the server. The response from the server
	 * is read and displayed for the user
	 */
	public static void sendRequest(int requestType) throws IOException {
		URL url = new URL(JSON_REQUEST_URL);
		String response = "";
		
		try {
			HttpURLConnection con = connectHttpURL(url);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");	
			
			String reqString = getPreSetJSON(requestType);
			
			System.out.println("JSON: " + reqString);
			
			writeToServer(con, reqString);
			
			if((response = readResponse(con)) != null) {
				System.out.println(response);
			}
			
			disconnectHttpURL(con);
		}
		catch(Exception e) {
			System.out.println("Send Request Failed");
		}
	}
	
	
	/*-
	 * Function to send a custom JSON to the server. The custom JSON is given by
	 * the user, and is sent to the server. The response from the server is read
	 * and displayed for the user
	 * 
	 * When a very malformed JSON was being sent (e.g. "{}") the server would execute
	 * as normal when dealing with a malformed JSON, although the client would read a
	 * empty string upon receiving the response. To combat this, the function checks
	 * whether the response is an empty string, and if so it would be counted as a
	 * malformed JSON response message from the server. 
	 */
	public static void sendRequest_customJSON(String json_String) throws IOException {
		URL url = new URL(JSON_REQUEST_URL);
		String response;
		
		try{	
			HttpURLConnection con = connectHttpURL(url);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			
			System.out.println("JSON: " + json_String);
			
			writeToServer(con, json_String);
			
			if((response = readResponse(con)) != null) {
				if(response.length() < 1) {
					System.out.println("Request Was Unsuccessful. Malformed JSON");
				}
				
				System.out.println(response);
			}
			
			disconnectHttpURL(con);
		}
		catch(Exception e) {
			System.out.println("Send Request Failed");
		}
	}
	
	
	/*-
	 * Function that retrieves the hourly statistics of a specific customer on a specific
	 * date from the server, and displays them to the user. The specific customer is found
	 * using the given customer ID, and the specific date is found using the given date string,
	 * both of which are sent as parameters to the server.
	 */
	public static void getHourlyStats(int custId, String date) throws IOException {
		String params = "id=" + custId + "&date=" + date;
				
		URL url = new URL(STATS_REQUEST_URL + "?" + params);
		
		try{
			HttpURLConnection con = connectHttpURL(url);
			
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			System.out.println("\n" + formatResponseString(readResponse(con), false));
			
			disconnectHttpURL(con);
		}
		catch(Exception e) {
			System.out.println("\nCannot get hourly statistics\nIssue Connecting to Server");
		}
	}
	
	
	/*-
	 * Function to format the response from the server.
	 * 
	 * Due to '\n' not being recognised as a line break when sent from the server as a response,
	 * "&&&" has been used to denote a line break, and so when formatting the response message,
	 * the message is split where "&&&" is found.
	 * 
	 * A boolean is sent to the function to denote whether each new line of the response is to be
	 * indexed, such as when the response text is used as part of the client UI (i.e. displaying
	 * the customer names). 
	 */
	public static String formatResponseString(String respString, boolean indexMessage) {
		String formattedResponse = "";
						
		if(respString.split("&&&").length > 0) {
			for(int nameIndex=0; nameIndex < respString.split("&&&").length; nameIndex++) {
				
				if(indexMessage) {
					formattedResponse = formattedResponse + respString.split("&&&")[nameIndex] + " (" + (nameIndex+1) + ")\n";
				}
				else {
					formattedResponse = formattedResponse + respString.split("&&&")[nameIndex] + "\n";
				}
				
			}
		}
		else {
			formattedResponse = "\nNO DATA FOUND";
		}
					
		return formattedResponse;
	}
	
	
	/*-
	 * Function sends a given string to a server by opening a data output stream. The 
	 * server is given via the HttpURLConnection sent to the function.  
	 */
	public static void writeToServer(HttpURLConnection connection, String messageString) {
		try {
			connection.setDoOutput(true);
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			out.writeBytes(messageString);
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("\nCANNOT WRITE TO SERVER\n");
		}
		
	}
	
	
	/*-
	 * Function that reads the response given from a server by opening an input stream
	 * reader, before returning the response as a string. The server is given via the
	 * HttpURLConnection sent to the function. 
	 */
	public static String readResponse(HttpURLConnection con) throws IOException {
		try {
			BufferedReader in = new BufferedReader(
					  new InputStreamReader(con.getInputStream()));
					String inputLine;
					StringBuffer content = new StringBuffer();
					while ((inputLine = in.readLine()) != null) {
					    content.append(inputLine);
					}
					in.close();		
					
			return content.toString();
		}
		catch(Exception e) {
			return null;
		}
		
	}
	
	
	/*-
	 * Function to create an open HTTP connection with the given URL
	 */
	public static HttpURLConnection connectHttpURL(URL url) throws IOException {
		return (HttpURLConnection) url.openConnection();
	}
	
	
	/*-
	 * Function to disconnect the given HttpURLConnection
	 */
	public static void disconnectHttpURL(HttpURLConnection connection) {
		connection.disconnect();
	}
}
