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
		long maxBlockTimestamp = 1543276800L;
		long minBlockTimestamp = 1543104000L;
		
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
		message.append("Rates (APR) as of block 6766843\n"); 
		
		for (MarketHistoryObject marketHistory : histories) {
			Token token = marketHistory.GetToken();
			
			String symbol = TokenUtils.GetSymbol(token);
			
			message.append("$");
			message.append(symbol);
			message.append(", ");
			
			float supplyRate = TokenUtils.GetHumanReadableRate(marketHistory.LatestSupplyRate());
			
			message.append("Supply ");			
			message.append(supplyRate);
			message.append("%");
			
			float borrowRate = TokenUtils.GetHumanReadableRate(marketHistory.LatestBorrowRate());
			
			message.append(" - Borrow ");
			message.append(borrowRate);
			message.append("%");
			
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
		
		CompoundAPIService.log.warning(message.toString());
	}
}