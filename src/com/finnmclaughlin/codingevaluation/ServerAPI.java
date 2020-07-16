package com.finnmclaughlin.codingevaluation;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerAPI {
	
	static String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
	static String DB_USER = "postgres";
	static String DB_PWD = "password";
	
	static String DATE_FORMAT = "yyyy-MM-dd";
	
	/*-
	 * Function to connect to Database using the static variables
	 * declared at the top of the ServerAPI class
	 */
	public static Connection connectToDB() throws SQLException {
		return DriverManager.getConnection(DB_URL, DB_USER, DB_PWD);
	}
	
	
	/*-
	 * Function to format the JSON String sent as a request to the server
	 * by looking for missing parameter values and explicitly stating "null"
	 * for future validation, before placing the values within a Hashmap and
	 * returning it. Most of the functionality of the function is surrounded
	 * within a try/catch, where if unsuccessful that whether the JSON is malformed
	 */
	public static Map<String, String> formatJSON(String inputStream){		
		Map<String, String> key_values = new HashMap<String, String>();
		
		try{
			inputStream = inputStream.replace(" ", "");
			inputStream = inputStream.replace(":,", ":null,");
			inputStream = inputStream.replace(":}", ":null}");
			
			JSONObject json = new JSONObject(inputStream);
			
			System.out.println(json);
			
			JSONArray keys = json.names();
			
			if(keys != null) {
				for(int keyIndex=0; keyIndex < keys.length(); keyIndex++) {
					key_values.put(keys.getString(keyIndex), json.get(keys.getString(keyIndex)).toString());
				}
			}
			else {
				key_values.put("Malformed_JSON", "null");
			}
		}
		catch(JSONException e) {
			System.out.println("MALFORMED JSON\n");
		}
		
		return key_values;
	}
	
	
	/*-
	 * Function to validate the values sent via the JSON request, returning a
	 * boolean value upon completion. Validation includes making sure there are 
	 * no missing values, that the customer exists in the customer table, that
	 * the customer is an active customer, that the IP Address and User Agents
	 * used are not blacklisted.
	 */
	public static boolean validateJSONRequest(Map<String, String> params) {		
		boolean validRequest = true;
		
		//Missing Value
		for(Map.Entry<String, String> parameter : params.entrySet()) {	
			if(parameter.getValue().compareTo("null") == 0) {
				validRequest = false;
				break;
			}
		}
		
		//Not in Customer Table || Disabled Customer
		if(validRequest) {
			String customerCheck = getQueryResult("Customer", "id=" + params.get("customerID"), "active", false);
			if(customerCheck.compareTo("") == 0 || customerCheck.compareTo("0") == 0) {
				validRequest = false;
			}
		}
		
		//Blacklisted IP
		if(validRequest) {
			String ipCheck = getQueryResult("ip_blacklist", "ip=" + params.get("remoteIP").replace(".", ""), "ip", false);
			if(ipCheck.compareTo("") != 0) {
				validRequest = false;
			}
		}
		
		//Blacklist User Agent
		if(validRequest) {
			String userAgentCheck = getQueryResult("ua_blacklist", "ua= \'" + params.get("userID") + "\'", "ua", false);
			if(userAgentCheck.compareTo("") != 0) {
				validRequest = false;
			}
		}
		
		return validRequest;
	}	
	
	
	/*-
	 * Compares the given date string to the date format of the database,
	 * by attempting to parse the string into the date format, and returns
	 * a boolean value to denote the validity
	 */
	public static boolean validateDateFormat(String dateInput) {
		DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		formatter.setLenient(false);
		try {
			formatter.parse(dateInput);
		    return true;
		} catch (Exception e) {
			System.out.println("Invalid date format used");
			return false;
		}
	}
	
	
	/*-
	 *  Function to format the parameters sent when retrieving data from the 
	 *  hourly_stats table, by looking for missing parameter values and explicitly
	 *  stating "null" for future validation, before placing the values within a
	 *  Hashmap and returning it
	 */
	public static Map<String, String> formatHourlyStatsParams(String parameterString){
		Map<String, String> info = new HashMap<String, String>();
		
		/* Gets the number of & in the string, which separates 2 parameters*/
		int paramCount = parameterString.length() - parameterString.replace("&", "").length() + 1;
		
		
		parameterString = parameterString.replace("=&", "=null&");
		
		if(parameterString.charAt(parameterString.length()-1) == '=') {
			parameterString = parameterString.concat("null");
		}
		
		for(int index=0; index < paramCount; index++) {
			String key = parameterString.split("&")[index].split("=")[0];
			String value = parameterString.split("&")[index].split("=")[1];
			info.put(key, value);
		}
		
		return info;
	}

	
	/*-
	 * Function that executes a SELECT statement from a specified table and returns data
	 * from a specified column within the table. If a condition is passed to the function,
	 * then that condition is executed in the statement, otherwise all instances from the
	 * specified table are returned.
	 * 
	 * A boolean value is also passed to the function to 
	 * determine whether the SELECT statement is being used to execute an hourly_stats
	 * request, if this is the case then "&&&" is added to the end of each iteration to distinguish
	 * between each data entry returned from the table, as '\n' was not being registered when sent
	 * as part of the response string 
	 */
	public static String getQueryResult(String table, String cond, String column, boolean hourlyStatsRequest) {
		
		try (Connection connection = connectToDB())
		{
			String reqQuery = "";
			
			if(cond == null) {
				reqQuery = "SELECT * FROM " + table + ";";
			}
			else {
				reqQuery = "SELECT * FROM " + table + " WHERE " + cond + ";";
			}
			
			
			Statement stmt = connection.createStatement();
	        ResultSet rs = stmt.executeQuery(reqQuery);
	        String response = "";
	        String result_split = "";
	        
	        if(hourlyStatsRequest) {
	        	result_split = "&&&";
	        }
	        
	        while ( rs.next() )
	        {
	        	response = response + rs.getString(column) + result_split;
	        }	
	        	        
	        return response;
	
		}
		catch (Exception e) {
	        System.out.println("Connection failure.");
	        e.printStackTrace();
	        
	        return "Connection Failure";
	    }
	}

	
	/*-
	 * Function to create a response message following a JSON request. The response message
	 * is determined by whether the JSON string was malformed, if the customer ID exists in
	 * the customer table (if not it cannot be logged in the requestLog table), and if the
	 * JSON request is in itself a valid request
	 */
	public static String createResponseMessage(boolean malformedJSON, boolean customerExists, boolean validRequest) {
		String responseMessage = "";
		
		if(malformedJSON) {
			responseMessage = "Request Was Unsuccessful. Malformed JSON";
		}
		else {
			if(customerExists) {
				if(validRequest) {
					responseMessage = "Request Made Successfully";
				}
				else {
					responseMessage = "Invalid Request. Request Was Unsuccessful";
				}
			}
			else {
				responseMessage = "CustomerID does not exist. Request Was Unsuccessful";
			}
		}
		
		return responseMessage;	
	}

	
	/*-
	 * Function to log a request into the logRequest table, which includes the customers ID and whether
	 * or not the request made was valid. The function first checks if the customer has already made a 
	 * request within the hour, and if so is present within the logRequest table. If they are not present,
	 * the valid and invalid request counts are initialised, the relevant count is incremented and they are
	 * both added to the logRequest table, along with the customerID. If they are present, the relevant
	 * request count of the specified customer ID is incremented within the table. A response message is
	 * returned, which is created by calling the responseMessage() function
	 */
	public static String logRequest(Map<String, String> params, boolean validRequest) {	
		try (Connection connection = connectToDB())
		{
			String custID = params.get("customerID");
			String logQuery = "";
				
			boolean customerExists_customerTable = true;
			boolean malformedJSON = false;
			
			if(custID == null) {
				malformedJSON  = true;
			}
			else {
				String customerExists_requestLogTable = getQueryResult("requestLog", "customerId=" + params.get("customerID"), "customerId", false);
				
				if(customerExists_requestLogTable.compareTo("") == 0) {												
					int valid_request = 0;
					int invalid_request = 0;
					
					if(validRequest) {
						valid_request = valid_request + 1;
					}
					else {
						invalid_request = invalid_request + 1;
					}
											
					
					if(getQueryResult("customer", "id=" + params.get("customerID"), "name", false).length() > 0) {
						logQuery = "INSERT INTO requestLog VALUES (" + custID + "," + valid_request + "," + invalid_request + ");";
					}
					else {
						customerExists_customerTable = false;
					}
				}
				else {				
					String update_column = "";
					int update_count = -1;
					
					if(validRequest) {
						update_column = "request_count";
						update_count = Integer.parseInt(getQueryResult("requestLog", "customerId=" + params.get("customerID"), "request_count", false));
					}
					else {
						update_column = "invalid_count";
						update_count = Integer.parseInt(getQueryResult("requestLog", "customerId=" + params.get("customerID"), "invalid_count", false));
					}
					
					if(update_column.compareTo("") != 0 && update_count >= 0) {
						logQuery = "UPDATE requestLog SET " + update_column + "=" + (update_count + 1) + " WHERE customerID = " + custID + ";";
					}
					
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
						
			return createResponseMessage(malformedJSON, customerExists_customerTable, validRequest);
		}
		catch (Exception e) 
		{
	        System.out.println(e);
	        e.printStackTrace();
	        
	        return "Database Error. Request Was Unsuccessful";
	    }
	}
	
	
	/*-
	 * Function to remove the data from the requestLog table and add it to the hourly_stats table.
	 * A timestamp is created and added with each entry to denote the hour in which the transfer
	 * takes place.
	 */
	public static void updateHourlyStatsTable() {		
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		System.out.println("\n" + timeStamp);
		
		try (Connection connection = connectToDB())
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