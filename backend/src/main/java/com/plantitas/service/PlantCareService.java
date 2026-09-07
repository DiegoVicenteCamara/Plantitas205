package com.plantitas.service;

import com.plantitas.dto.PlantCareRequest;
import com.plantitas.dto.PlantCareResponse;
import com.plantitas.dto.PlantDetailResponse;
import com.plantitas.dto.PlantSearchItem;
import com.plantitas.exception.ResourceNotFoundException;
import com.plantitas.model.Plant;
import com.plantitas.model.PlantCategory;
import com.plantitas.model.RequirementLevel;
import com.plantitas.repository.PlantRepository;
import com.plantitas.repository.PlantSpecifications;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class PlantCareService {

	private static final Logger log = LoggerFactory.getLogger(PlantCareService.class);

	private final PlantRepository plantRepository;
	private final WeatherClient weatherClient;
	private final ReverseGeocodingClient reverseGeocodingClient;

	public PlantCareService(
		PlantRepository plantRepository,
		WeatherClient weatherClient,
		ReverseGeocodingClient reverseGeocodingClient
	) {
		this.plantRepository = plantRepository;
		this.weatherClient = weatherClient;
		this.reverseGeocodingClient = reverseGeocodingClient;
	}

	public PlantCareResponse getPlantCare(PlantCareRequest request) {
		log.debug("Processing plant care request for plantId={}, season={}", request.plantId(), request.season());

		Plant plant = resolvePlant(request.plantId());
		String normalizedSeason = normalizeSeason(request.season());
		LocationResolution locationResolution = resolveLocationForClimate(request);
		WeatherResolution weatherResolution = resolveWeatherData(request);
		String city = locationResolution.city();
		WeatherData weatherData = weatherResolution.weatherData();
		String dataQuality = determineDataQuality(locationResolution.geocodeFallback(), weatherResolution.weatherFallback());

		String summary = "Para " + plant.getCommonName() + " en " + city + " durante " + normalizedSeason + ".";
		String recommendation = buildRecommendation(plant, normalizedSeason, weatherData);

		log.debug("Plant care resolved: plant={}, city={}, quality={}", plant.getCommonName(), city, dataQuality);

		return new PlantCareResponse(
			plant.getCommonName(),
			city,
			normalizedSeason,
			summary,
			recommendation,
			plant.isIndoorFriendly(),
			weatherData.temperature(),
			weatherData.humidity(),
			weatherData.altitude(),
			dataQuality
		);
	}

	private WeatherResolution resolveWeatherData(PlantCareRequest request) {
		if (request.latitude() == null || request.longitude() == null) {
			return new WeatherResolution(WeatherData.empty(), false);
		}

		try {
			WeatherData weatherData = weatherClient.getCurrentWeather(request.latitude(), request.longitude());
			if (weatherData == null) {
				return new WeatherResolution(WeatherData.empty(), true);
			}

			boolean weatherFallback = weatherData.temperature() == null
				&& weatherData.humidity() == null
				&& weatherData.precipitation() == null;
			return new WeatherResolution(weatherData, weatherFallback);
		} catch (RuntimeException exception) {
			log.warn("Weather lookup failed for ({}, {}): {}", request.latitude(), request.longitude(), exception.getMessage());
			return new WeatherResolution(WeatherData.empty(), true);
		}
	}

	private LocationResolution resolveLocationForClimate(PlantCareRequest request) {
		if (request.latitude() != null && request.longitude() != null) {
			String fallbackCity = normalizeText(request.city(), "Ubicación seleccionada");
			try {
				String resolvedCity = reverseGeocodingClient.resolveCity(request.latitude(), request.longitude());
				String normalizedCity = normalizeText(resolvedCity, fallbackCity);
				boolean geocodeFallback = resolvedCity == null || resolvedCity.trim().isEmpty();
				return new LocationResolution(normalizedCity, geocodeFallback);
			} catch (RuntimeException exception) {
				log.warn("Reverse geocoding failed for ({}, {}): {}", request.latitude(), request.longitude(), exception.getMessage());
				return new LocationResolution(fallbackCity, true);
			}
		}

		return new LocationResolution(normalizeText(request.city(), "No indicada"), false);
	}

	private String determineDataQuality(boolean geocodeFallback, boolean weatherFallback) {
		if (geocodeFallback) {
			return "geocode-fallback";
		}
		if (weatherFallback) {
			return "weather-fallback";
		}
		return "full";
	}

	public List<PlantSearchItem> searchPlants(String query) {
		return searchPlants(query, null, null, null, null);
	}

	public List<PlantSearchItem> searchPlants(String query, String category, String light, String water, String humidity) {
		String normalizedQuery = normalizeText(query, "");
		PlantCategory normalizedCategory = parseCategory(category);
		RequirementLevel lightThreshold = parseRequirementLevel(light, "light");
		RequirementLevel waterThreshold = parseRequirementLevel(water, "water");
		RequirementLevel humidityThreshold = parseRequirementLevel(humidity, "humidity");

		Specification<Plant> specification = buildSearchSpecification(
			normalizedQuery, normalizedCategory, lightThreshold, waterThreshold, humidityThreshold
		);

		return plantRepository
			.findAll(specification, Sort.by(Sort.Direction.ASC, "commonName"))
			.stream()
			.map(plant -> new PlantSearchItem(plant.getId(), plant.getCommonName(), plant.getScientificName(), plant.getImageUrl()))
			.toList();
	}

	private Specification<Plant> buildSearchSpecification(
		String query, PlantCategory category,
		RequirementLevel light, RequirementLevel water, RequirementLevel humidity
	) {
		return Specification
			.where(PlantSpecifications.commonOrScientificNameContains(query))
			.and(PlantSpecifications.hasCategory(category))
			.and(PlantSpecifications.lightRequirementEquals(light))
			.and(PlantSpecifications.waterRequirementEquals(water))
			.and(PlantSpecifications.humidityRequirementEquals(humidity));
	}

	public List<String> suggestPlantNames(String prefix) {
		String normalizedPrefix = normalizeText(prefix, "");
		if (normalizedPrefix.isBlank()) {
			return List.of();
		}

		return plantRepository.findTop10ByCommonNameStartingWithIgnoreCaseOrderByCommonNameAsc(normalizedPrefix)
			.stream()
			.map(Plant::getCommonName)
			.distinct()
			.toList();
	}

	public PlantDetailResponse getPlantById(Long id) {
		Plant plant = plantRepository
			.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("No existe una planta con ese ID."));

		return PlantDetailResponse.builder()
			.id(plant.getId())
			.slug(plant.getSlug())
			.commonName(plant.getCommonName())
			.scientificName(plant.getScientificName())
			.imageUrl(plant.getImageUrl())
			.indoorFriendly(plant.isIndoorFriendly())
			.wateringRecommendation(plant.getWateringRecommendation())
			.lightRecommendation(plant.getLightRecommendation())
			.idealClimate(plant.getIdealClimate())
			.idealTemperature(plant.getIdealTemperature())
			.idealHumidity(plant.getIdealHumidity())
			.toxicidad(plant.getToxicidad())
			.build();
	}

	private Plant resolvePlant(String plantId) {
		String normalizedPlantId = normalizeText(plantId, "");
		if (normalizedPlantId.isBlank()) {
			throw new IllegalArgumentException("El identificador de la planta es obligatorio.");
		}

		return findPlantByIdOrSlugOrName(normalizedPlantId)
			.orElseThrow(() -> new ResourceNotFoundException("No existe una planta de prueba para el valor indicado."));
	}

	private Optional<Plant> findPlantByIdOrSlugOrName(String candidate) {
		return tryParseLong(candidate)
			.flatMap(plantRepository::findById)
			.or(() -> plantRepository.findBySlugIgnoreCase(candidate))
			.or(() -> plantRepository
				.findByCommonNameContainingIgnoreCaseOrScientificNameContainingIgnoreCase(candidate, candidate)
				.stream()
				.findFirst());
	}

	private Optional<Long> tryParseLong(String value) {
		if (value.chars().allMatch(Character::isDigit)) {
			return Optional.of(Long.parseLong(value));
		}
		return Optional.empty();
	}

	private String normalizeSeason(String season) {
		String normalized = normalizeText(season, "primavera").toLowerCase(Locale.ROOT);
		return switch (normalized) {
			case "verano" -> "verano";
			case "otono", "otoño" -> "otoño";
			case "invierno" -> "invierno";
			default -> "primavera";
		};
	}

	private String normalizeText(String value, String defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? defaultValue : trimmed;
	}

	private PlantCategory parseCategory(String category) {
		String normalizedCategory = normalizeText(category, "");
		if (normalizedCategory.isBlank()) {
			return null;
		}

		try {
			return PlantCategory.valueOf(normalizedCategory.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Valor inválido para category. Usa una categoría válida.");
		}
	}

	private RequirementLevel parseRequirementLevel(String value, String fieldName) {
		String normalizedValue = normalizeText(value, "");
		if (normalizedValue.isBlank()) {
			return null;
		}

		try {
			return RequirementLevel.valueOf(normalizedValue.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Valor inválido para " + fieldName + ". Usa LOW, MEDIUM o HIGH.");
		}
	}

	private String buildRecommendation(Plant plant, String season, WeatherData weatherData) {
		String seasonalTip = switch (season) {
			case "verano" -> "En verano revisa humedad del sustrato con más frecuencia.";
			case "otoño" -> "En otoño reduce ligeramente la frecuencia de riego.";
			case "invierno" -> "En invierno evita excesos de agua y corrientes frías.";
			default -> "En primavera puedes retomar fertilización suave si aplica.";
		};

		String weatherTip = buildWeatherTip(weatherData);
		if (!weatherTip.isBlank()) {
			return plant.getWateringRecommendation() + " " + plant.getLightRecommendation() + " " + seasonalTip + " " + weatherTip;
		}

		return plant.getWateringRecommendation() + " " + plant.getLightRecommendation() + " " + seasonalTip;
	}

	private String buildWeatherTip(WeatherData weatherData) {
		if (weatherData.temperature() == null && weatherData.humidity() == null && weatherData.precipitation() == null) {
			return "";
		}

		StringBuilder tipBuilder = new StringBuilder("Según el clima actual de tu ubicación: ");
		boolean hasSegment = false;

		if (weatherData.temperature() != null) {
			if (weatherData.temperature() >= 32) {
				tipBuilder.append("temperatura alta, aumenta vigilancia de riego");
				hasSegment = true;
			} else if (weatherData.temperature() <= 8) {
				tipBuilder.append("temperatura baja, reduce riego y evita corrientes frías");
				hasSegment = true;
			}
		}

		if (weatherData.humidity() != null) {
			if (weatherData.humidity() >= 80) {
				if (hasSegment) tipBuilder.append("; ");
				tipBuilder.append("humedad elevada, evita encharcamientos");
				hasSegment = true;
			} else if (weatherData.humidity() <= 30) {
				if (hasSegment) tipBuilder.append("; ");
				tipBuilder.append("humedad baja, considera aumentar humedad ambiental");
				hasSegment = true;
			}
		}

		if (weatherData.precipitation() != null && weatherData.precipitation() > 0) {
			if (hasSegment) tipBuilder.append("; ");
			tipBuilder.append("hay precipitación reciente, revisa drenaje antes de añadir más agua");
			hasSegment = true;
		}

		if (!hasSegment) {
			return "";
		}

		return tipBuilder.append('.').toString();
	}

	private record LocationResolution(String city, boolean geocodeFallback) {
	}

	private record WeatherResolution(WeatherData weatherData, boolean weatherFallback) {
	}
}
