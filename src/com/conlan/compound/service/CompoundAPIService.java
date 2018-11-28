package com.conlan.compound.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import com.google.gson.Gson;

public class CompoundAPIService {
	public static final Logger log = Logger.getLogger(MarketHistoryService.class.getName());
	
	public static final String API_KEY = System.getProperty("COMPOUND_API_KEY");
	
	public static <T>T Get(String url, Class<T> returnClass) {		
		HttpURLConnection hc = null;
		
		String response = null;
		
		try {
			URL address = new URL(url);
			hc = (HttpURLConnection) address.openConnection();
			
			hc.setRequestMethod("GET");
			hc.setRequestProperty("compound-api-key", API_KEY);
			hc.setRequestProperty("Accept", "application/json");
			hc.setRequestProperty("User-Agent", "Mozilla/4.0"); // override the user agent to avoid blacklist
			
			int responseCode = hc.getResponseCode();
			
			log.info(responseCode + " " + hc.toString());
			
			BufferedReader in = new BufferedReader(new InputStreamReader(hc.getInputStream()));
			
			String inputLine;
			
			StringBuffer sb = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				sb.append(inputLine);
			}
			
			in.close();
			
			response = sb.toString();
			
			log.info(response);
			
			if (responseCode == HttpURLConnection.HTTP_OK) {			
				Gson gson = new Gson();
				
				T responseObject = gson.fromJson(response, returnClass);
				
				return responseObject;
			}
		} catch (Throwable t) {
			log.warning(t.toString());
		} finally {
			if (hc != null) {
				hc.disconnect();
			}
		}
		
		return null;
	}
}
