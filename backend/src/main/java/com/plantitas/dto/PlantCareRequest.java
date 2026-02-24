package com.plantitas.dto;

public record PlantCareRequest(
	String plantId,
	String city,
	Double latitude,
	Double longitude,
	String season
) {
}
