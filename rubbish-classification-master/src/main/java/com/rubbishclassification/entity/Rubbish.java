package com.rubbishclassification.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;

/**
 * @program: wastesort
 * @description:
 * @author: ZXY
 * @create: 2022-01-25 18:09
 **/
@Entity
public class Rubbish {
	
	@Id
	private Long id;
	private String name;
	private Float wood;
	private Float glass;
	private Float iron;
	private Float aluminium;
	private Float plastic;
	
	@ManyToMany(mappedBy = "rubbishes")
	private List<Type> types;
	
	public Rubbish(String name, String city) {
		this.name = name;
	}
	
	
	public Rubbish() {
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Float getWood() {
		return wood;
	}
	
	public void setWood(Float wood) {
		this.wood = wood;
	}
	
	public Float getGlass() {
		return glass;
	}
	
	public void setGlass(Float glass) {
		this.glass = glass;
	}
	
	public Float getIron() {
		return iron;
	}
	
	public void setIron(Float iron) {
		this.iron = iron;
	}
	
	public Float getAluminium() {
		return aluminium;
	}
	
	public void setAluminium(Float aluminium) {
		this.aluminium = aluminium;
	}
	
	public Float getPlastic() {
		return plastic;
	}
	
	public void setPlastic(Float plastic) {
		this.plastic = plastic;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	@JsonIgnore
	public List<Type> getTypes() {
		return types;
	}
	
	public void setTypes(List<Type> types) {
		this.types = types;
	}
}
