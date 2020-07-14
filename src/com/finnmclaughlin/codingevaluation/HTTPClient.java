package com.finnmclaughlin.codingevaluation;

import java.io.IOException;
import com.finnmclaughlin.codingevaluation.ClientAPI;

public class HTTPClient {
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub	
				
		boolean exitProgram = false;
		
		while(!exitProgram) {
			System.out.println(ClientAPI.getMenuText());
			int userInput = ClientAPI.getUserInput();			
				
			if(userInput > 0) {
				boolean backToMain = false;
				
				switch(userInput) {			
				case 1:
					System.out.println("Case 1\n");
					while(!backToMain) {
						System.out.println(ClientAPI.getRequestOptionsText());
						userInput = ClientAPI.getUserInput();
						
						switch(userInput) {
						case 1:
							ClientAPI.sendRequest(1);
							backToMain = true;
							break;
						
						case 2:
							System.out.println(ClientAPI.getInvalidRequestOptionsText());
							userInput = ClientAPI.getUserInput();
							if (userInput > 0 && userInput < 7) {
								ClientAPI.sendRequest(userInput + 1);
							}
							backToMain = true;
							break;
						
						default:
							backToMain = true;
							exitProgram = true;
						}	
					}			
					break;
				
				
					
					
				case 2:
					System.out.println(ClientAPI.getHourlyStatsMenuText());
					userInput = ClientAPI.getUserInput();
					
					if(userInput > 0 && userInput < ClientAPI.getCustomerNamesCount()+1) {
						int customerID = userInput;
						
						System.out.println(ClientAPI.getDateText());
						userInput = ClientAPI.getUserInput();
						
						ClientAPI.getHourlyStats(customerID, "2020-07-12");
					}
					else {
						System.out.println("\nINVALID INPUT: " + userInput + "\nOut of bounds input\n");
					}
					
					break;
				
				
					
					
					
					
					
					
					
				case 3:
					System.out.println("Case 3\n");
					System.out.println("Exiting Program...\n");
					exitProgram = true;
					break;
					
				default:
					System.out.println("Invalid");
				}
			}		
		}
		
	}
}

