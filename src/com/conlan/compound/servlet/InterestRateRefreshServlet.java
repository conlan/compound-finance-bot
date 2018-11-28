package com.conlan.compound.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.conlan.compound.serialization.MarketHistoryObject;
import com.conlan.compound.service.TokenUtils.Token;
import com.conlan.compound.service.CompoundAPIService;
import com.conlan.compound.service.MarketHistoryService;
import com.conlan.compound.service.TokenUtils;

public class InterestRateRefreshServlet extends HttpServlet {
	private static final long serialVersionUID = -9181463126866704910L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {		
		long unixNow = System.currentTimeMillis() / 1000L;
		
		long maxBlockTimestamp = unixNow;
		
		long minBlockTimestamp = unixNow - (60 * /*minute */ 60/* hour */); // 1 hour ago
		
		List<MarketHistoryObject> histories = new ArrayList<MarketHistoryObject>();
		
		for (Token token : Token.values()) {
			MarketHistoryObject marketHistory = MarketHistoryService.GetHistory(token, minBlockTimestamp, maxBlockTimestamp);
			
			if (marketHistory != null) {
				histories.add(marketHistory);
			} else {
				return;
			}			
		}
		
		// only proceed if we successfully retrieved all asset rates
		
		StringBuilder message = new StringBuilder();

		// track the latest block value amongst all the market history objects
		long latestBlock = Long.MIN_VALUE;
		
		for (MarketHistoryObject marketHistory : histories) {
			// check block number
			long block = marketHistory.LatestBlockNumber();
			if (block > latestBlock) {
				latestBlock = block;
			}
			
			// get token and symbol
			Token token = marketHistory.GetToken();
			
			String symbol = TokenUtils.GetSymbol(token);
			
			// show symbol
			message.append("$");
			message.append(symbol);
			message.append(" - ");
			
			// show borrow rate
			float borrowRate = TokenUtils.GetHumanReadableRate(marketHistory.LatestBorrowRate());
			
			message.append("Borrow ");
			message.append(borrowRate);
			message.append("%");
			
			// show supply rate
			float supplyRate = TokenUtils.GetHumanReadableRate(marketHistory.LatestSupplyRate());
			
			message.append(" - Supply ");			
			message.append(supplyRate);
			message.append("%");					
			
			// add optional rate decoration
			String tokenRateDecoration = TokenUtils.GetRateDecoration(supplyRate, borrowRate);
			if (tokenRateDecoration != null) {
				message.append(" ");
				message.append(tokenRateDecoration);
			}
			
			// append newline if this isn't the last history
			if (marketHistory != histories.get(histories.size() - 1)) {
				message.append("\n");
			}
		}
		
		// insert this at the beginning of the message now that we know the latest block number
		message.insert(0, "Rates (APR) as of block " + latestBlock + ":\n"); 
		
		CompoundAPIService.log.warning(message.toString());
	}
}
