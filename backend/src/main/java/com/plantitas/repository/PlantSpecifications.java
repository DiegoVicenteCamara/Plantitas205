package com.plantitas.repository;

import com.plantitas.model.Plant;
import com.plantitas.model.PlantCategory;
import com.plantitas.model.RequirementLevel;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class PlantSpecifications {

	private PlantSpecifications() {}

	public static Specification<Plant> commonOrScientificNameContains(String query) {
		if (query == null || query.isBlank()) {
			return null;
		}

		String normalizedQuery = "%" + query.toLowerCase() + "%";
		return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.or(
			criteriaBuilder.like(criteriaBuilder.lower(root.get("commonName")), normalizedQuery),
			criteriaBuilder.like(criteriaBuilder.lower(root.get("scientificName")), normalizedQuery)
		);
	}

	public static Specification<Plant> hasCategory(PlantCategory category) {
		if (category == null) {
			return null;
		}

		return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("category"), category);
	}

	public static Specification<Plant> lightRequirementAtMost(RequirementLevel threshold) {
		return requirementAtMost("lightRequirement", threshold);
	}

	public static Specification<Plant> waterRequirementAtMost(RequirementLevel threshold) {
		return requirementAtMost("waterRequirement", threshold);
	}

	private static Specification<Plant> requirementAtMost(String fieldName, RequirementLevel threshold) {
		if (threshold == null) {
			return null;
		}

		List<RequirementLevel> acceptedLevels = List.of(RequirementLevel.values())
			.subList(0, threshold.ordinal() + 1);
		return (root, criteriaQuery, criteriaBuilder) -> root.get(fieldName).in(acceptedLevels);
	}
}
