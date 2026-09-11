package com.plantitas.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import org.junit.jupiter.api.Test;

class DtoRecordsTest {

	@Test
	void plantCareRequest_exposesAllFields() {
		PlantCareRequest request = new PlantCareRequest("monstera", "Madrid", 40.4, -3.7, "verano");

		assertEquals("monstera", request.plantId());
		assertEquals("Madrid", request.city());
		assertEquals(40.4, request.latitude());
		assertEquals(-3.7, request.longitude());
		assertEquals("verano", request.season());
	}

	@Test
	void plantCareResponse_exposesAllFields() {
		PlantCareResponse response = new PlantCareResponse(
			"Monstera",
			"Madrid",
			"primavera",
			"Resumen",
			"Recomendación",
			false,
			21.3,
			55,
			650.0,
			"full",
			"20-28 °C",
			"60-80%",
			true,
			false
		);

		assertEquals("Monstera", response.plantId());
		assertEquals("Madrid", response.city());
		assertEquals("primavera", response.season());
		assertEquals("Resumen", response.summary());
		assertEquals("Recomendación", response.recommendation());
		assertFalse(response.indoorFriendly());
		assertEquals(21.3, response.temperature());
		assertEquals(55, response.humidity());
		assertEquals(650.0, response.altitude());
		assertEquals("full", response.dataQuality());
		assertEquals("20-28 °C", response.idealTemperature());
		assertEquals("60-80%", response.idealHumidity());
		assertEquals(true, response.temperatureInRange());
		assertEquals(false, response.humidityInRange());
	}

	@Test
	void plantSearchDtos_exposeAllFields() {
		PlantSearchItem item = new PlantSearchItem(1L, "Aloe", "Aloe barbadensis", "https://img.test/aloe.jpg");
		PlantSearchResponse response = new PlantSearchResponse(List.of(item));

		assertEquals(1L, item.id());
		assertEquals("Aloe", item.commonName());
		assertEquals("Aloe barbadensis", item.scientificName());
		assertEquals("https://img.test/aloe.jpg", item.imageUrl());
		assertEquals(1, response.data().size());
	}

	@Test
	void plantDetailResponse_exposesIdealConditionFields() {
		PlantDetailResponse detail = new PlantDetailResponse(
			1L,
			"monstera",
			"Monstera",
			"Monstera deliciosa",
			"img1",
			true,
			"Riego moderado",
			"Luz indirecta",
			"Tropical",
			"20-28 °C",
			"60-80%",
			"Moderada para mascotas"
		);

		assertEquals("Tropical", detail.idealClimate());
		assertEquals("20-28 °C", detail.idealTemperature());
		assertEquals("60-80%", detail.idealHumidity());
		assertEquals("Moderada para mascotas", detail.toxicidad());
	}
}
