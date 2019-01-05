package com.conlan.compound.serialization;

import java.util.ArrayList;

import com.conlan.compound.service.Web3Service;


public class LiquidationEventData {
	public String assetBorrow;
	public Long amountRepaid;
	
	public String assetCollateral;
	public Long amountSeized;
	
	public static LiquidationEventData Parse(String eventData) {
		LiquidationEventData liquidation = new LiquidationEventData();
		
		int paramLength = 64;
		
		ArrayList<String> params = new ArrayList<String>();

		// offset by initial "0x"
		for (int i = 2; i < eventData.length(); i+=paramLength) {
			String param = eventData.substring(i, i + paramLength);
			
			params.add(param);			
		}
		
		liquidation.assetBorrow = Web3Service.ToAddress(params.get(1));
		
		liquidation.amountRepaid = Web3Service.ToNumeric(params.get(4));
		
		liquidation.assetCollateral = Web3Service.ToAddress(params.get(7));
		
		liquidation.amountSeized = Web3Service.ToNumeric(params.get(10));
		
		return liquidation;
	}
}
