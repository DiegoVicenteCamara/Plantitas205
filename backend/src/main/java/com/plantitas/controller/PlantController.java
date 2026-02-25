package com.plantitas.controller;

import com.plantitas.dto.PlantCareRequest;
import com.plantitas.dto.PlantCareResponse;
import com.plantitas.dto.PlantDetailResponse;
import com.plantitas.dto.PlantSearchResponse;
import com.plantitas.service.PlantCareService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class PlantController {

	private final PlantCareService plantCareService;

	public PlantController(PlantCareService plantCareService) {
		this.plantCareService = plantCareService;
	}

	@PostMapping("/plant-care")
	public PlantCareResponse getPlantCare(@RequestBody PlantCareRequest request) {
		try {
			validateLocationRequest(request);
			return plantCareService.getPlantCare(request);
		} catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
		}
	}

	private void validateLocationRequest(PlantCareRequest request) {
		boolean hasLatitude = request.latitude() != null;
		boolean hasLongitude = request.longitude() != null;

		if (hasLatitude != hasLongitude) {
			throw new IllegalArgumentException("Debes enviar latitude y longitude juntas.");
		}
	}

	@GetMapping("/plants/search")
	public PlantSearchResponse searchPlants(
		@RequestParam(value = "q", required = false) String query,
		@RequestParam(value = "category", required = false) String category,
		@RequestParam(value = "light", required = false) String light,
		@RequestParam(value = "water", required = false) String water,
		@RequestParam(value = "humidity", required = false) String humidity
	) {
		try {
			return new PlantSearchResponse(plantCareService.searchPlants(query, category, light, water, humidity));
		} catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
		}
	}

	@GetMapping("/plants/suggestions")
	public List<String> getPlantSuggestions(@RequestParam("prefix") String prefix) {
		return plantCareService.suggestPlantNames(prefix);
	}

	@GetMapping("/plants/{id}")
	public PlantDetailResponse getPlantById(@PathVariable Long id) {
		try {
			return plantCareService.getPlantById(id);
		} catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
		}
	}
}
