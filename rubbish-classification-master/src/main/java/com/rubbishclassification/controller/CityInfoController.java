package com.rubbishclassification.controller;

import com.rubbishclassification.entity.City;
import com.rubbishclassification.service.CityService;
import com.rubbishclassification.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * @program: wastesort
 * @description:
 * @author: ZXY
 * @create: 2022-02-06 21:37
 **/
@RestController
public class CityInfoController {
	
	@Autowired
	CityService cityService;
	
	@GetMapping("/api/v1/info")
	public Response getCityTypeInfo(String city, HttpSession session) throws Exception {
		
		if (city == null) {
			throw new Exception("城市名为空");
		}
		
		City c = cityService.getCityTypesByName(city);
		
		if (c == null||c.getRecords().isEmpty()) {
			throw new Exception("当前城市未在46个试点城市之内");
		}
		
		return Response.success("查询成功", c);
		
		
	}
	
	
}
