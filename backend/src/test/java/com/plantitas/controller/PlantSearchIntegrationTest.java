package com.plantitas.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PlantSearchIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void searchByWaterMedium_excludesLowWaterPlantsRegression() throws Exception {
		mockMvc.perform(get("/api/plants/search").param("water", "medium"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[*].common_name", not(hasItem("Sansevieria"))))
			.andExpect(jsonPath("$.data[*].common_name", not(hasItem("Zamioculca"))))
			.andExpect(jsonPath("$.data[*].common_name", hasItem("Poto")));
	}

	@Test
	void searchByCombinedFilters_returnsOnlyExactMatchesAcrossDimensions() throws Exception {
		mockMvc.perform(get("/api/plants/search")
				.param("category", "interior")
				.param("light", "low")
				.param("water", "low")
				.param("humidity", "low"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[*].common_name", hasItem("Zamioculca")))
			.andExpect(jsonPath("$.data[*].common_name", not(hasItem("Sansevieria"))))
			.andExpect(jsonPath("$.data[*].common_name", not(hasItem("Poto"))));
	}
}
