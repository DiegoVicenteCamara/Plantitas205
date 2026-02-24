package com.plantitas.dto;

public record PlantSearchItem(
	Long id,
	String common_name,
	String scientific_name,
	String image_url
) {
}
