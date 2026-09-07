package com.plantitas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlantDetailResponse(
	Long id,
	String slug,
	@JsonProperty("common_name") String commonName,
	@JsonProperty("scientific_name") String scientificName,
	@JsonProperty("image_url") String imageUrl,
	@JsonProperty("indoor_friendly") boolean indoorFriendly,
	@JsonProperty("watering_recommendation") String wateringRecommendation,
	@JsonProperty("light_recommendation") String lightRecommendation,
	@JsonProperty("ideal_climate") String idealClimate,
	@JsonProperty("ideal_temperature") String idealTemperature,
	@JsonProperty("ideal_humidity") String idealHumidity,
	String toxicidad
) {
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Long id;
		private String slug;
		private String commonName;
		private String scientificName;
		private String imageUrl;
		private boolean indoorFriendly;
		private String wateringRecommendation;
		private String lightRecommendation;
		private String idealClimate;
		private String idealTemperature;
		private String idealHumidity;
		private String toxicidad;

		public Builder id(Long id) { this.id = id; return this; }
		public Builder slug(String slug) { this.slug = slug; return this; }
		public Builder commonName(String commonName) { this.commonName = commonName; return this; }
		public Builder scientificName(String scientificName) { this.scientificName = scientificName; return this; }
		public Builder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
		public Builder indoorFriendly(boolean indoorFriendly) { this.indoorFriendly = indoorFriendly; return this; }
		public Builder wateringRecommendation(String wateringRecommendation) { this.wateringRecommendation = wateringRecommendation; return this; }
		public Builder lightRecommendation(String lightRecommendation) { this.lightRecommendation = lightRecommendation; return this; }
		public Builder idealClimate(String idealClimate) { this.idealClimate = idealClimate; return this; }
		public Builder idealTemperature(String idealTemperature) { this.idealTemperature = idealTemperature; return this; }
		public Builder idealHumidity(String idealHumidity) { this.idealHumidity = idealHumidity; return this; }
		public Builder toxicidad(String toxicidad) { this.toxicidad = toxicidad; return this; }

		public PlantDetailResponse build() {
			return new PlantDetailResponse(
				id, slug, commonName, scientificName, imageUrl,
				indoorFriendly, wateringRecommendation, lightRecommendation,
				idealClimate, idealTemperature, idealHumidity, toxicidad
			);
		}
	}
}
