package com.plantitas.service;

import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenStreetMapReverseGeocodingClient implements ReverseGeocodingClient {

	private static final Logger log = LoggerFactory.getLogger(OpenStreetMapReverseGeocodingClient.class);

	private final RestClient restClient;
	private final RoundedCoordinateCache<String> cache;

	public OpenStreetMapReverseGeocodingClient(
		RestClient.Builder restClientBuilder,
		@Value("${geocoding.api.base-url:https://nominatim.openstreetmap.org}") String geocodingBaseUrl,
		@Value("${geocoding.api.cache-ttl-ms:300000}") long cacheTtlMs,
		@Value("${geocoding.api.cache-precision:3}") int cachePrecision
	) {
		this.restClient = restClientBuilder
			.baseUrl(geocodingBaseUrl)
			.defaultHeader("User-Agent", "PlantitasApp/1.0")
			.build();
		this.cache = new RoundedCoordinateCache<>(cacheTtlMs, cachePrecision);
	}

	@Override
	@Retry(name = "geocodingClient", fallbackMethod = "fallbackCity")
	public String resolveCity(double latitude, double longitude) {
		return cache.getOrCompute(latitude, longitude, () -> fetchCity(latitude, longitude));
	}

	private String fallbackCity(double latitude, double longitude, Exception exception) {
		log.warn("Geocoding retry exhausted for ({}, {}): {}", latitude, longitude, exception.getMessage());
		return null;
	}

	private String fetchCity(double latitude, double longitude) {
		log.debug("Reverse geocoding for ({}, {})", latitude, longitude);

		ReverseGeocodingResponse response = restClient
			.get()
			.uri(uriBuilder -> uriBuilder
				.path("/reverse")
				.queryParam("lat", latitude)
				.queryParam("lon", longitude)
				.queryParam("format", "jsonv2")
				.queryParam("accept-language", "es")
				.build())
			.retrieve()
			.body(ReverseGeocodingResponse.class);

		if (response == null || response.address() == null) {
			log.warn("Empty geocoding response for ({}, {})", latitude, longitude);
			return null;
		}

		Address address = response.address();
		String city = resolveCityFromAddress(address);
		log.debug("Geocoding resolved: city={}", city);
		return city;
	}

	private String resolveCityFromAddress(Address address) {
		if (hasText(address.city())) return address.city();
		if (hasText(address.town())) return address.town();
		if (hasText(address.village())) return address.village();
		if (hasText(address.municipality())) return address.municipality();
		return null;
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private record ReverseGeocodingResponse(Address address) {
	}

	private record Address(
		String city,
		String town,
		String village,
		String municipality
	) {
	}
}
