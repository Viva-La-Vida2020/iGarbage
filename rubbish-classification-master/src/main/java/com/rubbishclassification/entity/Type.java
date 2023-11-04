package com.rubbishclassification.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

/**
 * @program: wastesort
 * @description:
 * @author: ZXY
 * @create: 2022-02-10 22:41
 **/
@Entity
public class Type {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String type;
	private String intro;
	private String des;
	private Integer total;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private City city;
	@ManyToMany
	private List<Rubbish> rubbishes;
	
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getIntro() {
		return intro;
	}
	
	public void setIntro(String intro) {
		this.intro = intro;
	}
	
	public String getDes() {
		return des;
	}
	
	public void setDes(String des) {
		this.des = des;
	}
	
	public Integer getTotal() {
		return total;
	}
	
	public void setTotal(Integer total) {
		this.total = total;
	}
	
	@JsonIgnore
	public City getCity() {
		return city;
	}
	
	public void setCity(City city) {
		this.city = city;
	}
	
	@JsonIgnore
	public List<Rubbish> getRubbishes() {
		return rubbishes;
	}
	
	public void setRubbishes(List<Rubbish> rubbishes) {
		this.rubbishes = rubbishes;
	}
}
