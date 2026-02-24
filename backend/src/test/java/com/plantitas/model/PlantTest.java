package com.plantitas.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class PlantTest {

	@Test
	void getters_returnAssignedValues() {
		Plant plant = new Plant();
		setField(plant, "id", 15L);
		setField(plant, "slug", "peace-lily");
		setField(plant, "commonName", "Peace Lily");
		setField(plant, "scientificName", "Spathiphyllum wallisii");
		setField(plant, "imageUrl", "img-url");
		setField(plant, "indoorFriendly", true);
		setField(plant, "wateringRecommendation", "Riego moderado");
		setField(plant, "lightRecommendation", "Luz indirecta");
		setField(plant, "idealClimate", "Tropical húmedo");
		setField(plant, "idealHumidity", "60-80%");
		setField(plant, "idealTemperatureMin", 20.0);
		setField(plant, "idealTemperatureMax", 28.0);
		setField(plant, "toxicidad", "Moderada para mascotas");

		assertEquals(15L, plant.getId());
		assertEquals("peace-lily", plant.getSlug());
		assertEquals("Peace Lily", plant.getCommonName());
		assertEquals("Spathiphyllum wallisii", plant.getScientificName());
		assertEquals("img-url", plant.getImageUrl());
		assertTrue(plant.isIndoorFriendly());
		assertEquals("Riego moderado", plant.getWateringRecommendation());
		assertEquals("Luz indirecta", plant.getLightRecommendation());
		assertEquals("Tropical húmedo", plant.getIdealClimate());
		assertEquals("60-80%", plant.getIdealHumidity());
		assertEquals(20.0, plant.getIdealTemperatureMin());
		assertEquals(28.0, plant.getIdealTemperatureMax());
		assertEquals("Moderada para mascotas", plant.getToxicidad());
	}

	private void setField(Object target, String fieldName, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("No se pudo preparar el objeto de prueba", exception);
		}
	}
}
