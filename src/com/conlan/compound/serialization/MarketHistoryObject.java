package com.conlan.compound.serialization;

import java.util.ArrayList;

public class MarketHistoryObject {
	public ArrayList<ValueObject> total_supply_history;
	public ArrayList<ValueObject> total_borrows_history;
	private ArrayList<RateObject> supply_rates;
	private ArrayList<RateObject> borrow_rates;
	
	public Object error;	
	
	public String asset;
	
	public MarketHistoryObject() {
		// no-args constructor
	}
	
	public float LatestSupplyRate() {
		RateObject latestSupply = (supply_rates.size() > 0) ? supply_rates.get(supply_rates.size() - 1) : null; 
		
		if (latestSupply == null) {
			return 0.0f;
		} else {
			return latestSupply.GetRate();
		}
	}
	
	public float LatestBorrowRate() {
		RateObject latestBorrow = (borrow_rates.size() > 0) ? borrow_rates.get(borrow_rates.size() - 1) : null; 
		
		if (latestBorrow == null) {
			return 0.0f;
		} else {
			return latestBorrow.GetRate();
		}
	}
}
