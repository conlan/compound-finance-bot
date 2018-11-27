package com.conlan.compound.serialization;

public class RateObject {
	private float rate;
	private long block_timestamp;
	private long block_number;
	
	public RateObject() {
		// no-args constructor
	}
	
	public long GetBlockNumber() {
		return block_number;	
	}
	
	public float GetRate() {
		return rate;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("RateObject (");
		sb.append("Rate = ");
		sb.append(rate);
		sb.append(", timestamp = ");
		sb.append(block_timestamp);
		sb.append(", number = ");
		sb.append(block_number);
		sb.append(")");
		
		return sb.toString();
	}
}
