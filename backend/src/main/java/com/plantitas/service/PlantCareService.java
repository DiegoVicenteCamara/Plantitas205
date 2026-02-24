package com.plantitas.service;

import com.plantitas.dto.PlantCareRequest;
import com.plantitas.dto.PlantCareResponse;
import com.plantitas.dto.PlantSearchItem;
import com.plantitas.model.Plant;
import com.plantitas.repository.PlantRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class PlantCareService {

	private final PlantRepository plantRepository;

	public PlantCareService(PlantRepository plantRepository) {
		this.plantRepository = plantRepository;
	}

	public PlantCareResponse getPlantCare(PlantCareRequest request) {
		Plant plant = resolvePlant(request.plantId());
		String normalizedSeason = normalizeSeason(request.season());
		String city = resolveLocationForClimate(request);

		String summary = "Para " + plant.getCommonName() + " en " + city + " durante " + normalizedSeason + ".";
		String recommendation = buildRecommendation(plant, normalizedSeason);

		return new PlantCareResponse(
			plant.getCommonName(),
			city,
			normalizedSeason,
			summary,
			recommendation,
			plant.isIndoorFriendly()
		);
	}

	private String resolveLocationForClimate(PlantCareRequest request) {
		if (request.latitude() != null && request.longitude() != null) {
			return request.latitude() + "," + request.longitude();
		}

		return normalizeText(request.city(), "No indicada");
	}

	public List<PlantSearchItem> searchPlants(String query) {
		String normalizedQuery = normalizeText(query, "");
		if (normalizedQuery.isBlank()) {
			return List.of();
		}

		return plantRepository
			.findByCommonNameContainingIgnoreCaseOrScientificNameContainingIgnoreCase(normalizedQuery, normalizedQuery)
			.stream()
			.map(plant -> new PlantSearchItem(
				plant.getId(),
				plant.getCommonName(),
				plant.getScientificName(),
				plant.getImageUrl()
			))
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

	private String buildRecommendation(Plant plant, String season) {
		String seasonalTip = switch (season) {
			case "verano" -> "En verano revisa humedad del sustrato con más frecuencia.";
			case "otoño" -> "En otoño reduce ligeramente la frecuencia de riego.";
			case "invierno" -> "En invierno evita excesos de agua y corrientes frías.";
			default -> "En primavera puedes retomar fertilización suave si aplica.";
		};

		return plant.getWateringRecommendation() + " " + plant.getLightRecommendation() + " " + seasonalTip;
	}
}
