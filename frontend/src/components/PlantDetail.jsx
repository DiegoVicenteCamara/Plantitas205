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

	const toTemperatureRangeDisplay = (min, max) => {
		if (min === null && max === null) {
			return "Desconocido";
		}
		if (min !== null && max !== null) {
			return `${min}-${max} °C`;
		}
		if (min !== null) {
			return `Desde ${min} °C`;
		}
		return `Hasta ${max} °C`;
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
			<section className="card">
				<h2>Detalle de planta</h2>
				{loading && <p>Cargando planta...</p>}
				{error && <p className="error">{error}</p>}
				{plant && !loading && (
					<div className="plant-detail-layout">
						<div className="plant-detail-main">
							<p><strong>Nombre:</strong> {toDisplayValue(plant.common_name)}</p>
							<p><strong>Nombre científico:</strong> {toDisplayValue(plant.scientific_name)}</p>
							{plant.image_url && (
								<img
									src={plant.image_url}
									alt={toDisplayValue(plant.common_name ?? plant.scientific_name)}
									className="plant-detail-image"
								/>
							)}
							<p><strong>Luz:</strong> {toDisplayValue(plant.light_recommendation)}</p>
							<p><strong>Riego:</strong> {toDisplayValue(plant.watering_recommendation)}</p>
							<p><strong>¿Apta para interior?</strong> {toIndoorDisplay(plant.indoor_friendly)}</p>
						</div>
						<aside className="plant-detail-conditions">
							<h3>Condiciones ideales</h3>
							<p><strong>Clima:</strong> {toDisplayValue(plant.ideal_climate)}</p>
							<p><strong>Temperatura ideal:</strong> {toDisplayValue(plant.ideal_temperature)}</p>
							<p><strong>Humedad ideal:</strong> {toDisplayValue(plant.ideal_humidity)}</p>
							<p><strong>Rango térmico ideal:</strong> {toTemperatureRangeDisplay(plant.ideal_temperature_min, plant.ideal_temperature_max)}</p>
							<p><strong>Toxicidad:</strong> {toDisplayValue(plant.toxicidad)}</p>
						</aside>
					</div>
				)}
				<button type="button" onClick={() => navigate("/")}>Volver al Home</button>
			</section>
		</main>
	);
}