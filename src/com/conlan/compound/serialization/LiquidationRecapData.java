package com.conlan.compound.serialization;

import java.util.Hashtable;

import com.conlan.compound.TokenUtils.Token;

public class LiquidationRecapData {
	private Hashtable<Token, Double> amountsRepaid;
	private Hashtable<Token, Double> amountsSeized;
	
	public LiquidationRecapData(Hashtable<Token, Double> amountsRepaid, Hashtable<Token, Double> amountsSeized) {
		this.amountsRepaid = amountsRepaid;
		
		this.amountsSeized = amountsSeized;		
	}
	
	public Double GetAmountRepaid(Token t) {
		if (amountsRepaid.containsKey(t)) {
			return amountsRepaid.get(t);
		} else {
			return 0.0;
		}
	}
	
	public Double GetAmountSeized(Token t) {
		if (amountsSeized.containsKey(t)) {
			return amountsSeized.get(t);
		} else {
			return 0.0;
		}
	}
}