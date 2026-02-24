package com.plantitas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.plantitas.dto.PlantCareRequest;
import com.plantitas.dto.PlantCareResponse;
import com.plantitas.dto.PlantDetailResponse;
import com.plantitas.dto.PlantSearchItem;
import com.plantitas.model.Plant;
import com.plantitas.repository.PlantRepository;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlantCareServiceTest {

	@Mock
	private PlantRepository plantRepository;

	@Mock
	private WeatherClient weatherClient;

	@Mock
	private ReverseGeocodingClient reverseGeocodingClient;

	private PlantCareService service;

	@BeforeEach
	void setUp() {
		service = new PlantCareService(plantRepository, weatherClient, reverseGeocodingClient);
	}

	@Test
	void getPlantCare_prioritizesCoordinatesOverCity() {
		Plant plant = createPlant(1L, "monstera", "Monstera", "Monstera deliciosa", true);
		when(plantRepository.findById(1L)).thenReturn(Optional.of(plant));
		when(reverseGeocodingClient.resolveCity(40.4168, -3.7038)).thenReturn("Madrid");
		when(weatherClient.getCurrentWeather(40.4168, -3.7038)).thenReturn(new WeatherData(24.5, 62, 667.0, 0.0, 1));

		PlantCareRequest request = new PlantCareRequest("1", "Madrid", 40.4168, -3.7038, "verano");
		PlantCareResponse response = service.getPlantCare(request);

		assertEquals("Monstera", response.plantId());
		assertEquals("Madrid", response.city());
		assertEquals("verano", response.season());
		assertEquals(24.5, response.temperature());
		assertEquals(62, response.humidity());
		assertEquals(667.0, response.altitude());
		assertTrue(response.summary().contains("Madrid"));
		assertTrue(response.recommendation().contains("En verano revisa humedad del sustrato con más frecuencia."));
		verify(reverseGeocodingClient).resolveCity(40.4168, -3.7038);
	}

	@Test
	void getPlantCare_usesSlugWhenNonNumericPlantIdAndNormalizesAutumnAlias() {
		Plant plant = createPlant(2L, "pothos", "Pothos", "Epipremnum aureum", true);
		when(plantRepository.findBySlugIgnoreCase("pothos")).thenReturn(Optional.of(plant));

		PlantCareRequest request = new PlantCareRequest("pothos", "  Valencia  ", null, null, "otono");
		PlantCareResponse response = service.getPlantCare(request);

		assertEquals("Valencia", response.city());
		assertEquals("otoño", response.season());
		assertNull(response.temperature());
		assertNull(response.humidity());
		assertNull(response.altitude());
		assertTrue(response.recommendation().contains("En otoño reduce ligeramente la frecuencia de riego."));
		verify(plantRepository).findBySlugIgnoreCase("pothos");
		verify(reverseGeocodingClient, never()).resolveCity(anyDouble(), anyDouble());
		verify(weatherClient, never()).getCurrentWeather(anyDouble(), anyDouble());
	}

	@Test
	void getPlantCare_fallsBackToGenericLocationWhenReverseGeocodingFails() {
		Plant plant = createPlant(5L, "ficus-lyrata", "Ficus Lyrata", "Ficus lyrata", true);
		when(plantRepository.findBySlugIgnoreCase("ficus-lyrata")).thenReturn(Optional.of(plant));
		when(reverseGeocodingClient.resolveCity(4.6097, -74.0817)).thenThrow(new RuntimeException("timeout"));

		PlantCareRequest request = new PlantCareRequest("ficus-lyrata", null, 4.6097, -74.0817, "primavera");
		PlantCareResponse response = service.getPlantCare(request);

		assertEquals("Ubicación seleccionada", response.city());
		assertTrue(response.summary().contains("Ubicación seleccionada"));
	}

	@Test
	void getPlantCare_changesWeatherDataAndRecommendationByCoordinates() {
		Plant plant = createPlant(6L, "pothos", "Pothos", "Epipremnum aureum", true);
		when(plantRepository.findBySlugIgnoreCase("pothos")).thenReturn(Optional.of(plant));
		when(reverseGeocodingClient.resolveCity(19.4326, -99.1332)).thenReturn("Ciudad de México");
		when(reverseGeocodingClient.resolveCity(-33.4489, -70.6693)).thenReturn("Santiago");
		when(weatherClient.getCurrentWeather(19.4326, -99.1332)).thenReturn(new WeatherData(33.0, 25, 2240.0, 0.0, 1));
		when(weatherClient.getCurrentWeather(-33.4489, -70.6693)).thenReturn(new WeatherData(7.0, 88, 520.0, 1.2, 61));

		PlantCareResponse warmResponse = service.getPlantCare(new PlantCareRequest("pothos", null, 19.4326, -99.1332, "verano"));
		PlantCareResponse coldResponse = service.getPlantCare(new PlantCareRequest("pothos", null, -33.4489, -70.6693, "invierno"));

		assertEquals("Ciudad de México", warmResponse.city());
		assertEquals("Santiago", coldResponse.city());
		assertTrue(warmResponse.temperature() > coldResponse.temperature());
		assertTrue(warmResponse.recommendation().contains("temperatura alta"));
		assertTrue(coldResponse.recommendation().contains("temperatura baja"));
		assertTrue(coldResponse.recommendation().contains("hay precipitación reciente"));
	}

	@Test
	void getPlantCare_usesRepositorySearchFallbackWhenSlugNotFound() {
		Plant plant = createPlant(3L, "calathea", "Calathea", "Calathea orbifolia", false);
		when(plantRepository.findBySlugIgnoreCase("calathea-orbifolia")).thenReturn(Optional.empty());
		when(plantRepository.findByCommonNameContainingIgnoreCaseOrScientificNameContainingIgnoreCase(
			"calathea-orbifolia", "calathea-orbifolia"
		)).thenReturn(List.of(plant));

		PlantCareRequest request = new PlantCareRequest("calathea-orbifolia", "Bogotá", null, null, "invierno");
		PlantCareResponse response = service.getPlantCare(request);

		assertEquals("Calathea", response.plantId());
		assertEquals("invierno", response.season());
		assertTrue(response.recommendation().contains("En invierno evita excesos de agua y corrientes frías."));
		assertFalse(response.indoorFriendly());
	}

	@Test
	void getPlantCare_defaultsCityAndSeasonWhenMissing() {
		Plant plant = createPlant(4L, "ficus", "Ficus", "Ficus elastica", true);
		when(plantRepository.findBySlugIgnoreCase("ficus")).thenReturn(Optional.of(plant));

		PlantCareRequest request = new PlantCareRequest("ficus", "   ", null, null, null);
		PlantCareResponse response = service.getPlantCare(request);

		assertEquals("No indicada", response.city());
		assertEquals("primavera", response.season());
		assertTrue(response.recommendation().contains("En primavera puedes retomar fertilización suave si aplica."));
	}

	@Test
	void getPlantCare_throwsWhenPlantIdBlank() {
		PlantCareRequest request = new PlantCareRequest("  ", "Madrid", null, null, "verano");

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.getPlantCare(request));

		assertEquals("El identificador de la planta es obligatorio.", exception.getMessage());
	}

	@Test
	void getPlantCare_throwsWhenPlantNotFoundAnywhere() {
		when(plantRepository.findBySlugIgnoreCase("not-found")).thenReturn(Optional.empty());
		when(plantRepository.findByCommonNameContainingIgnoreCaseOrScientificNameContainingIgnoreCase("not-found", "not-found"))
			.thenReturn(List.of());

		PlantCareRequest request = new PlantCareRequest("not-found", "Madrid", null, null, "verano");

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.getPlantCare(request));

		assertEquals("No existe una planta de prueba para el valor indicado.", exception.getMessage());
	}

	@Test
	void searchPlants_returnsEmptyOnBlankQuery() {
		List<PlantSearchItem> result = service.searchPlants("   ");

		assertTrue(result.isEmpty());
		verify(plantRepository, never()).findTop10ByCommonNameContainingIgnoreCaseOrderByCommonNameAsc(anyString());
	}

	@Test
	void searchPlants_mapsRepositoryEntitiesToDto() {
		Plant plant = createPlant(7L, "aloe-vera", "Aloe Vera", "Aloe barbadensis", true);
		when(plantRepository.findTop10ByCommonNameContainingIgnoreCaseOrderByCommonNameAsc("aloe"))
			.thenReturn(List.of(plant));

		List<PlantSearchItem> result = service.searchPlants("aloe");

		assertEquals(1, result.size());
		assertEquals("Aloe Vera", result.getFirst().common_name());
	}

	@Test
	void suggestPlantNames_returnsEmptyOnBlankPrefix() {
		List<String> result = service.suggestPlantNames(" ");

		assertTrue(result.isEmpty());
		verify(plantRepository, never()).findTop10ByCommonNameStartingWithIgnoreCaseOrderByCommonNameAsc(anyString());
	}

	@Test
	void suggestPlantNames_returnsDistinctNames() {
		Plant p1 = createPlant(10L, "spider-plant", "Spider Plant", "Chlorophytum comosum", true);
		Plant p2 = createPlant(11L, "spider-plant-2", "Spider Plant", "Chlorophytum comosum", true);
		when(plantRepository.findTop10ByCommonNameStartingWithIgnoreCaseOrderByCommonNameAsc("spi"))
			.thenReturn(List.of(p1, p2));

		List<String> result = service.suggestPlantNames("spi");

		assertEquals(List.of("Spider Plant"), result);
	}

	@Test
	void getPlantById_returnsMappedDetailWhenFound() {
		Plant plant = createPlant(12L, "aloe-vera", "Aloe Vera", "Aloe barbadensis", true);
		setField(plant, "imageUrl", "https://image.test/aloe.jpg");
		when(plantRepository.findById(12L)).thenReturn(Optional.of(plant));

		PlantDetailResponse result = service.getPlantById(12L);

		assertEquals(12L, result.id());
		assertEquals("aloe-vera", result.slug());
		assertEquals("Aloe Vera", result.common_name());
		assertEquals("Aloe barbadensis", result.scientific_name());
		assertEquals("https://image.test/aloe.jpg", result.image_url());
		assertTrue(result.indoor_friendly());
	}

	@Test
	void getPlantById_throwsWhenNotFound() {
		when(plantRepository.findById(999L)).thenReturn(Optional.empty());

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.getPlantById(999L));

		assertEquals("No existe una planta con ese ID.", exception.getMessage());
	}

	private Plant createPlant(Long id, String slug, String commonName, String scientificName, boolean indoorFriendly) {
		Plant plant = new Plant();
		setField(plant, "id", id);
		setField(plant, "slug", slug);
		setField(plant, "commonName", commonName);
		setField(plant, "scientificName", scientificName);
		setField(plant, "indoorFriendly", indoorFriendly);
		setField(plant, "wateringRecommendation", "Riego moderado.");
		setField(plant, "lightRecommendation", "Luz indirecta brillante.");
		return plant;
	}

	private void setField(Object target, String fieldName, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("No se pudo configurar el objeto de prueba", exception);
		}
	}
}
