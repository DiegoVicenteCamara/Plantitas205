package com.plantitas.service;

public interface WeatherClient {

	WeatherData getCurrentWeather(double latitude, double longitude);
}
