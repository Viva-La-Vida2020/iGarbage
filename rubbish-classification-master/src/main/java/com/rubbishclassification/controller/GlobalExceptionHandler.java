package com.rubbishclassification.controller;

import com.rubbishclassification.utils.Response;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @program: wastesort
 * @description:
 * @author: ZXY
 * @create: 2022-02-06 21:43
 **/
@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(value = Exception.class)
	public Response exceptionHandle(Exception e) {
		return Response.fail(e.getMessage());
	}

}
