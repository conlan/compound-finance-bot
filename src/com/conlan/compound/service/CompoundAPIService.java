package com.conlan.compound.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import com.conlan.compound.TokenUtils;
import com.conlan.compound.TokenUtils.Token;
import com.conlan.compound.serialization.MarketHistoryObject;
import com.google.gson.Gson;

public class CompoundAPIService {
	public static final Logger log = Logger.getLogger(CompoundAPIService.class.getName());
	
	public static final String API_KEY = System.getProperty("COMPOUND_API_KEY");
	
	public static final String ENDPOINT_MARKET_HISTORY = "https://api.compound.finance/api/market_history/v1/graph"; 
	
	public static MarketHistoryObject GetHistory(Token token, long minBlockTimestamp, long maxBlockTimestamp) {
		// get the most recent interest rates from Compound
		StringBuilder urlBuilder = new StringBuilder();
		
		urlBuilder.append(ENDPOINT_MARKET_HISTORY);
		urlBuilder.append("?");
		
		urlBuilder.append("asset=");
		urlBuilder.append(TokenUtils.GetAddress(token));
		
		urlBuilder.append("&min_block_timestamp=");
		urlBuilder.append(minBlockTimestamp);

		urlBuilder.append("&max_block_timestamp=");
		urlBuilder.append(maxBlockTimestamp);
		
		urlBuilder.append("&num_buckets=1");
		
		MarketHistoryObject marketHistory = CompoundAPIService.Get(urlBuilder.toString(), MarketHistoryObject.class);
		
		if (marketHistory != null) {
			marketHistory.SetToken(token);
		}
		
		return marketHistory;
	}
	
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
