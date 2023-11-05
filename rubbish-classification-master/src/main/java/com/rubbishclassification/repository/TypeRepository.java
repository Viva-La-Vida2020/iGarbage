package com.rubbishclassification.repository;

import com.rubbishclassification.entity.Type;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TypeRepository extends JpaRepository<Type, Long> {

	Optional<Type> findByRubbishesIdAndCityName(Long id, String city);
	
	List<Type> findTypesByCityName(String city);
}
