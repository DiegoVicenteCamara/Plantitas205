package com.plantitas.service;

import com.plantitas.dto.PlantCareRequest;
import com.plantitas.dto.PlantCareResponse;
import com.plantitas.dto.PlantDetailResponse;
import com.plantitas.dto.PlantSearchItem;
import com.plantitas.model.Plant;
import com.plantitas.repository.PlantRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class PlantCareService {

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
		Plant plant = resolvePlant(request.plantId());
		String normalizedSeason = normalizeSeason(request.season());
		String city = resolveLocationForClimate(request);
		WeatherData weatherData = resolveWeatherData(request);

		String summary = "Para " + plant.getCommonName() + " en " + city + " durante " + normalizedSeason + ".";
		String recommendation = buildRecommendation(plant, normalizedSeason, weatherData);

		return new PlantCareResponse(
			plant.getCommonName(),
			city,
			normalizedSeason,
			summary,
			recommendation,
			plant.isIndoorFriendly(),
			weatherData.temperature(),
			weatherData.humidity(),
			weatherData.altitude()
		);
	}

	private WeatherData resolveWeatherData(PlantCareRequest request) {
		if (request.latitude() == null || request.longitude() == null) {
			return WeatherData.empty();
		}

		try {
			WeatherData weatherData = weatherClient.getCurrentWeather(request.latitude(), request.longitude());
			return weatherData == null ? WeatherData.empty() : weatherData;
		} catch (RuntimeException exception) {
			return WeatherData.empty();
		}
	}

	private String resolveLocationForClimate(PlantCareRequest request) {
		if (request.latitude() != null && request.longitude() != null) {
			String fallbackCity = normalizeText(request.city(), "Ubicación seleccionada");
			try {
				String resolvedCity = reverseGeocodingClient.resolveCity(request.latitude(), request.longitude());
				return normalizeText(resolvedCity, fallbackCity);
			} catch (RuntimeException exception) {
				return fallbackCity;
			}
		}

		return normalizeText(request.city(), "No indicada");
	}

	public List<PlantSearchItem> searchPlants(String query) {
		String normalizedQuery = normalizeText(query, "");
		if (normalizedQuery.isBlank()) {
			return List.of();
		}

		return plantRepository
			.findTop10ByCommonNameContainingIgnoreCaseOrderByCommonNameAsc(normalizedQuery)
			.stream()
			.map(plant -> new PlantSearchItem(plant.getId(), plant.getCommonName()))
			.toList();
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
			.orElseThrow(() -> new IllegalArgumentException("No existe una planta con ese ID."));

		return new PlantDetailResponse(
			plant.getId(),
			plant.getSlug(),
			plant.getCommonName(),
			plant.getScientificName(),
			plant.getImageUrl(),
			plant.isIndoorFriendly(),
			plant.getWateringRecommendation(),
			plant.getLightRecommendation()
		);
	}

	private Plant resolvePlant(String plantId) {
		String normalizedPlantId = normalizeText(plantId, "");
		if (normalizedPlantId.isBlank()) {
			throw new IllegalArgumentException("El identificador de la planta es obligatorio.");
		}

		return tryFindByNumericId(normalizedPlantId)
			.or(() -> plantRepository.findBySlugIgnoreCase(normalizedPlantId))
			.or(() -> plantRepository
				.findByCommonNameContainingIgnoreCaseOrScientificNameContainingIgnoreCase(normalizedPlantId, normalizedPlantId)
				.stream()
				.findFirst())
			.orElseThrow(() -> new IllegalArgumentException("No existe una planta de prueba para el valor indicado."));
	}

	private java.util.Optional<Plant> tryFindByNumericId(String candidate) {
		try {
			long id = Long.parseLong(candidate);
			return plantRepository.findById(id);
		} catch (NumberFormatException exception) {
			return java.util.Optional.empty();
		}
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
				if (hasSegment) {
					tipBuilder.append("; ");
				}
				tipBuilder.append("humedad elevada, evita encharcamientos");
				hasSegment = true;
			} else if (weatherData.humidity() <= 30) {
				if (hasSegment) {
					tipBuilder.append("; ");
				}
				tipBuilder.append("humedad baja, considera aumentar humedad ambiental");
				hasSegment = true;
			}
		}

		if (weatherData.precipitation() != null && weatherData.precipitation() > 0) {
			if (hasSegment) {
				tipBuilder.append("; ");
			}
			tipBuilder.append("hay precipitación reciente, revisa drenaje antes de añadir más agua");
			hasSegment = true;
		}

		if (!hasSegment) {
			return "";
		}

		return tipBuilder.append('.').toString();
	}
}
