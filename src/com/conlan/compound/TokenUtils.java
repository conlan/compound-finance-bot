package com.conlan.compound;

import com.conlan.compound.service.CompoundAPIService;

public class TokenUtils {
	public static final double HOT_SUPPLY_RATE_THRESHOLD = 5d; // 5%
	public static final double UPDATED_SUPPLY_RATE_THRESHOLD = 0.01d; // 0.01%
	
	public enum Token {
		WETH,
		DAI,
		BAT,
		ZRX,
		REP,		
	}
	
	public static String GetAddress(Token token) {
		switch (token) {
			case WETH: return "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2";
			case BAT: return "0x0D8775F648430679A709E98d2b0Cb6250d2887EF";
			case ZRX: return "0xE41d2489571d322189246DaFA5ebDe1F4699F498";
			case REP: return "0x1985365e9f78359a9B6AD760e32412f4a445E862";
			case DAI: return "0x89d24A6b4CcB1B6fAA2625fE562bDD9a23260359";
		}
		return null;
	}
	
	public static String GetSymbol(Token token) {
		switch (token) {
			case WETH: return "WETH";
			case BAT: return "BAT";
			case ZRX: return "ZRX";
			case REP: return "REP";
			case DAI: return "DAI";
		}
		return null;		
	}
	
	public static double GetHumanReadableRate(double rawRate) {
		rawRate *= 100 * 10 * 10;
		
		rawRate = Math.round(rawRate);
		
		rawRate /= 100f;
		
		return rawRate;
	}
	
	// decorate the rate with a fun emoji
	public static String GetRateDecoration(double currentSupplyRate, double previousSupplyRate) {
		// TODO take previous supply rate into account
		if (currentSupplyRate <= 0) { // no point in supplying capital
			return "😵";
		} else if (currentSupplyRate >= HOT_SUPPLY_RATE_THRESHOLD) { // good rates!
			return "🔥";
		} else {
			// check if we have a previous supply rate
			if (previousSupplyRate != 0) {
				// we do, so find how much the rate changed
				double rateChange = currentSupplyRate - previousSupplyRate;
				// find absolute delta
				double delta = Math.abs(rateChange);
				
				CompoundAPIService.log.warning("current rate = " + currentSupplyRate + ", previous = " + previousSupplyRate +
						" change = " + rateChange + "  , delta = " + delta + "  " + UPDATED_SUPPLY_RATE_THRESHOLD);
				// if delta is big enough, use a chart decoration
				if (delta >= UPDATED_SUPPLY_RATE_THRESHOLD) {
					if (rateChange > 0) {
						// positive rate change
						return "📈";
					} else {
						// negative rate change
						return "📉";
					}
				}
			}
			
			// no decoration, just use a space filler so at least all symbols are lined up
			return "▪️";
		} 
	}
}