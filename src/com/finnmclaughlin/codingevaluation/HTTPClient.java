package com.finnmclaughlin.codingevaluation;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


public class HTTPClient {
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub	
		
//		URL url = new URL("http://localhost:8085/test?id=2&date=2020-07-12");
//		HttpURLConnection con = (HttpURLConnection) url.openConnection();
//		con.setRequestMethod("GET");
//		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		
		URL url = new URL("http://localhost:8085/");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");
		
		String reqString = "";
		/* Valid JSON */
		reqString = "{\"customerID\":2,\"tagID\":2,\"userID\":\"aaaaaaaa-bbbb-cccc-1111-222222222222\",\"remoteIP\":\"123.234.56.78\",\"timestamp\":1500000000}";
		/* Malformed JSON */ //----- TODO
		//reqString = "{\"customerID\":2,\"tagID\":2,\"userID\":\"aaaaaaaa-bbbb-cccc-1111-222222222222\",\"remoteIP\":\"123.234.56.78\",\"timestamp\":1500000000}";
		/* Null Value JSON */
		//reqString = "{\"customerID\":2,\"tagID\": ,\"userID\":\"aaaaaaaa-bbbb-cccc-1111-222222222222\",\"remoteIP\":\"123.234.56.78\",\"timestamp\":1500000000}";
		/* Customer Does not exist */
		//reqString = "{\"customerID\":15,\"tagID\":2,\"userID\":\"aaaaaaaa-bbbb-cccc-1111-222222222222\",\"remoteIP\":\"123.234.56.78\",\"timestamp\":1500000000}";
		/* Inactive Customer */
		//reqString = "{\"customerID\":3,\"tagID\":2,\"userID\":\"aaaaaaaa-bbbb-cccc-1111-222222222222\",\"remoteIP\":\"123.234.56.78\",\"timestamp\":1500000000}";
		/* Blacklisted UserAgent */
		//reqString = "{\"customerID\":2,\"tagID\":2,\"userID\":\"Googlebot\",\"remoteIP\":\"123.234.56.78\",\"timestamp\":1500000000}";
		/* Blacklist IP Address */
		//reqString = "{\"customerID\":2,\"tagID\":2,\"userID\":\"aaaaaaaa-bbbb-cccc-1111-222222222222\",\"remoteIP\":\"213.070.64.33\",\"timestamp\":1500000000}";
		
		con.setDoOutput(true);
		DataOutputStream out = new DataOutputStream(con.getOutputStream());
		out.writeBytes(reqString);
		out.flush();
		out.close();
		
		BufferedReader in = new BufferedReader(
				  new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer content = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
				    content.append(inputLine);
				}
				in.close();
				
		System.out.println(content.toString());
		
		con.disconnect();
	}
	
	public static String ParameterStringBuilder(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
 
        for (Map.Entry<String, String> entry : params.entrySet()) {
          result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
          result.append("=");
          result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
          result.append("&");
        }
 
        String resultString = result.toString();
        
        System.out.println(resultString);
        
        if(resultString.length() > 0) {
        	return resultString.substring(0, resultString.length() - 1);
        }
        else {
        	return resultString;
        }        
	}
}

