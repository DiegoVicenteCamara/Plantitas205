package com.plantitas.dto;

public record PlantCareResponse(
		String plantId,
		String city,
		String season,
		String summary,
		String recommendation,
		boolean indoorFriendly
) {
}
