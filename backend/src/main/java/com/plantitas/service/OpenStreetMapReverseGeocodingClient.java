package com.plantitas.service;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenStreetMapReverseGeocodingClient implements ReverseGeocodingClient {

	private final RestClient restClient;

	public OpenStreetMapReverseGeocodingClient(
		@Value("${geocoding.api.base-url:https://nominatim.openstreetmap.org}") String geocodingBaseUrl,
		@Value("${geocoding.api.connect-timeout-ms:2500}") int connectTimeoutMs,
		@Value("${geocoding.api.read-timeout-ms:3500}") int readTimeoutMs
	) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
		requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

		this.restClient = RestClient
			.builder()
			.baseUrl(geocodingBaseUrl)
			.defaultHeader("User-Agent", "PlantitasApp/1.0")
			.requestFactory(requestFactory)
			.build();
	}

	@Override
	public String resolveCity(double latitude, double longitude) {
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
			return null;
		}

		Address address = response.address();
		if (hasText(address.city())) {
			return address.city();
		}
		if (hasText(address.town())) {
			return address.town();
		}
		if (hasText(address.village())) {
			return address.village();
		}
		if (hasText(address.municipality())) {
			return address.municipality();
		}

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
