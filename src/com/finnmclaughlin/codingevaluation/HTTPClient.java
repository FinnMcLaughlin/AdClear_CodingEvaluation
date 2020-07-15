package com.finnmclaughlin.codingevaluation;

import java.io.IOException;
import java.util.Scanner;

import com.finnmclaughlin.codingevaluation.ClientAPI;

public class HTTPClient {
	public static void main(String[] args) throws IOException {
				
		boolean exitProgram = false;
		Scanner inputScanner = new Scanner(System.in);
		/*-
		 * Nested while loops and switch statements which act as a UI allowing the
		 * user to test out the various features of HTTPServer.java such as:
		 * 
		 *  Sending requests to the server;
		 *  	- Sending pre-written JSON strings, both valid and invalid
		 *  	- Sending custom JSON string that the user can input
		 *  
		 *  Retrieving data from the hourly_stats table;
		 *  	- Specifying a customer based on a given list of customers
		 *  		taken from the customer table
		 *  	- Specifying a date based on user input of the day, month
		 *  		and year
		 *  
		 *  Exiting the program
		 */
		while(!exitProgram) {
			System.out.println(ClientAPI.getMenuText());
			int userInput = Integer.parseInt(ClientAPI.getUserInput(inputScanner));			
				
			if(userInput > 0) {
				boolean backToMain = false;
				
				switch(userInput) {
				
				/* Case to handle sending requests to the server */
				case 1:
					while(!backToMain) {
						System.out.println(ClientAPI.getRequestOptionsText());
						userInput = Integer.parseInt(ClientAPI.getUserInput(inputScanner));
						
						switch(userInput) {
						
						/* Case to handle sending valid pre-written JSON strings */
						case 1:
							ClientAPI.sendRequest(1);
							backToMain = true;
							break;
						
						/* Case to handle sending invalid pre-written JSON strings */
						case 2:
							System.out.println(ClientAPI.getInvalidRequestOptionsText());
							userInput = Integer.parseInt(ClientAPI.getUserInput(inputScanner));
							
							if (userInput > 0 && userInput < 7) {
								ClientAPI.sendRequest(userInput + 1);
								backToMain = true;
							}
							else {
								System.out.print("Out of Bounds Input. Must be between 1-7\n");
							}
							
							break;
						
						/* Case to handle sending custom JSON strings */
						case 3:
							System.out.println(ClientAPI.getCustomerJSONRequestText());
							ClientAPI.sendRequest_customJSON(ClientAPI.getUserInput_JSON(inputScanner));
							
							backToMain = true;
							break;
						
						default:
							System.out.print("Out of Bounds Input. Must be between 1-2\n");
						}	
					}			
					break;			
					
				/* Case to handle retrieving data from the hourly_stats table */
				case 2:
					while(!backToMain) {
						System.out.println(ClientAPI.getHourlyStatsMenuText());
						userInput = Integer.parseInt(ClientAPI.getUserInput(inputScanner));
						
						if(userInput > 0 && userInput <= ClientAPI.getCustomerNamesCount()) {
							int customerID = userInput;
							String date = ClientAPI.getDateInput(inputScanner);
						
							ClientAPI.getHourlyStats(customerID, date);
							backToMain = true;
						}
						else {
							System.out.print("Out of Bounds Input. Must be between 1-" + ClientAPI.getCustomerNamesCount() + "\n");
						}
						
					}
					
					break;

				/* Case to exit the program */
				case 3:
					System.out.println("Exiting Program...\n");
					inputScanner.close();
					exitProgram = true;
					break;
				
					
				default:
					System.out.println("Invalid");
				}
			}		
		}
		
	}
}