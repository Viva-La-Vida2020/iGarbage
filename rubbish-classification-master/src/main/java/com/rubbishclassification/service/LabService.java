package com.rubbishclassification.service;

import com.rubbishclassification.entity.Label;
import com.rubbishclassification.repository.LabelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @program: RubbishClassification
 * @description:
 * @author: ZXY
 * @create: 2022-03-28 01:03
 **/
@Service
public class LabService {
	
	@Autowired
	LabelRepository labelRepository;
	
	public Optional<Label> getLabel(long id) {
		return labelRepository.findLabelById(id);
	}
}
