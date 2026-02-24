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
			"full"
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
	}

	@Test
	void plantSearchDtos_exposeAllFields() {
		PlantSearchItem item = new PlantSearchItem(1L, "Aloe", "Aloe barbadensis", "https://img.test/aloe.jpg");
		PlantSearchResponse response = new PlantSearchResponse(List.of(item));

		assertEquals(1L, item.id());
		assertEquals("Aloe", item.common_name());
		assertEquals("Aloe barbadensis", item.scientific_name());
		assertEquals("https://img.test/aloe.jpg", item.image_url());
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
			"Luz indirecta brillante",
			"Cada 5-7 días",
			20.0,
			28.0,
			"Moderada para mascotas"
		);

		assertEquals("Tropical", detail.ideal_climate());
		assertEquals("20-28 °C", detail.ideal_temperature());
		assertEquals("60-80%", detail.ideal_humidity());
		assertEquals("Luz indirecta brillante", detail.requerimientos_luz());
		assertEquals("Cada 5-7 días", detail.frecuencia_riego());
		assertEquals(20.0, detail.temperatura_ideal_min());
		assertEquals(28.0, detail.temperatura_ideal_max());
		assertEquals("Moderada para mascotas", detail.toxicidad());
	}
}
