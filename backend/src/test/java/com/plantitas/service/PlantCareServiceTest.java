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
		assertEquals("full", response.dataQuality());
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
		assertEquals("full", response.dataQuality());
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
		assertEquals("geocode-fallback", response.dataQuality());
	}

	@Test
	void getPlantCare_marksWeatherFallbackWhenWeatherFails() {
		Plant plant = createPlant(8L, "zz-plant", "ZZ Plant", "Zamioculcas zamiifolia", true);
		when(plantRepository.findBySlugIgnoreCase("zz-plant")).thenReturn(Optional.of(plant));
		when(reverseGeocodingClient.resolveCity(48.8566, 2.3522)).thenReturn("París");
		when(weatherClient.getCurrentWeather(48.8566, 2.3522)).thenThrow(new RuntimeException("timeout"));

		PlantCareResponse response = service.getPlantCare(new PlantCareRequest("zz-plant", null, 48.8566, 2.3522, "invierno"));

		assertEquals("París", response.city());
		assertEquals("weather-fallback", response.dataQuality());
	}

	@Test
	void getPlantCare_handlesPartialCoordinatesWithoutCallingExternalClients() {
		Plant plant = createPlant(9L, "snake-plant", "Snake Plant", "Dracaena trifasciata", true);
		when(plantRepository.findBySlugIgnoreCase("snake-plant")).thenReturn(Optional.of(plant));

		PlantCareResponse response = service.getPlantCare(
			new PlantCareRequest("snake-plant", "Lisboa", 38.7223, null, "primavera")
		);

		assertEquals("Lisboa", response.city());
		assertEquals("full", response.dataQuality());
		assertNull(response.temperature());
		assertNull(response.humidity());
		assertNull(response.altitude());
		verify(reverseGeocodingClient, never()).resolveCity(anyDouble(), anyDouble());
		verify(weatherClient, never()).getCurrentWeather(anyDouble(), anyDouble());
	}

	@Test
	void getPlantCare_marksGeocodeFallbackWhenReverseGeocodingReturnsBlank() {
		Plant plant = createPlant(13L, "anthurium", "Anthurium", "Anthurium andraeanum", true);
		when(plantRepository.findBySlugIgnoreCase("anthurium")).thenReturn(Optional.of(plant));
		when(reverseGeocodingClient.resolveCity(-0.1807, -78.4678)).thenReturn("   ");
		when(weatherClient.getCurrentWeather(-0.1807, -78.4678)).thenReturn(new WeatherData(20.0, 50, 2850.0, 0.0, 1));

		PlantCareResponse response = service.getPlantCare(
			new PlantCareRequest("anthurium", "Quito", -0.1807, -78.4678, "verano")
		);

		assertEquals("Quito", response.city());
		assertEquals("geocode-fallback", response.dataQuality());
	}

	@Test
	void getPlantCare_marksGeocodeFallbackWhenReverseGeocodingReturnsNull() {
		Plant plant = createPlant(17L, "peace-lily", "Peace Lily", "Spathiphyllum", true);
		when(plantRepository.findBySlugIgnoreCase("peace-lily")).thenReturn(Optional.of(plant));
		when(reverseGeocodingClient.resolveCity(37.7749, -122.4194)).thenReturn(null);
		when(weatherClient.getCurrentWeather(37.7749, -122.4194)).thenReturn(new WeatherData(18.0, 70, 16.0, 0.0, 2));

		PlantCareResponse response = service.getPlantCare(
			new PlantCareRequest("peace-lily", "San Francisco", 37.7749, -122.4194, "primavera")
		);

		assertEquals("San Francisco", response.city());
		assertEquals("geocode-fallback", response.dataQuality());
	}

	@Test
	void getPlantCare_marksWeatherFallbackWhenWeatherHasNoSignals() {
		Plant plant = createPlant(14L, "lavender", "Lavender", "Lavandula", true);
		when(plantRepository.findBySlugIgnoreCase("lavender")).thenReturn(Optional.of(plant));
		when(reverseGeocodingClient.resolveCity(-12.0464, -77.0428)).thenReturn("Lima");
		when(weatherClient.getCurrentWeather(-12.0464, -77.0428))
			.thenReturn(new WeatherData(null, null, 150.0, null, 0));

		PlantCareResponse response = service.getPlantCare(
			new PlantCareRequest("lavender", null, -12.0464, -77.0428, "invierno")
		);

		assertEquals("Lima", response.city());
		assertEquals("weather-fallback", response.dataQuality());
	}

	@Test
	void getPlantCare_addsPrecipitationTipWithoutSeparatorWhenNoPreviousSegments() {
		Plant plant = createPlant(15L, "mint", "Mint", "Mentha", true);
		when(plantRepository.findBySlugIgnoreCase("mint")).thenReturn(Optional.of(plant));
		when(reverseGeocodingClient.resolveCity(51.5072, -0.1276)).thenReturn("Londres");
		when(weatherClient.getCurrentWeather(51.5072, -0.1276))
			.thenReturn(new WeatherData(20.0, 50, 35.0, 1.8, 61));

		PlantCareResponse response = service.getPlantCare(
			new PlantCareRequest("mint", null, 51.5072, -0.1276, "primavera")
		);

		assertTrue(response.recommendation().contains("Según el clima actual de tu ubicación: hay precipitación reciente"));
	}

	@Test
	void getPlantCare_addsHumidityTipsWhenTemperatureIsMissing() {
		Plant plant = createPlant(16L, "begonia", "Begonia", "Begonia rex", true);
		when(plantRepository.findBySlugIgnoreCase("begonia")).thenReturn(Optional.of(plant));
		when(reverseGeocodingClient.resolveCity(6.2442, -75.5812)).thenReturn("Medellín");
		when(weatherClient.getCurrentWeather(6.2442, -75.5812))
			.thenReturn(new WeatherData(null, 88, 1495.0, 0.0, 3));

		PlantCareResponse highHumidityResponse = service.getPlantCare(
			new PlantCareRequest("begonia", null, 6.2442, -75.5812, "otoño")
		);

		assertTrue(highHumidityResponse.recommendation().contains("humedad elevada, evita encharcamientos"));

		when(weatherClient.getCurrentWeather(6.2442, -75.5812))
			.thenReturn(new WeatherData(null, 20, 1495.0, 0.0, 3));

		PlantCareResponse lowHumidityResponse = service.getPlantCare(
			new PlantCareRequest("begonia", null, 6.2442, -75.5812, "otoño")
		);

		assertTrue(lowHumidityResponse.recommendation().contains("humedad baja, considera aumentar humedad ambiental"));
	}

	@Test
	void getPlantCare_keepsFullQualityWhenOnlyPrecipitationExists() {
		Plant plant = createPlant(18L, "calendula", "Caléndula", "Calendula officinalis", true);
		when(plantRepository.findBySlugIgnoreCase("calendula")).thenReturn(Optional.of(plant));
		when(reverseGeocodingClient.resolveCity(52.52, 13.41)).thenReturn("Berlín");
		when(weatherClient.getCurrentWeather(52.52, 13.41))
			.thenReturn(new WeatherData(null, null, 34.0, 2.1, 61));

		PlantCareResponse response = service.getPlantCare(
			new PlantCareRequest("calendula", null, 52.52, 13.41, "primavera")
		);

		assertEquals("full", response.dataQuality());
		assertTrue(response.recommendation().contains("hay precipitación reciente"));
	}

	@Test
	void getPlantCare_omitsWeatherTipWhenHumidityAndPrecipitationAreNullAndTemperatureIsMild() {
		Plant plant = createPlant(19L, "chive", "Cebollín", "Allium schoenoprasum", true);
		when(plantRepository.findBySlugIgnoreCase("chive")).thenReturn(Optional.of(plant));
		when(reverseGeocodingClient.resolveCity(41.3851, 2.1734)).thenReturn("Barcelona");
		when(weatherClient.getCurrentWeather(41.3851, 2.1734))
			.thenReturn(new WeatherData(20.0, null, 12.0, null, 1));

		PlantCareResponse response = service.getPlantCare(
			new PlantCareRequest("chive", null, 41.3851, 2.1734, "primavera")
		);

		assertEquals("full", response.dataQuality());
		assertFalse(response.recommendation().contains("Según el clima actual de tu ubicación"));
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
		assertEquals("full", warmResponse.dataQuality());
		assertEquals("full", coldResponse.dataQuality());
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
		verify(plantRepository, never()).findTop10ByCommonNameContainingIgnoreCaseOrScientificNameContainingIgnoreCaseOrderByCommonNameAsc(anyString(), anyString());
	}

	@Test
	void searchPlants_mapsRepositoryEntitiesToDto() {
		Plant plant = createPlant(7L, "aloe-vera", "Aloe Vera", "Aloe barbadensis", true);
		when(plantRepository.findTop10ByCommonNameContainingIgnoreCaseOrScientificNameContainingIgnoreCaseOrderByCommonNameAsc("aloe", "aloe"))
			.thenReturn(List.of(plant));

		List<PlantSearchItem> result = service.searchPlants("aloe");

		assertEquals(1, result.size());
		assertEquals("Aloe Vera", result.getFirst().common_name());
		assertEquals("Aloe barbadensis", result.getFirst().scientific_name());
		assertEquals("https://img.test/aloe.jpg", result.getFirst().image_url());
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
		assertNull(result.ideal_climate());
		assertNull(result.ideal_temperature());
		assertNull(result.ideal_humidity());
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
