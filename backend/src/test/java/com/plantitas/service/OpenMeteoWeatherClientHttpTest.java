package com.plantitas.service;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OpenMeteoWeatherClientHttpTest {

	@Test
	void getCurrentWeather_sendsExpectedQueryParams_andUsesCacheForRoundedCoordinates() {
		RestClient.Builder restClientBuilder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
		OpenMeteoWeatherClient client = new OpenMeteoWeatherClient(
			restClientBuilder,
			"https://api.open-meteo.com",
			120000,
			3
		);

		server.expect(requestTo(allOf(
			containsString("/v1/forecast?"),
			containsString("latitude=40.4168"),
			containsString("longitude=-3.7038"),
			containsString("timezone=auto"),
			containsString("current=temperature_2m,relative_humidity_2m,precipitation,weather_code")
		)))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(
				"""
				{
				  "elevation": 667.0,
				  "current": {
				    "temperature_2m": 22.4,
				    "relative_humidity_2m": 52,
				    "precipitation": 0.0,
				    "weather_code": 1
				  }
				}
				""",
				MediaType.APPLICATION_JSON
			));

		WeatherData first = client.getCurrentWeather(40.4168, -3.7038);
		WeatherData second = client.getCurrentWeather(40.41684, -3.70384);

		assertNotNull(first);
		assertEquals(22.4, first.temperature());
		assertEquals(52, first.humidity());
		assertEquals(667.0, first.altitude());
		assertEquals(first, second);
		server.verify();
	}
}
