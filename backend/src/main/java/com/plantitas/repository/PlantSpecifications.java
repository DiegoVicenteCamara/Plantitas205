package com.plantitas.repository;

import com.plantitas.model.Plant;
import com.plantitas.model.PlantCategory;
import com.plantitas.model.RequirementLevel;
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

	public static Specification<Plant> lightRequirementEquals(RequirementLevel level) {
		return requirementEquals("lightRequirement", level);
	}

	public static Specification<Plant> waterRequirementEquals(RequirementLevel level) {
		return requirementEquals("waterRequirement", level);
	}

	public static Specification<Plant> humidityRequirementEquals(RequirementLevel level) {
		return requirementEquals("humidityRequirement", level);
	}

	private static Specification<Plant> requirementEquals(String fieldName, RequirementLevel level) {
		if (level == null) {
			return null;
		}
		return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(fieldName), level);
	}
}
