package com.rubbishclassification.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * @program: wastesort
 * @description:
 * @author: ZXY
 * @create: 2022-02-16 21:52
 **/
@Entity
public class City {
	
	@Id
	private Long id;
	private String name;
	private Integer count;
	
	@OneToMany
	private List<Type> records;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Integer getCount() {
		return count;
	}
	
	public void setCount(Integer count) {
		this.count = count;
	}
	
	public List<Type> getRecords() {
		return records;
	}
	
	public void setRecords(List<Type> records) {
		this.records = records;
	}
}
