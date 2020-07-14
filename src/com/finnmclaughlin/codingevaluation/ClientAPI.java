package com.finnmclaughlin.codingevaluation;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ClientAPI {
	
	public static int getUserInput() {
		Scanner inputScanner = new Scanner(System.in);
		String userInputString = inputScanner.nextLine().replace(" ", "");
		
		if(ifValidInput(userInputString)) {
			return Integer.parseInt(userInputString);
		}
		else {
			System.out.println("\nINVALID INPUT: " + userInputString + "\nMust be integer\n\n");
			return -1;
		}		 
	}
	
	public static boolean ifValidInput(String userInputString) {
		try {
			Integer.parseInt(userInputString);
			return true;
		}
		catch(Exception e){
			return false;
		}
	}
	
	public static String getMenuText() {
		return "-----MAIN-----\n"
				+ "Send Request to Server (1)\n"
				+ "Get Hourly Stats from Server (2)\n"
				+ "Exit Program (3)\n"
				+ "What would you like to do? (Enter the digit)";
	}
	
	public static String getRequestOptionsText() {		
		return "\nSend Request-----\n"
				+ "What request would you like to send to the server?\n"
				+ "Valid Request (1)\n"
				+ "Invalid Request (2)\n"
				+ "Request Type: ";
	}
	
	public static String getInvalidRequestOptionsText(){		
		return "\nWhat type of invalid request would you like to send?\n"
				+ "Malformed JSON (1)\n"
				+ "Null Value JSON (2)\n"
				+ "Non-Existing Customer (3)\n"
				+ "Inactive Customer (4) \n"
				+ "Blacklisted User Agent (5)\n"
				+ "Blacklisted IP Address (6)\n"
				+ "Request Type: ";
	}
	
	public static String getHourlyStatsMenuText() throws IOException {		
		return "Which customer would you like to inquire about?\n"
				+ getCustomerNamesStringFormatted()
				+ "Customer ID: ";
	}
	
	public static boolean checkDateFormat(String date) {
		
		return true;
	}
	
	public static String getDateText() {	
		return "What date would you like to inquire about?\n"
				+ "(YYYY-MM-DD) Date: ";
	}
	
	public static String getCustomerNames() throws IOException {		
		URL url = new URL("http://localhost:8085/test/names");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		
		String names = readResponse(con);
				
		disconnectHttpURL(con);
	
		return names;
	}
	
	public static String formatResponseMessages(String respMessage) {
		String customerNamesString = "";
		
		if(respMessage.split("&&&").length > 0) {
			for(int nameIndex=0; nameIndex < respMessage.split("&&&").length; nameIndex++) {
				customerNamesString = customerNamesString + respMessage.split("&&&")[nameIndex] + " (" + (nameIndex+1) + ")\n";
			}
		}
					
		return customerNamesString;
	}
	
	public static String getCustomerNamesStringFormatted() throws IOException {					
		return formatResponseMessages(getCustomerNames());
	}
	
	public static int getCustomerNamesCount() throws IOException{
		return getCustomerNames().split("&&&").length;
	}
	
	public static String getRequestString(int index) {
		
		System.out.println("INDEX --> " + index);
		switch(index) {
		/* Valid JSON */
		case 1:
			return "{\"customerID\":2,\"tagID\":2,\"userID\":\"aaaaaaaa-bbbb-cccc-1111-222222222222\",\"remoteIP\":\"123.234.56.78\",\"timestamp\":1500000000}";
		
		/* Malformed JSON */ //----- TODO
		case 2:
			//"{\"customerID\":2,\"tagID\":2,\"userID\":\"aaaaaaaa-bbbb-cccc-1111-222222222222\",\"remoteIP\":\"123.234.56.78\",\"timestamp\":1500000000}";
			return "";
		
		/* Null Value JSON */
		case 3:
			return "{\"customerID\":2,\"tagID\": ,\"userID\":\"aaaaaaaa-bbbb-cccc-1111-222222222222\",\"remoteIP\":\"123.234.56.78\",\"timestamp\":1500000000}";
		
		/* Customer Does not exist */
		case 4:
			return "{\"customerID\":15,\"tagID\":2,\"userID\":\"aaaaaaaa-bbbb-cccc-1111-222222222222\",\"remoteIP\":\"123.234.56.78\",\"timestamp\":1500000000}";
		
		/* Inactive Customer */
		case 5:
			return "{\"customerID\":3,\"tagID\":2,\"userID\":\"aaaaaaaa-bbbb-cccc-1111-222222222222\",\"remoteIP\":\"123.234.56.78\",\"timestamp\":1500000000}";
		
		/* Blacklisted UserAgent */
		case 6:
			return "{\"customerID\":2,\"tagID\":2,\"userID\":\"Googlebot\",\"remoteIP\":\"123.234.56.78\",\"timestamp\":1500000000}";
		
		/* Blacklist IP Address */
		case 7:
			return "{\"customerID\":2,\"tagID\":2,\"userID\":\"aaaaaaaa-bbbb-cccc-1111-222222222222\",\"remoteIP\":\"213.070.64.33\",\"timestamp\":1500000000}";
			
		default: return "";
		}
	}
	
	public static void sendRequest(int requestType) throws IOException {
		URL url = new URL("http://localhost:8085/");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");	
		
		String reqString = getRequestString(requestType);
		
		con.setDoOutput(true);
		DataOutputStream out = new DataOutputStream(con.getOutputStream());
		out.writeBytes(reqString);
		out.flush();
		out.close();
		
		System.out.println(readResponse(con));
		
		disconnectHttpURL(con);
	}
	
	public static void getHourlyStats(int custId, String date) throws IOException {
		String params = "id=" + custId + "&date=" + date;
		
		URL url = new URL("http://localhost:8085/test?" + params);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		
		System.out.println(formatResponseMessages(readResponse(con)));
		
		disconnectHttpURL(con);
	}
	
	public static String readResponse(HttpURLConnection con) throws IOException {
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
	
	public static void disconnectHttpURL(HttpURLConnection con) {
		con.disconnect();
	}
}
