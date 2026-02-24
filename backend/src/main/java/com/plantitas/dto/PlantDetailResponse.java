package com.plantitas.dto;

public record PlantDetailResponse(
	Long id,
	String slug,
	String common_name,
	String scientific_name,
	String image_url,
	boolean indoor_friendly,
	String watering_recommendation,
	String light_recommendation,
	String ideal_climate,
	String ideal_temperature,
	String ideal_humidity
) {
}