package com.conlan.compound.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Hex;

import com.conlan.compound.TokenUtils.Token;
import com.conlan.compound.serialization.ContractLogObject;
import com.conlan.compound.serialization.ContractLogsObject;
import com.conlan.compound.serialization.LiquidationEventData;
import com.conlan.compound.serialization.LiquidationRecapData;
import com.google.gson.Gson;

public class Web3Service {
	public static final String INFURA_TOKEN = System.getProperty("infura.token");
	
	public static final String COMPOUND_ADDRESS = System.getProperty("compound.address");
	public static final String COMPOUND_LIQUIDATION_TOPIC = System.getProperty("compound.liquidation.topic");
	
	public static final Logger log = Logger.getLogger(Web3Service.class.getName());
	
	public static LiquidationRecapData FetchLiquidationRecap(long lastFetchedBlock, long toBlock) {
		Hashtable<String, Object> paramsObject = new Hashtable<String, Object>();
		paramsObject.put("fromBlock", ToHex(lastFetchedBlock));
		paramsObject.put("toBlock", ToHex(toBlock));		
		paramsObject.put("address", COMPOUND_ADDRESS);
		
		ArrayList<String> topics = new ArrayList<String>();
		topics.add(COMPOUND_LIQUIDATION_TOPIC);
		
		paramsObject.put("topics", topics);
		
		ContractLogsObject contractLogs = Web3RPC("eth_getLogs", paramsObject, ContractLogsObject.class);
		
		LiquidationRecapData liquidationRecap = null;
		
		if (contractLogs != null) {
			Hashtable<Token, Double> amountsRepaid = new Hashtable<Token, Double>();
			Hashtable<Token, Double> amountsSeized = new Hashtable<Token, Double>();
			
			for (ContractLogObject logObject : contractLogs.result) {
				 LiquidationEventData liquidation = logObject.ToLiquidationEventData();
				
				 // update the amount repaid
				 if (amountsRepaid.containsKey(liquidation.assetBorrow) == false) {
					 amountsRepaid.put(liquidation.assetBorrow, new Double(0));
				 }
				 
				 amountsRepaid.put(liquidation.assetBorrow, amountsRepaid.get(liquidation.assetBorrow) + liquidation.amountRepaid);
				 
				 // update the amount seized
				 if (amountsSeized.containsKey(liquidation.assetCollateral) == false) {
					 amountsSeized.put(liquidation.assetCollateral, new Double(0));
				 }
				 
				 amountsSeized.put(liquidation.assetCollateral, amountsSeized.get(liquidation.assetCollateral) + liquidation.amountSeized);
			}
			
			liquidationRecap = new LiquidationRecapData(amountsRepaid, amountsSeized);
		}
		
		return liquidationRecap;
	}
	
	public static <T>T Web3RPC(String method, Hashtable<String, Object> paramsObject, Class<T> returnClass) {
		String url="https://mainnet.infura.io/v3/" + INFURA_TOKEN;
		
		Hashtable<String, Object> data = new Hashtable<String, Object>();
		data.put("jsonrpc", "2.0");		
		data.put("method", method);
		data.put("id", 1);
	
		ArrayList<Object> params = new ArrayList<Object>();
		data.put("params", params);
		
		if (paramsObject != null) {
			params.add(paramsObject);
		}
		
		return PostJSON(url, data, returnClass);
	}
	
	// TODO refactor this and compoundAPIService.Get into a separate service
	public static <T>T PostJSON(String url, Object jsonObj, Class<T> returnClass) {
		HttpURLConnection hc = null;
		
		String response = null;
		
		try {			
			URL object=new URL(url);
			
			hc = (HttpURLConnection) object.openConnection();
			hc.setDoOutput(true);
			hc.setDoInput(true);
			hc.setRequestProperty("Content-Type", "application/json");
			hc.setRequestProperty("Accept", "application/json");
			hc.setRequestMethod("POST");
			
			Gson gson = new Gson();	
			
			log.info(gson.toJson(jsonObj));

			OutputStreamWriter wr = new OutputStreamWriter(hc.getOutputStream());
			wr.write(gson.toJson(jsonObj));
			wr.flush();

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
				T responseObject = gson.fromJson(response, returnClass);
				
				return responseObject;
			}	
		} 
		catch (Throwable t) {
			t.printStackTrace();
		} 
		finally {
			if (hc != null) {
				hc.disconnect();
			}
		}
		return null;
	}
	
	public static String ToHex(long number) {
		BigInteger numericValue = new BigInteger("" + number);
		
		byte[] bytes = numericValue.toByteArray();
		
		return "0x" + Hex.encodeHexString(bytes);
	}
	
	public static BigDecimal ToNumeric(String hex) {		
		try {
			byte[] bytes = Hex.decodeHex(hex.toCharArray());
			
			BigInteger numericValue = new BigInteger(bytes);
			
			return new BigDecimal(numericValue);
		} catch (Exception e) {
			e.printStackTrace();
			return new BigDecimal("0");
		}
	}
	
	public static String ToAddress(String hex) {		
		try {
			byte[] bytes = Hex.decodeHex(hex.toCharArray());
			
			BigInteger numericValue = new BigInteger(bytes);
			
			StringBuilder address = new StringBuilder(numericValue.toString(16));
			
			int addressLength = address.length();
			// zero pad the address
			while (addressLength < 40) {
				address.insert(0, "0");
				
				addressLength++;
			}			

			address.insert(0, "0x");
			
			return address.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
