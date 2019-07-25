package com.conlan.compound.serialization;

import java.math.BigDecimal;
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
		
		// borrow
		liquidation.assetBorrow = TokenUtils.GetToken(Web3Service.ToAddress(params.get(1)));
		
		BigDecimal decimals = new BigDecimal("10").pow(TokenUtils.GetDecimals(liquidation.assetBorrow));
		liquidation.amountRepaid = Web3Service.ToNumeric(params.get(4)).divide(decimals).doubleValue();
		
		// collateral
		liquidation.assetCollateral = TokenUtils.GetToken(Web3Service.ToAddress(params.get(7)));
		
		decimals = new BigDecimal("10").pow(TokenUtils.GetDecimals(liquidation.assetCollateral));
		liquidation.amountSeized = Web3Service.ToNumeric(params.get(10)).divide(decimals).doubleValue();
		
		
		return liquidation;
	}
}
