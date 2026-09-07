package com.plantitas.dto;

import com.plantitas.model.PlantCategory;
import com.plantitas.model.RequirementLevel;

public record PlantSearchCriteria(
	String query,
	PlantCategory category,
	RequirementLevel lightRequirement,
	RequirementLevel waterRequirement,
	RequirementLevel humidityRequirement
) {
}
