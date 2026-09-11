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
	void findAll_withCombinedSpecifications_returnsPlantsMatchingExactCriteria() {
		Specification<Plant> specification = Specification
			.where(PlantSpecifications.hasCategory(PlantCategory.SUCULENTA))
			.and(PlantSpecifications.lightRequirementEquals(RequirementLevel.LOW))
			.and(PlantSpecifications.waterRequirementEquals(RequirementLevel.LOW))
			.and(PlantSpecifications.humidityRequirementEquals(RequirementLevel.LOW));

		List<Plant> result = plantRepository.findAll(specification, Sort.by(Sort.Direction.ASC, "commonName"));

		assertFalse(result.isEmpty());
		assertTrue(result.stream().allMatch(plant -> plant.getCategory() == PlantCategory.SUCULENTA));
		assertTrue(result.stream().allMatch(plant -> plant.getLightRequirement() == RequirementLevel.LOW));
		assertTrue(result.stream().allMatch(plant -> plant.getWaterRequirement() == RequirementLevel.LOW));
		assertTrue(result.stream().allMatch(plant -> plant.getHumidityRequirement() == RequirementLevel.LOW));
	}

	@Test
	void findAll_withLowWater_doesNotIncludeMediumOrHighWater() {
		Specification<Plant> specification = Specification
			.where(PlantSpecifications.waterRequirementEquals(RequirementLevel.LOW));

		List<Plant> result = plantRepository.findAll(specification, Sort.by(Sort.Direction.ASC, "commonName"));

		assertFalse(result.isEmpty());
		assertTrue(result.stream().allMatch(plant -> plant.getWaterRequirement() == RequirementLevel.LOW));
		assertTrue(result.stream().noneMatch(plant -> plant.getWaterRequirement() == RequirementLevel.MEDIUM));
		assertTrue(result.stream().noneMatch(plant -> plant.getWaterRequirement() == RequirementLevel.HIGH));
	}

	@Test
	void findAll_withMediumLight_doesNotIncludeLowOrHighLight() {
		Specification<Plant> specification = Specification
			.where(PlantSpecifications.lightRequirementEquals(RequirementLevel.MEDIUM));

		List<Plant> result = plantRepository.findAll(specification, Sort.by(Sort.Direction.ASC, "commonName"));

		assertFalse(result.isEmpty());
		assertTrue(result.stream().allMatch(plant -> plant.getLightRequirement() == RequirementLevel.MEDIUM));
		assertTrue(result.stream().noneMatch(plant -> plant.getLightRequirement() == RequirementLevel.LOW));
		assertTrue(result.stream().noneMatch(plant -> plant.getLightRequirement() == RequirementLevel.HIGH));
	}

	@Test
	void findAll_withoutFilters_doesNotRestrictResults() {
		List<Plant> result = plantRepository.findAll(Specification.where(null), Sort.by(Sort.Direction.ASC, "commonName"));

		assertFalse(result.isEmpty());
	}
}
