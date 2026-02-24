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
			false
		);

		assertEquals("Monstera", response.plantId());
		assertEquals("Madrid", response.city());
		assertEquals("primavera", response.season());
		assertEquals("Resumen", response.summary());
		assertEquals("Recomendación", response.recommendation());
		assertFalse(response.indoorFriendly());
	}

	@Test
	void plantSearchDtos_exposeAllFields() {
		PlantSearchItem item = new PlantSearchItem(1L, "Aloe", "Aloe vera", "image-url");
		PlantSearchResponse response = new PlantSearchResponse(List.of(item));

		assertEquals(1L, item.id());
		assertEquals("Aloe", item.common_name());
		assertEquals("Aloe vera", item.scientific_name());
		assertEquals("image-url", item.image_url());
		assertEquals(1, response.data().size());
	}
}
