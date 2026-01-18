package com.plantitas.controller;

import com.plantitas.dto.PlantCareRequest;
import com.plantitas.dto.PlantCareResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PlantCareController {

	@PostMapping(path = "/plant-care", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public PlantCareResponse getPlantCare(@RequestBody PlantCareRequest request) {
		String summary = "Evaluación pendiente con API externa";
		String recommendation = "Completar integración con la API de cuidados y la API meteorológica.";
		return new PlantCareResponse(request.plantId(), request.city(), request.season(), summary, recommendation, true);
	}
}
