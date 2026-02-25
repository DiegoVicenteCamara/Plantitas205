package com.plantitas.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.plantitas.model.Plant;
import com.plantitas.model.PlantCategory;
import com.plantitas.model.RequirementLevel;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@DataJpaTest
class PlantRepositoryTest {

	@Autowired
	private PlantRepository plantRepository;

	@Test
	void findAll_withCombinedSpecifications_returnsPlantsMatchingAllCriteria() {
		Specification<Plant> specification = Specification
			.where(PlantSpecifications.hasCategory(PlantCategory.CACTUS))
			.and(PlantSpecifications.lightRequirementAtMost(RequirementLevel.HIGH))
			.and(PlantSpecifications.waterRequirementAtMost(RequirementLevel.LOW));

		List<Plant> result = plantRepository.findAll(specification, Sort.by(Sort.Direction.ASC, "commonName"));

		assertFalse(result.isEmpty());
		assertTrue(result.stream().allMatch(plant -> plant.getCategory() == PlantCategory.CACTUS));
		assertTrue(result.stream().allMatch(plant -> plant.getLightRequirement().ordinal() <= RequirementLevel.HIGH.ordinal()));
		assertTrue(result.stream().allMatch(plant -> plant.getWaterRequirement().ordinal() <= RequirementLevel.LOW.ordinal()));
	}

	@Test
	void findAll_withoutFilters_doesNotRestrictResults() {
		List<Plant> result = plantRepository.findAll(Specification.where(null), Sort.by(Sort.Direction.ASC, "commonName"));

		assertFalse(result.isEmpty());
	}
}
