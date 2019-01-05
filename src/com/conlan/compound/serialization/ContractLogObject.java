package com.conlan.compound.serialization;

public class ContractLogObject {
	public String data;
	
	public ContractLogObject() {
		// no-args constructor
	}
	
	public LiquidationEventData ToLiquidationEventData() {
		LiquidationEventData liquidation = LiquidationEventData.Parse(data);
		
		return liquidation;
	}
}
