package com.plantitas.service;

import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenMeteoWeatherClient implements WeatherClient {

	private static final Logger log = LoggerFactory.getLogger(OpenMeteoWeatherClient.class);

	private final RestClient restClient;
	private final RoundedCoordinateCache<WeatherData> cache;

	public OpenMeteoWeatherClient(
		RestClient.Builder restClientBuilder,
		@Value("${weather.api.base-url:https://api.open-meteo.com}") String weatherBaseUrl,
		@Value("${weather.api.cache-ttl-ms:120000}") long cacheTtlMs,
		@Value("${weather.api.cache-precision:3}") int cachePrecision
	) {
		this.restClient = restClientBuilder
			.baseUrl(weatherBaseUrl)
			.build();
		this.cache = new RoundedCoordinateCache<>(cacheTtlMs, cachePrecision);
	}

	@Override
	@Retry(name = "weatherClient", fallbackMethod = "fallbackWeather")
	public WeatherData getCurrentWeather(double latitude, double longitude) {
		return cache.getOrCompute(latitude, longitude, () -> fetchCurrentWeather(latitude, longitude));
	}

	private WeatherData fallbackWeather(double latitude, double longitude, Exception exception) {
		log.warn("Weather retry exhausted for ({}, {}): {}", latitude, longitude, exception.getMessage());
		return WeatherData.empty();
	}

	private WeatherData fetchCurrentWeather(double latitude, double longitude) {
		log.debug("Fetching weather for ({}, {})", latitude, longitude);

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
			log.warn("Empty weather response for ({}, {})", latitude, longitude);
			return WeatherData.empty();
		}

		WeatherData data = new WeatherData(
			response.current().temperature_2m(),
			response.current().relative_humidity_2m(),
			response.elevation(),
			response.current().precipitation(),
			response.current().weather_code()
		);

		log.debug("Weather resolved: temp={}, humidity={}, alt={}", data.temperature(), data.humidity(), data.altitude());
		return data;
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
