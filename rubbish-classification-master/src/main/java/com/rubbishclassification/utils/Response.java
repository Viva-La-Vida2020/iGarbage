package com.rubbishclassification.utils;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @program: wastesort
 * @description:
 * @author: ZXY
 * @create: 2022-02-06 21:24
 **/
public class Response {

	private Code code;
	private String message;
	private Object data;
	
	public Response(Code code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public Response(Code code, String message, Object data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}
	
	public static Response fail(String message) {
		Response response = new Response(Code.FAIL, message);
		return response;
	}
	
	public static Response success(String message, Object data) {
		Response response = new Response(Code.SUCCESS, message, data);
		return response;
	}
	
	public Code getCode() {
		return code;
	}
	
	public void setCode(Code code) {
		this.code = code;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public Object getData() {
		return data;
	}
	
	public void setData(Object data) {
		this.data = data;
	}
}
