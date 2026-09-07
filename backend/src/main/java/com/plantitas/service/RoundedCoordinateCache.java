package com.plantitas.service;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

class RoundedCoordinateCache<T> {

	private final long ttlMillis;
	private final int precision;
	private final Map<String, CacheEntry<T>> cacheEntries = new ConcurrentHashMap<>();

	RoundedCoordinateCache(long ttlMillis, int precision) {
		this.ttlMillis = Math.max(1L, ttlMillis);
		this.precision = Math.max(0, precision);
	}

	T getOrCompute(double latitude, double longitude, Supplier<T> supplier) {
		long now = System.currentTimeMillis();
		String key = buildKey(latitude, longitude);
		CacheEntry<T> cachedEntry = cacheEntries.get(key);

		if (cachedEntry != null && cachedEntry.expiresAtMillis() > now) {
			return cachedEntry.value();
		}

		T computedValue = supplier.get();
		cacheEntries.put(key, new CacheEntry<>(computedValue, now + ttlMillis));
		purgeExpiredEntries();
		return computedValue;
	}

	private void purgeExpiredEntries() {
		long now = System.currentTimeMillis();
		cacheEntries.entrySet().removeIf(entry -> entry.getValue().expiresAtMillis() <= now);
	}
	private String buildKey(double latitude, double longitude) {
		double roundedLatitude = round(latitude);
		double roundedLongitude = round(longitude);
		String pattern = "%1$." + precision + "f|%2$." + precision + "f";
		return String.format(Locale.ROOT, pattern, roundedLatitude, roundedLongitude);
	}

	private double round(double value) {
		double factor = Math.pow(10, precision);
		return Math.round(value * factor) / factor;
	}

	private record CacheEntry<T>(
		T value,
		long expiresAtMillis
	) {
	}
}
