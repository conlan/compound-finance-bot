package com.conlan.compound.servlet;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.conlan.compound.TokenUtils;
import com.conlan.compound.TokenUtils.Token;
import com.conlan.compound.serialization.CurrentBlockObject;
import com.conlan.compound.serialization.LiquidationRecapData;
import com.conlan.compound.service.Web3Service;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

public class ReportLiquidationServlet extends HttpServlet {
	public static final Logger log = Logger.getLogger(ReportLiquidationServlet.class.getName());
	
	private static final long serialVersionUID = -6486835234713150146L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		// load the block that we last fetched up to
		Query q = new Query("block_info");
		
		List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
		
		if (results.size() == 0) {
//			Entity tom = new Entity("block_info");
//			tom.setUnindexedProperty("last_fetched_block", 7014797);
//			datastore.put(tom);
//			
			Web3Service.log.warning("No block infos found! Bailing.");
			return;
		}
		
		Entity blockInfo = results.get(0);
		
		long lastFetchedBlock = Long.valueOf(blockInfo.getProperty("last_fetched_block").toString());
		
		// query the current block number
		CurrentBlockObject currentBlockObj = Web3Service.Web3RPC("eth_blockNumber", null, CurrentBlockObject.class);
		
		long currentBlock = currentBlockObj.Block();
		
		// update datastore with this for the subsequent /reportliquidations call (incrementing by 1 to avoid a duplicate block between calls)
		blockInfo.setUnindexedProperty("last_fetched_block", currentBlock + 1);		
		datastore.put(blockInfo);
		
		log.info("Updated last_feteched_block to " + (currentBlock + 1));

		// fetch the liquidation recap from the last fetched block
		LiquidationRecapData liquidationRecap = Web3Service.FetchLiquidationRecap(lastFetchedBlock, currentBlock);
		
		StringBuilder sb = new StringBuilder();
		sb.append("💀 Liquidation stats from block (");		
		sb.append(lastFetchedBlock);
		sb.append(" -> ");
		sb.append(currentBlock);
		sb.append(")\n\n");
		
		boolean shouldTweet = false;
		
		for (Token t : Token.values()) {
			double amountRepaid = liquidationRecap.GetAmountRepaid(t);			
			
			double amountSeized = liquidationRecap.GetAmountSeized(t);
			
			if ((amountRepaid > 0) || (amountSeized > 0)) {				
				sb.append("$");
				sb.append(TokenUtils.GetSymbol(t));
				
				if (amountRepaid > 0) {
					sb.append(" 💸 ");
					sb.append(TokenUtils.GetHumanReadableTokenValue(amountRepaid));
					sb.append(" Repaid");
				}
				
				if (amountSeized > 0) {
					sb.append(" ⚡️ ");
					sb.append(TokenUtils.GetHumanReadableTokenValue(amountSeized));
					sb.append(" Seized");
				}
				
				sb.append("\n");
				
				shouldTweet = true;
			}
		}
		
		if (shouldTweet) {
			// Queue up a task to tweet this message
			Queue queue = QueueFactory.getDefaultQueue();		
			queue.add(TaskOptions.Builder.withUrl("/tweet").param("status", sb.toString()));
		}
	}
}
