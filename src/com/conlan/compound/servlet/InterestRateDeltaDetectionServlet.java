package com.conlan.compound.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.conlan.compound.TokenUtils;
import com.conlan.compound.TokenUtils.Token;
import com.conlan.compound.serialization.MarketHistoryObject;
import com.conlan.compound.service.CompoundAPIService;

/*
 * Poll for significant deltas in interest rate and tweet them.
 */
public class InterestRateDeltaDetectionServlet extends HttpServlet {
	private static final long serialVersionUID = -3110782275544909954L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {		
		// retrieve the past 2 hours
		int numHours = 2;
		
		// retrieve market histories for all assets
		List<MarketHistoryObject> histories = new ArrayList<MarketHistoryObject>();
		
		for (Token token : Token.values()) {
			MarketHistoryObject marketHistory = CompoundAPIService.GetAssetMarketHistory(token, numHours);
			
			if (marketHistory != null) {
				histories.add(marketHistory);
			} else {
				return;
			}
		}
		
		// only proceed if we successfully retrieved all asset rates		
		StringBuilder message = new StringBuilder("🚨🚨 Recent rate (APR) alert 🚨🚨\n");
		
		boolean atLeastOneMarketQualifiedForAlert = false;
		
		for (MarketHistoryObject marketHistory : histories) {			
			// check if this market history qualifies for an alert			
			double earliestSupplyRate = TokenUtils.GetHumanReadableRate(marketHistory.EarliestSupplyRate());
			double currentSupplyRate = TokenUtils.GetHumanReadableRate(marketHistory.LatestSupplyRate());	
			
			// get token and symbol
			Token token = marketHistory.GetToken();			
			String symbol = TokenUtils.GetSymbol(token);
			
			double rateChangePercentage = TokenUtils.GetRatePercentageChange(currentSupplyRate, earliestSupplyRate);
			int humanReadableRateChangePercentage = TokenUtils.GetHumanReadableRateChangePercentage(rateChangePercentage);
			
			CompoundAPIService.log.info(token + " " + earliestSupplyRate + " -> " + currentSupplyRate + " = " + humanReadableRateChangePercentage + "%");
			
			if (TokenUtils.DoesRateChangeQualifyForAlert(currentSupplyRate, earliestSupplyRate)) {
				atLeastOneMarketQualifiedForAlert = true;									
				
				if (humanReadableRateChangePercentage > 0) {
					message.append("📈 ");
				} else {
					message.append("📉 ");
				}
				
				message.append("$");
				message.append(symbol);
				message.append(" Supply rate has ");
				
				if (humanReadableRateChangePercentage > 0) {
					message.append("gone up by ");	
				} else {
					message.append("dropped ");
				}
				
				message.append(humanReadableRateChangePercentage);
				message.append("%");
				if (humanReadableRateChangePercentage > 0) {
					message.append("!");
				} else {
					message.append("...");
				}
			}			
		}
		
		message.append("\nCompound your crypto at compound.finance");
		
		CompoundAPIService.log.info(message.toString());
		
		if (atLeastOneMarketQualifiedForAlert) {
			// TODO tweet
			CompoundAPIService.log.warning("WOULD TWEET");
		}
	}
}
