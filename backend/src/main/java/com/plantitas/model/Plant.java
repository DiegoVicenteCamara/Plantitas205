package com.plantitas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "plants")
public class Plant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String slug;

	@Column(name = "common_name", nullable = false)
	private String commonName;

	@Column(name = "scientific_name", nullable = false)
	private String scientificName;

	@Column(name = "image_url")
	private String imageUrl;

	@Column(name = "indoor_friendly", nullable = false)
	private boolean indoorFriendly;

	@Column(name = "watering_recommendation", nullable = false)
	private String wateringRecommendation;

	@Column(name = "light_recommendation", nullable = false)
	private String lightRecommendation;

	@Column(name = "ideal_climate")
	private String idealClimate;

	@Column(name = "ideal_humidity")
	private String idealHumidity;

	@Column(name = "ideal_temperature_min")
	private Double idealTemperatureMin;

	@Column(name = "ideal_temperature_max")
	private Double idealTemperatureMax;

	@Column(name = "toxicidad")
	private String toxicidad;

	public Long getId() {
		return id;
	}

	public String getSlug() {
		return slug;
	}

	public String getCommonName() {
		return commonName;
	}

	public String getScientificName() {
		return scientificName;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public boolean isIndoorFriendly() {
		return indoorFriendly;
	}

	public String getWateringRecommendation() {
		return wateringRecommendation;
	}

	public String getLightRecommendation() {
		return lightRecommendation;
	}

	public String getIdealClimate() {
		return idealClimate;
	}

	public String getIdealHumidity() {
		return idealHumidity;
	}

	public Double getIdealTemperatureMin() {
		return idealTemperatureMin;
	}

	public Double getIdealTemperatureMax() {
		return idealTemperatureMax;
	}

	public String getToxicidad() {
		return toxicidad;
	}
}
