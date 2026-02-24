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
			"40.4,-3.7",
			"verano",
			"Resumen",
			"Recomendación",
			true
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
			.andExpect(jsonPath("$.city").value("40.4,-3.7"));
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
			new PlantSearchItem(1L, "Aloe", "Aloe vera", "img1"),
			new PlantSearchItem(2L, "Ficus", "Ficus elastica", "img2")
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
}
