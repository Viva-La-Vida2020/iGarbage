package com.rubbishclassification.utils;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Code {
	SUCCESS(1), FAIL(0);
	
	@JsonValue
	private int code;
	
	Code(int i) {
		code = i;
	}
	
	public int getCode() {
		return code;
	}
	
	public void setCode(int code) {
		this.code = code;
	}
}
