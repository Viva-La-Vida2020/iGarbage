package com.rubbishclassification.service;

import com.rubbishclassification.entity.City;
import com.rubbishclassification.entity.Rubbish;
import com.rubbishclassification.entity.Type;
import com.rubbishclassification.repository.RubbishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: garbageClassification
 * @description:
 * @author: ZXY
 * @create: 2022-02-17 22:01
 **/
@Service
public class RubbishService {
	
	@Autowired
	RubbishRepository rubbishRepository;
	
	
	public List<Rubbish> getRubbishesByTypeIDAndCityName(String c, String type, Integer page, Integer size) throws Exception {
		List<Rubbish> rubbishes = rubbishRepository.getRubbishesByTypesTypeAndTypesCityName(type, c, PageRequest.of(page - 1, size));
		return rubbishes;
	}
	
	public List<Rubbish> getRubbishesByNameLike(String name) {
		return rubbishRepository.getRubbishesByNameLike(name);
	}
	
	public Rubbish getRubbishByID(Long id) {
		return rubbishRepository.getById(id);
	}
}
