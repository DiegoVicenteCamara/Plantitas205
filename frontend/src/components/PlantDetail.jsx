import { useNavigate, useParams } from "react-router-dom";
import usePlantDetail from "../hooks/usePlantDetail.js";
import { toDisplayValue, toIndoorDisplay } from "../utils/display.js";

function buildClimateRecommendation(plantData) {
	if (!plantData) return "Desconocido";

	const fields = [
		toDisplayValue(plantData.ideal_climate),
		toDisplayValue(plantData.ideal_temperature),
		toDisplayValue(plantData.ideal_humidity),
		toDisplayValue(plantData.watering_recommendation),
		toDisplayValue(plantData.light_recommendation),
	];

	if (fields.every((f) => f === "Desconocido")) return "Desconocido";

	const [climate, temp, humidity, watering, light] = fields;
	return `Para un clima ${climate}, mantén la planta cerca de ${temp} con humedad ${humidity}. Prioriza ${watering.toLowerCase()} y ${light.toLowerCase()}.`;
}

export default function PlantDetail() {
	const { id } = useParams();
	const navigate = useNavigate();
	const { plant, loading, error } = usePlantDetail(id);

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
				<button type="button" className="btn btn--secondary plant-detail-back-button" onClick={() => navigate("/")}>
					Volver al Home
				</button>
			</section>
		</main>
	);
}
