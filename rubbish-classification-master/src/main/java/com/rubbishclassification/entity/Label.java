package com.rubbishclassification.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @program: RubbishClassification
 * @description:
 * @author: ZXY
 * @create: 2022-03-28 01:00
 **/
@Entity
public class Label {
	
	@Id
	private Long id;
	private String name;
	private String type;
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	@Id
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
}
