package com.plantitas.service;

public record WeatherData(
	Double temperature,
	Integer humidity,
	Double altitude,
	Double precipitation,
	Integer weatherCode
) {
	public static WeatherData empty() {
		return new WeatherData(null, null, null, null, null);
	}
}
