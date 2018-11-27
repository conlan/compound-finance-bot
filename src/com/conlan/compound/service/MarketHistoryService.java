package com.conlan.compound.service;

import com.conlan.compound.serialization.MarketHistoryObject;

public class MarketHistoryService {
	public static final String ENDPOINT_MARKET_HISTORY = "https://api.compound.finance/api/market_history/v1/graph"; 
	
	public static MarketHistoryObject GetHistory(String asset, long minBlockTimestamp, long maxBlockTimestamp) {
		// get the most recent interest rates from Compound
		StringBuilder urlBuilder = new StringBuilder();
		
		urlBuilder.append(ENDPOINT_MARKET_HISTORY);
		urlBuilder.append("?");
		
		urlBuilder.append("asset=");
		urlBuilder.append(asset);
		
		urlBuilder.append("&min_block_timestamp=");
		urlBuilder.append(minBlockTimestamp);

		urlBuilder.append("&max_block_timestamp=");
		urlBuilder.append(maxBlockTimestamp);
		
		urlBuilder.append("&num_buckets=1");
		
		return CompoundService.Get(urlBuilder.toString(), MarketHistoryObject.class);
	}
}
