package com.rubbishclassification.service;

import com.rubbishclassification.entity.City;
import com.rubbishclassification.entity.Type;
import com.rubbishclassification.repository.CityRepository;
import com.rubbishclassification.repository.TypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @program: wastesort
 * @description:
 * @author: ZXY
 * @create: 2022-02-16 21:57
 **/
@Service
public class CityService {
	
	@Autowired
	TypeRepository typeRepository;
	@Autowired
	CityRepository cityRepository;
	
	
	public City getCityTypesByName(String name) {
		
		Optional<City> optionalCity = cityRepository.getCityByName(name);
		List<Type> types = typeRepository.findTypesByCityName(name);
		
		if (!optionalCity.isPresent() || types.isEmpty()) return null;
		
		City city = optionalCity.get();
		city.setRecords(types);
		
		return city;
	}
}
