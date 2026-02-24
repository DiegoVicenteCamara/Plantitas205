package com.plantitas.service;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenMeteoWeatherClient implements WeatherClient {

	private final RestClient restClient;

	public OpenMeteoWeatherClient(
		@Value("${weather.api.base-url:https://api.open-meteo.com}") String weatherBaseUrl,
		@Value("${weather.api.connect-timeout-ms:2500}") int connectTimeoutMs,
		@Value("${weather.api.read-timeout-ms:3500}") int readTimeoutMs
	) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
		requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

		this.restClient = RestClient
			.builder()
			.baseUrl(weatherBaseUrl)
			.requestFactory(requestFactory)
			.build();
	}

	@Override
	public WeatherData getCurrentWeather(double latitude, double longitude) {
		OpenMeteoResponse response = restClient
			.get()
			.uri(uriBuilder -> uriBuilder
				.path("/v1/forecast")
				.queryParam("latitude", latitude)
				.queryParam("longitude", longitude)
				.queryParam("timezone", "auto")
				.queryParam("current", "temperature_2m,relative_humidity_2m,precipitation,weather_code")
				.build())
			.retrieve()
			.body(OpenMeteoResponse.class);

		if (response == null || response.current() == null) {
			return WeatherData.empty();
		}

		return new WeatherData(
			response.current().temperature_2m(),
			response.current().relative_humidity_2m(),
			response.elevation(),
			response.current().precipitation(),
			response.current().weather_code()
		);
	}

	private record OpenMeteoResponse(
		Double elevation,
		CurrentWeather current
	) {
	}

	private record CurrentWeather(
		Double temperature_2m,
		Integer relative_humidity_2m,
		Double precipitation,
		Integer weather_code
	) {
	}
}
