package com.finnmclaughlin.codingevaluation;

import java.io.IOException;
import java.util.Scanner;

import com.finnmclaughlin.codingevaluation.ClientAPI;

public class HTTPClient {
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub	
				
		boolean exitProgram = false;
		Scanner inputScanner = new Scanner(System.in);
		
		while(!exitProgram) {
			System.out.println(ClientAPI.getMenuText());
			int userInput = Integer.parseInt(ClientAPI.getUserInput(inputScanner));			
				
			if(userInput > 0) {
				boolean backToMain = false;
				
				switch(userInput) {
				
				case 1:
					while(!backToMain) {
						System.out.println(ClientAPI.getRequestOptionsText());
						userInput = Integer.parseInt(ClientAPI.getUserInput(inputScanner));
						
						switch(userInput) {
						case 1:
							ClientAPI.sendRequest(1);
							backToMain = true;
							break;
						
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