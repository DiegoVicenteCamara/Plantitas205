package com.plantitas.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.plantitas.dto.PlantCareResponse;
import com.plantitas.dto.PlantDetailResponse;
import com.plantitas.dto.PlantSearchItem;
import com.plantitas.service.PlantCareService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlantController.class)
class PlantControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private PlantCareService plantCareService;

	@Test
	void postPlantCare_returnsOkWhenRequestIsValid() throws Exception {
		PlantCareResponse response = new PlantCareResponse(
			"Monstera",
			"Madrid",
			"verano",
			"Resumen",
			"Recomendación",
			true,
			23.1,
			60,
			667.0,
			"full"
		);
		when(plantCareService.getPlantCare(any())).thenReturn(response);

		mockMvc.perform(post("/api/plant-care")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "plantId": "monstera",
					  "city": "Madrid",
					  "latitude": 40.4,
					  "longitude": -3.7,
					  "season": "verano"
					}
				"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.plantId").value("Monstera"))
					.andExpect(jsonPath("$.city").value("Madrid"))
					.andExpect(jsonPath("$.dataQuality").value("full"));
	}

	@Test
	void postPlantCare_returnsBadRequestWhenCoordinatesAreIncomplete() throws Exception {
		mockMvc.perform(post("/api/plant-care")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "plantId": "monstera",
					  "city": "Madrid",
					  "latitude": 40.4,
					  "season": "verano"
					}
				"""))
			.andExpect(status().isBadRequest());

		verify(plantCareService, never()).getPlantCare(any());
	}

	@Test
	void postPlantCare_returnsBadRequestWhenServiceThrowsIllegalArgument() throws Exception {
		when(plantCareService.getPlantCare(any())).thenThrow(new IllegalArgumentException("Planta inválida"));

		mockMvc.perform(post("/api/plant-care")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "plantId": "unknown",
					  "city": "Madrid",
					  "season": "verano"
					}
				"""))
			.andExpect(status().isBadRequest());
	}

	@Test
	void getPlantsSearch_returnsMappedList() throws Exception {
		List<PlantSearchItem> items = List.of(
			new PlantSearchItem(1L, "Aloe", "Aloe barbadensis"),
			new PlantSearchItem(2L, "Ficus", "Ficus elastica")
		);
		when(plantCareService.searchPlants("al")).thenReturn(items);

		mockMvc.perform(get("/api/plants/search").param("q", "al"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data", hasSize(2)))
			.andExpect(jsonPath("$.data[0].common_name").value("Aloe"));
	}

	@Test
	void getPlantSuggestions_returnsSuggestionList() throws Exception {
		when(plantCareService.suggestPlantNames("mo")).thenReturn(List.of("Monstera", "Moss"));

		mockMvc.perform(get("/api/plants/suggestions").param("prefix", "mo"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[0]").value("Monstera"));
	}

	@Test
	void getPlantById_returnsPlantDetailWhenPlantExists() throws Exception {
		PlantDetailResponse detail = new PlantDetailResponse(
			1L,
			"monstera",
			"Monstera",
			"Monstera deliciosa",
			"img1",
			true,
			"Riego moderado.",
			"Luz indirecta brillante.",
			"Tropical húmedo",
			"20-28 °C",
			"60-80%"
		);
		when(plantCareService.getPlantById(1L)).thenReturn(detail);

		mockMvc.perform(get("/api/plants/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.slug").value("monstera"))
			.andExpect(jsonPath("$.common_name").value("Monstera"));
	}

	@Test
	void getPlantById_returnsNotFoundWhenPlantDoesNotExist() throws Exception {
		when(plantCareService.getPlantById(999L)).thenThrow(new IllegalArgumentException("No existe una planta con ese ID."));

		mockMvc.perform(get("/api/plants/999"))
			.andExpect(status().isNotFound());
	}
}
