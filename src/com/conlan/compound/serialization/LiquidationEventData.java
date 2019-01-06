package com.conlan.compound.serialization;

import java.util.ArrayList;

import com.conlan.compound.TokenUtils;
import com.conlan.compound.TokenUtils.Token;
import com.conlan.compound.service.Web3Service;


public class LiquidationEventData {
	public Token assetBorrow;
	public Double amountRepaid;
	
	public Token assetCollateral;
	public Double amountSeized;
	
	public static LiquidationEventData Parse(String eventData) {
		LiquidationEventData liquidation = new LiquidationEventData();
		
		int paramLength = 64;
		
		ArrayList<String> params = new ArrayList<String>();

		// offset by initial "0x"
		for (int i = 2; i < eventData.length(); i+=paramLength) {
			String param = eventData.substring(i, i + paramLength);
			
			params.add(param);			
		}
		
		liquidation.assetBorrow = TokenUtils.GetToken(Web3Service.ToAddress(params.get(1)));
		
		int decimals = TokenUtils.GetDecimals(liquidation.assetBorrow);		
		liquidation.amountRepaid = new Double(Web3Service.ToNumeric(params.get(4)) / Math.pow(10, decimals));
		
		liquidation.assetCollateral = TokenUtils.GetToken(Web3Service.ToAddress(params.get(7)));
		
		decimals = TokenUtils.GetDecimals(liquidation.assetCollateral);
		liquidation.amountSeized = new Double(Web3Service.ToNumeric(params.get(10)) / Math.pow(10, decimals));
		
		return liquidation;
	}
}
