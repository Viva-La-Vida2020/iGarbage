package com.rubbishclassification.controller;

import com.rubbishclassification.entity.City;
import com.rubbishclassification.entity.Rubbish;
import com.rubbishclassification.service.CityService;
import com.rubbishclassification.service.RubbishService;
import com.rubbishclassification.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @program: garbageClassification
 * @description:
 * @author: ZXY
 * @create: 2022-02-17 21:06
 **/
@RestController
@RequestMapping("/api/v1")
public class TypeInfoController {
	
	@Autowired
	CityService cityService;
	@Autowired
	RubbishService rubbishService;
	
	@GetMapping("/list")
	public Response getRubbishListByTypeAndCity(String city, String type, @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "1") Integer page) throws Exception {
		
		if (page <= 0 || pageSize <= 0 || pageSize > 50) {
			throw new Exception("参数不合法");
		}
		
		if (city == null || type == null) {
			throw new Exception("参数不存在");
		}
		
		List<Rubbish> rubbishes = rubbishService.getRubbishesByTypeIDAndCityName(city, type, page, pageSize);
		
		if (rubbishes.isEmpty()) {
			throw new Exception("查询失败");
		}
		
		return Response.success("查询成功", rubbishes);
		
	}
	
	
}
