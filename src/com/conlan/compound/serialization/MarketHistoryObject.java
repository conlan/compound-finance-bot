package com.conlan.compound.serialization;

import java.util.ArrayList;

import com.conlan.compound.TokenUtils.Token;

public class MarketHistoryObject {
	public ArrayList<ValueObject> total_supply_history;
	public ArrayList<ValueObject> total_borrows_history;
	private ArrayList<RateObject> supply_rates;
	private ArrayList<RateObject> borrow_rates;
	
	public Object error;	
	
	public String asset;
	
	private Token token;
	
	public MarketHistoryObject() {
		// no-args constructor
	}
	
	public Token GetToken() {
		return token;
	}
	
	public void SetToken(Token token) {
		this.token = token;
	}
	
	public long LatestBlockNumber() {
		long supplyHistoryBlock = (total_supply_history.size() > 0) ? total_supply_history.get(total_supply_history.size() - 1).block_number : 0L;
		long supplyRateBlock = (supply_rates.size() > 0) ? supply_rates.get(supply_rates.size() - 1).GetBlockNumber() : 0L;
		
		long borrowHistoryBlock = (total_borrows_history.size() > 0) ? total_borrows_history.get(total_borrows_history.size() - 1).block_number : 0L;
		long borrowRateBlock = (borrow_rates.size() > 0) ? borrow_rates.get(borrow_rates.size() - 1).GetBlockNumber() : 0L;
		
		// return highest block number
		return Math.max(supplyHistoryBlock, Math.max(supplyRateBlock, Math.max(borrowHistoryBlock, borrowRateBlock))); 
	}
	
	public double LatestSupplyRate() {
		RateObject latestSupply = (supply_rates.size() > 0) ? supply_rates.get(supply_rates.size() - 1) : null; 
		
		if (latestSupply == null) {
			return 0.0f;
		} else {
			return latestSupply.GetRate();
		}
	}
	
	public double LatestBorrowRate() {
		RateObject latestBorrow = (borrow_rates.size() > 0) ? borrow_rates.get(borrow_rates.size() - 1) : null; 
		
		if (latestBorrow == null) {
			return 0.0f;
		} else {
			return latestBorrow.GetRate();
		}
	}
}
