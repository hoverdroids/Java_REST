package com.hoverdroids.java.rest;

//This is derived from http://androidexample.com/Restful_Webservice_Call_And_Get_And_Parse_JSON_Data-_Android_Example/index.php?view=article_discription&aid=101&aaid=123

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Main {
	
	private static final int TIMEOUT = 5000;//ms
	
	private static final String URL = "http://www.androidexample.com/media/webservice/JsonReturn.php";
	
	//The following keys are API-specific. By pasting the target url into a web browser, 
	//we know that the returned JSON Object will look like:
	//{"Android":[
	//	{"name":"1","number":"1","date_added":"1"},
	//	{"name":"2","number":"2","date_added":"2"},
	//	{"name":"3","number":"3","date_added":"3"}]
	//}
	//In other words:
	//key=Android value=Array[3]
	//Array[0] key=name value=1 ... key=number value=1 ... key=date_added value=1
	//Array[1] key=name value=2 ... key=number value=2 ... key=date_added value=2
	//Array[2] key=name value=3 ... key=number value=3 ... key=date_added value=3
	private static final String KEY_ANDROID = "Android";
	private static final String KEY_NAME = "name";
	private static final String KEY_NUMBER = "number";
	private static final String KEY_DATE_ADDED = "date_added";
	
	
	public static void main(String[] args){
		//The site with a RESTful API
		getJsonAndPrint(URL);	
	}
	
	public static String getJsonAndPrint(String restUrl){
		String jsonString = "No JSON found";
		
		try {
			
			//Create a conn with the target url
			URL url = new URL(restUrl);			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setReadTimeout(TIMEOUT);
			conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
        	conn.addRequestProperty("User-Agent", "Mozilla");
        	conn.addRequestProperty("Referer", "google.com");
        	
        	//Get the strema before looking at the response code - else the following error is thrown:
        	//"Cannot write output after reading input."
        	OutputStream outStream = conn.getOutputStream();
        	        	
        	int status = conn.getResponseCode();
        	if(status == HttpURLConnection.HTTP_OK){
        		//Go ahead and get the data now...
        		System.out.println("Connection OK");
        		jsonString = toJsonString(conn, outStream);
        		printlnJsonOject(jsonString);
        		outStream.close();
        		return jsonString;
        		
        	}else if(!(status == HttpURLConnection.HTTP_MOVED_TEMP
        				|| status == HttpURLConnection.HTTP_MOVED_PERM
        					|| status == HttpURLConnection.HTTP_SEE_OTHER)){
        		//The connections is no good so don't try to get the data and show the error
        		System.out.println("Connection Error ... status:" + status);
        		outStream.close();
        		return jsonString;
        		
        	}else{
        		outStream.close();
        		
        		//Go to the redirect and see if we have better luck getting the info
        		System.out.println("Redirecting ... ");
        		
        		// get redirect url from "location" header field
    			String newUrl = conn.getHeaderField("Location");

    			// get the cookie if need, for login
    			String cookies = conn.getHeaderField("Set-Cookie");

    			// open the new connection again
    			conn = (HttpURLConnection) new URL(newUrl).openConnection();
    			conn.setDoOutput(true);
    			conn.setReadTimeout(TIMEOUT);
    			conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            	conn.addRequestProperty("User-Agent", "Mozilla");
            	conn.addRequestProperty("Referer", "google.com");
    			conn.setRequestProperty("Cookie", cookies);
    			
    			outStream = conn.getOutputStream();
    			
    			if(status == HttpURLConnection.HTTP_OK){
            		//Go ahead and get the data now...
            		jsonString = toJsonString(conn, outStream);
            		printlnJsonOject(jsonString);
            		return jsonString;
            		
    			}else{
    				//The connections is no good so don't try to get the data and show the error
            		System.out.println("Error code:" + status);
            		return jsonString;
    			}
        	}
        		
		} catch (MalformedURLException e) {
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();	
		}
		
		return jsonString;
	}
	
	public static String toJsonString(HttpURLConnection conn, OutputStream outStream){

		String output = "BAD";
		try {
			OutputStreamWriter outputStreamWr = new OutputStreamWriter(outStream);
			
			outputStreamWr.flush();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;
			
			while((line = br.readLine()) != null) {
				sb.append(line);
				sb.append(System.getProperty("line.separator"));
			}
			
			output = sb.toString();
			
			br.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		return output;
	}
	
	public static void printlnJsonOject(String jsonString){
		System.out.println(jsonString);
		if(jsonString == null){
			System.out.println("Null jsonObject");
			return;
		}
		try {
			JSONObject jsonResponse = new JSONObject(jsonString);			
			JSONArray jsonArray = jsonResponse.optJSONArray(KEY_ANDROID);
						
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject el = jsonArray.getJSONObject(i);
							
				System.out.println(KEY_ANDROID + ":");
				System.out.println(KEY_NAME + ":" + el.getString(KEY_NAME));
				System.out.println(KEY_NAME + ":" + el.getString(KEY_NUMBER));
				System.out.println(KEY_NAME + ":" + el.getString(KEY_DATE_ADDED));				
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
