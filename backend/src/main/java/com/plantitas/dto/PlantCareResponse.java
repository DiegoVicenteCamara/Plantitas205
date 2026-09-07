package com.plantitas.dto;

public record PlantCareResponse(
	String plantId,
	String city,
	String season,
	String summary,
	String recommendation,
	boolean indoorFriendly,
	Double temperature,
	Integer humidity,
	Double altitude,
	String dataQuality,
	String idealTemperature,
	String idealHumidity,
	Boolean temperatureInRange,
	Boolean humidityInRange
) {
}
