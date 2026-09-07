package com.plantitas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlantSearchItem(
	Long id,
	@JsonProperty("common_name") String commonName,
	@JsonProperty("scientific_name") String scientificName,
	@JsonProperty("image_url") String imageUrl
) {
}
