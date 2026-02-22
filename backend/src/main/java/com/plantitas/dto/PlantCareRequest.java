package com.plantitas.dto;

public record PlantCareRequest(
	String plantId,
	String city,
	String season
) {
}
