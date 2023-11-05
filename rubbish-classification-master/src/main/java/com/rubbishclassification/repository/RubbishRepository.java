package com.rubbishclassification.repository;

import com.rubbishclassification.entity.Rubbish;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * @program: garbageClassification
 * @description:
 * @author: ZXY
 * @create: 2022-02-17 21:11
 **/
public interface RubbishRepository extends JpaRepository<Rubbish, Long> {
	
	@Query(value = "select " +
			"*" +
			"    from rubbish rubbish0_" +
			"    left outer join type_rubbishes types1_ on rubbish0_.id=types1_.rubbishes_id " +
			"    left outer join `type` type2_ on types1_.types_id=type2_.id " +
			"    left outer join city city3_ on type2_.city_id=city3_.id " +
			"    where type2_.`type`=?1 and city3_.`name`=?2 ", nativeQuery = true)
	List<Rubbish> getRubbishesByTypesTypeAndTypesCityName(String type, String city, Pageable pageable);
	
	List<Rubbish> getRubbishesByNameLike(String name);

	Optional<Rubbish> getRubbishesByName(String name);

	Optional<Rubbish> getRubbishById(Long id);
}
