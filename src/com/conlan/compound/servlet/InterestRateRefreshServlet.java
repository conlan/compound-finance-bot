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
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.conlan.compound.service.CompoundAPIService;

public class InterestRateRefreshServlet extends HttpServlet {
	private static final long serialVersionUID = -9181463126866704910L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// get the now time
		long unixNow = System.currentTimeMillis() / 1000L;
		
		// retrieve the past 4 hours
		int numHours = 4;
		
		// set max block time
		long maxBlockTimestamp = unixNow;
		
		// set min block time
		long minBlockTimestamp = unixNow - (60 * /*minute*/ 60 * /*hour*/ numHours);
		
		// retrieve market histories for all assets
		List<MarketHistoryObject> histories = new ArrayList<MarketHistoryObject>();
		
		for (Token token : Token.values()) {
			MarketHistoryObject marketHistory = CompoundAPIService.GetHistory(token, minBlockTimestamp, maxBlockTimestamp, numHours);
			
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
			
			double currentBorrowRate = TokenUtils.GetHumanReadableRate(marketHistory.LatestBorrowRate());
			
			double currentSupplyRate = TokenUtils.GetHumanReadableRate(marketHistory.LatestSupplyRate());
			double previousSupplyRate = TokenUtils.GetHumanReadableRate(marketHistory.EarliestSupplyRate());
			
			// insert rate decoration
			String tokenRateDecoration = TokenUtils.GetRateDecoration(currentSupplyRate, previousSupplyRate);
			if (tokenRateDecoration != null) {				
				message.append(tokenRateDecoration);
				message.append(" ");
			}
			
			// show symbol
			message.append("$");
			message.append(symbol);
			message.append(" - ");
			
			// show borrow rate						
			message.append("Borrow ");
			message.append(currentBorrowRate);
			message.append("%");
			
			// show supply rate
			message.append(" - Supply ");			
			message.append(currentSupplyRate);
			message.append("%");					

			// append newline if this isn't the last history
			if (marketHistory != histories.get(histories.size() - 1)) {
				message.append("\n");
			}
		}
		
		// insert this at the beginning of the message now that we know the latest block number
		message.insert(0, "Rates (APR) as of block " + latestBlock + ":\n\n");

		// Queue up a task to tweet this message
		Queue queue = QueueFactory.getDefaultQueue();		
		queue.add(TaskOptions.Builder.withUrl("/tweet").param("status", message.toString()));
	}
}
