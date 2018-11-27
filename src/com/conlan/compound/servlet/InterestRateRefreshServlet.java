package com.conlan.compound.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.conlan.compound.Constants;
import com.conlan.compound.service.MarketHistoryService;

public class InterestRateRefreshServlet extends HttpServlet {
	private static final long serialVersionUID = -9181463126866704910L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		long maxBlockTimestamp = 1543276800L;
		long minBlockTimestamp = 1543104000L;
		
		for (String asset : Constants.ALL_ASSETS) {
			MarketHistoryService.GetHistory(asset, minBlockTimestamp, maxBlockTimestamp);
			break;
		}		
	}
}
