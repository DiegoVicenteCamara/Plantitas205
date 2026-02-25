import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { fetchPlantById } from "../services/plantCareService.js";

export default function PlantDetail() {
	const { id } = useParams();
	const navigate = useNavigate();
	const [plant, setPlant] = useState(null);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState("");

	const toDisplayValue = (value) => {
		if (value === null || value === undefined) {
			return "Desconocido";
		}
		if (typeof value === "string") {
			const trimmedValue = value.trim();
			return trimmedValue ? trimmedValue : "Desconocido";
		}
		return String(value);
	};

	const toIndoorDisplay = (value) => {
		if (typeof value === "boolean") {
			return value ? "Sí" : "No";
		}
		return "Desconocido";
	};

	const buildClimateRecommendation = (plantData) => {
		if (!plantData) {
			return "Desconocido";
		}

		const idealClimate = toDisplayValue(plantData.ideal_climate);
		const idealTemperature = toDisplayValue(plantData.ideal_temperature);
		const idealHumidity = toDisplayValue(plantData.ideal_humidity);
		const watering = toDisplayValue(plantData.watering_recommendation);
		const light = toDisplayValue(plantData.light_recommendation);

		if (
			idealClimate === "Desconocido"
			&& idealTemperature === "Desconocido"
			&& idealHumidity === "Desconocido"
			&& watering === "Desconocido"
			&& light === "Desconocido"
		) {
			return "Desconocido";
		}

		return `Para un clima ${idealClimate}, mantén la planta cerca de ${idealTemperature} con humedad ${idealHumidity}. Prioriza ${watering.toLowerCase()} y ${light.toLowerCase()}.`;
	};

	useEffect(() => {
		let cancelled = false;

		const loadPlant = async () => {
			setLoading(true);
			setError("");
			try {
				const data = await fetchPlantById(id);
				if (!cancelled) {
					setPlant(data);
				}
			} catch (loadError) {
				if (!cancelled) {
					setPlant(null);
					setError(loadError.message === "PLANT_NOT_FOUND"
						? "La planta no existe."
						: "No se pudo cargar la información de la planta.");
				}
			} finally {
				if (!cancelled) {
					setLoading(false);
				}
			}
		};

		loadPlant();

		return () => {
			cancelled = true;
		};
	}, [id]);

	return (
		<main className="content">
			<section className="card plant-detail-card">
				<h2>Detalle de planta</h2>
				{loading && <p>Cargando planta...</p>}
				{error && <p className="error">{error}</p>}
				{plant && !loading && (
					<div className="plant-detail-layout">
						<div className="plant-detail-media">
							{plant.image_url ? (
								<img
									src={plant.image_url}
									alt={toDisplayValue(plant.common_name ?? plant.scientific_name)}
									className="plant-detail-image"
								/>
							) : (
								<div className="plant-detail-image plant-detail-image--placeholder" aria-hidden="true">
									🌿
								</div>
							)}
						</div>

						<div className="plant-detail-main">
							<div className="plant-detail-title-block">
								<p><strong>Nombre:</strong> {toDisplayValue(plant.common_name)}</p>
								<p><strong>Nombre científico:</strong> {toDisplayValue(plant.scientific_name)}</p>
								<p><strong>¿Apta para interior?</strong> {toIndoorDisplay(plant.indoor_friendly)}</p>
							</div>

							<div className="plant-detail-badges" aria-label="Requerimientos clave de cuidado">
								<article className="plant-detail-badge">
									<span className="plant-detail-badge__icon" aria-hidden="true">💧</span>
									<div>
										<p className="plant-detail-badge__label">Riego</p>
										<p className="plant-detail-badge__value">{toDisplayValue(plant.watering_recommendation)}</p>
									</div>
								</article>
								<article className="plant-detail-badge">
									<span className="plant-detail-badge__icon" aria-hidden="true">☀️</span>
									<div>
										<p className="plant-detail-badge__label">Luz</p>
										<p className="plant-detail-badge__value">{toDisplayValue(plant.light_recommendation)}</p>
									</div>
								</article>
								<article className="plant-detail-badge">
									<span className="plant-detail-badge__icon" aria-hidden="true">🌡️</span>
									<div>
										<p className="plant-detail-badge__label">Temperatura</p>
										<p className="plant-detail-badge__value">{toDisplayValue(plant.ideal_temperature)}</p>
									</div>
								</article>
							</div>

							<section className="plant-detail-climate-highlight" aria-label="Recomendación climática">
								<h3>Recomendación climática</h3>
								<p>{buildClimateRecommendation(plant)}</p>
							</section>
						</div>
						<aside className="plant-detail-conditions">
							<h3>Condiciones ideales</h3>
							<p><strong>Clima:</strong> {toDisplayValue(plant.ideal_climate)}</p>
							<p><strong>Temperatura ideal:</strong> {toDisplayValue(plant.ideal_temperature)}</p>
							<p><strong>Humedad ideal:</strong> {toDisplayValue(plant.ideal_humidity)}</p>
							<p><strong>Toxicidad:</strong> {toDisplayValue(plant.toxicidad)}</p>
						</aside>
					</div>
				)}
				<button type="button" className="btn btn--secondary plant-detail-back-button" onClick={() => navigate("/")}>Volver al Home</button>
			</section>
		</main>
	);
}