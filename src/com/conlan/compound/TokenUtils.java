package com.conlan.compound;

import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Locale;

import com.conlan.compound.service.CompoundAPIService;

public class TokenUtils {
	// threshold for whether to show a fire lit
	public static final double HOT_SUPPLY_RATE_THRESHOLD = 5d; // 5%
	// threshold for whether to show an up/down chart
	public static final double UPDATED_SUPPLY_RATE_THRESHOLD = 0.01d; // 0.01%
	// the percentage change for a supply rate to qualify for an alert
	public static final double SUPPLY_RATE_ALERT_THRESHOLD = 0.20d; // 20% chance needed for an alert
	
	public static Hashtable<Token, String> tokenSymbolMap = new Hashtable<Token,String>();
	public static Hashtable<Token, Integer> tokenDecimalMap = new Hashtable<Token, Integer>();
	
	static {
		tokenSymbolMap.put(Token.WETH, "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2".toLowerCase());
		tokenDecimalMap.put(Token.WETH, 18);
		
		tokenSymbolMap.put(Token.BAT, "0x0D8775F648430679A709E98d2b0Cb6250d2887EF".toLowerCase());
		tokenDecimalMap.put(Token.BAT, 18);
		
		tokenSymbolMap.put(Token.ZRX, "0xE41d2489571d322189246DaFA5ebDe1F4699F498".toLowerCase());
		tokenDecimalMap.put(Token.ZRX, 18);
		
		tokenSymbolMap.put(Token.REP, "0x1985365e9f78359a9B6AD760e32412f4a445E862".toLowerCase());
		tokenDecimalMap.put(Token.REP, 18);
		
		tokenSymbolMap.put(Token.DAI, "0x89d24A6b4CcB1B6fAA2625fE562bDD9a23260359".toLowerCase());
		tokenDecimalMap.put(Token.DAI, 18);
	}
	
	public enum Token {
		WETH,
		DAI,
		BAT,
		ZRX,
		REP,		
	}
	
	public static Token GetToken(String address) {
		address = address.toLowerCase();
		
		for (Token t : Token.values()) {
			if (GetAddress(t).equals(address)) {
				return t;
			}
		}
		return Token.WETH;
	}
	
	public static String GetAddress(Token token) {
		return tokenSymbolMap.get(token);		
	}
	
	public static int GetDecimals(Token token) {
		return tokenDecimalMap.get(token);
	}
	
	public static String GetSymbol(Token token) {
		return token.toString().toUpperCase();
	}
	
	public static int GetHumanReadableRateChangePercentage(double rawRateChangePercentage) {
		int percentage = (int) (rawRateChangePercentage * 100);
		
		return percentage;
	}
	
	public static String GetHumanReadableTokenValue(double value) {
		return NumberFormat.getNumberInstance(Locale.US).format(value);
	}
	
	public static double GetHumanReadableRate(double rawRate) {
		rawRate *= 100 * 10 * 10;
		
		rawRate = Math.round(rawRate);
		
		rawRate /= 100f;
		
		return rawRate;
	}
	
	public static boolean DoesRateChangeQualifyForAlert(double currentSupplyRate, double previousSupplyRate) {
		double rateChangePercentage = GetRatePercentageChange(currentSupplyRate, previousSupplyRate);
		
		if (Math.abs(rateChangePercentage) >= SUPPLY_RATE_ALERT_THRESHOLD) {
			return true;
		}
		
		return false;
	}
	
	public static double GetRatePercentageChange (double currentSupplyRate, double previousSupplyRate) {
		if (previousSupplyRate == 0) {
			return 0;
		}
		
		double rateChangePercentage = (currentSupplyRate / previousSupplyRate) - 1.0;
		
		return rateChangePercentage;
	}
	
	// decorate the rate with a fun emoji
	public static String GetRateDecoration(double currentSupplyRate, double previousSupplyRate) {
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