package com.rubbishclassification.repository;

import com.rubbishclassification.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {
	Optional<City> getCityByName(String name);
	
	@Query(value = "select * " +
			"from city left join city_records on city.id=city_records.city_id " +
			"where city_records.records_id = ?1", nativeQuery = true)
	Optional<City> findCityByTypeID(Long id);
}
