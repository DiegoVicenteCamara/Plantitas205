package com.plantitas.controller;

import com.plantitas.dto.PlantCareRequest;
import com.plantitas.dto.PlantCareResponse;
import com.plantitas.dto.PlantSearchResponse;
import com.plantitas.service.PlantCareService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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
			return plantCareService.getPlantCare(request);
		} catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
		}
	}

	@GetMapping("/plants/search")
	public PlantSearchResponse searchPlants(@RequestParam("q") String query) {
		return new PlantSearchResponse(plantCareService.searchPlants(query));
	}

	@GetMapping("/plants/suggestions")
	public List<String> getPlantSuggestions(@RequestParam("prefix") String prefix) {
		return plantCareService.suggestPlantNames(prefix);
	}
}
