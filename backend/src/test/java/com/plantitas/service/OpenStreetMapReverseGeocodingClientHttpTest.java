package com.plantitas.service;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OpenStreetMapReverseGeocodingClientHttpTest {

	@Test
	void resolveCity_sendsExpectedQueryParams_andUsesCacheForRoundedCoordinates() {
		RestClient.Builder restClientBuilder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
		OpenStreetMapReverseGeocodingClient client = new OpenStreetMapReverseGeocodingClient(
			restClientBuilder,
			"https://nominatim.openstreetmap.org",
			120000,
			3
		);

		server.expect(requestTo(allOf(
			containsString("/reverse?"),
			containsString("lat=40.4168"),
			containsString("lon=-3.7038"),
			containsString("format=jsonv2"),
			containsString("accept-language=es")
		)))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(
				"""
				{
				  "address": {
				    "city": "Madrid"
				  }
				}
				""",
				MediaType.APPLICATION_JSON
			));

		String first = client.resolveCity(40.4168, -3.7038);
		String second = client.resolveCity(40.41682, -3.70383);

		assertEquals("Madrid", first);
		assertEquals("Madrid", second);
		server.verify();
	}

	@Test
	void resolveCity_returnsNullWhenNoKnownAddressFieldExists() {
		RestClient.Builder restClientBuilder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
		OpenStreetMapReverseGeocodingClient client = new OpenStreetMapReverseGeocodingClient(
			restClientBuilder,
			"https://nominatim.openstreetmap.org",
			120000,
			3
		);

		server.expect(requestTo(containsString("/reverse?")))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(
				"""
				{
				  "address": {
				    "state": "Comunidad de Madrid"
				  }
				}
				""",
				MediaType.APPLICATION_JSON
			));

		String result = client.resolveCity(40.4168, -3.7038);

		assertNull(result);
		server.verify();
	}
}
