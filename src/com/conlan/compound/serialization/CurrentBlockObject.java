package com.conlan.compound.serialization;

import com.conlan.compound.service.Web3Service;

public class CurrentBlockObject {
	public String result;
	
	private long block_ = -1;
	
	public CurrentBlockObject() {
		// no-args constructor
	}
	
	public long Block() {
		if (block_ == -1) {
			block_ = Web3Service.ToNumeric(result.substring(2)).longValue(); // offset for the initial "0x"
		}
		
		return block_;
	}
}
