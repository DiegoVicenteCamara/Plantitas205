package com.plantitas.repository;

import com.plantitas.model.Plant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantRepository extends JpaRepository<Plant, Long> {

	@Override
	Optional<Plant> findById(Long id);

	Optional<Plant> findBySlugIgnoreCase(String slug);

	List<Plant> findTop10ByCommonNameContainingIgnoreCaseOrderByCommonNameAsc(String name);

	List<Plant> findTop10ByCommonNameContainingIgnoreCaseOrScientificNameContainingIgnoreCaseOrderByCommonNameAsc(
		String commonName,
		String scientificName
	);

	List<Plant> findTop10ByCommonNameStartingWithIgnoreCaseOrderByCommonNameAsc(String prefix);

	List<Plant> findByCommonNameContainingIgnoreCaseOrScientificNameContainingIgnoreCase(
		String commonNameQuery,
		String scientificNameQuery
	);
}
