import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { fetchPlantById } from "../services/plantCareService.js";

export default function PlantDetail() {
	const { id } = useParams();
	const navigate = useNavigate();
	const [plant, setPlant] = useState(null);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState("");

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
					<div className="result">
						{plant.image_url && (
							<img
								src={plant.image_url}
								alt={plant.common_name ?? plant.scientific_name}
								className="plant-detail-image"
							/>
						)}
						<p><strong>Nombre:</strong> {plant.common_name ?? "Sin nombre común"}</p>
						<p><strong>Nombre científico:</strong> {plant.scientific_name}</p>
						<p><strong>Riego:</strong> {plant.watering_recommendation}</p>
						<p><strong>Luz:</strong> {plant.light_recommendation}</p>
						<p><strong>¿Apta para interior?</strong> {plant.indoor_friendly ? "Sí" : "No"}</p>
					</div>
				)}
				<button type="button" onClick={() => navigate("/")}>Volver al Home</button>
			</section>
		</main>
	);
}