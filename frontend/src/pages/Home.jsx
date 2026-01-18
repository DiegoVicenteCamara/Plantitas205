import { useState } from "react";
import { fetchPlantCare } from "../services/plantCareService.js";
import CitySelector from "../components/CitySelector.jsx";

export default function Home() {
	const [plantId, setPlantId] = useState("");
	const [city, setCity] = useState("");
	const [season, setSeason] = useState("primavera");
	const [result, setResult] = useState(null);
	const [loading, setLoading] = useState(false);
	const [error, setError] = useState("");

	const handleSubmit = async (event) => {
		event.preventDefault();
		setError("");
		setLoading(true);
		try {
			const response = await fetchPlantCare({ plantId, city, season });
			setResult(response);
		} catch (err) {
			setError("No se pudo obtener la recomendación.");
			setResult(null);
		} finally {
			setLoading(false);
		}
	};

	return (
		<main className="content">
			<section className="card">
				<h2>Consulta de cuidado</h2>
				<form onSubmit={handleSubmit} className="form">
					<label>
						ID o especie de planta
						<input
							type="text"
							value={plantId}
							onChange={(event) => setPlantId(event.target.value)}
							placeholder="ej: monstera"
							required
						/>
					</label>
					<CitySelector value={city} onChange={setCity} />
					<label>
						Época del año
						<select value={season} onChange={(event) => setSeason(event.target.value)}>
							<option value="primavera">Primavera</option>
							<option value="verano">Verano</option>
							<option value="otono">Otoño</option>
							<option value="invierno">Invierno</option>
						</select>
					</label>
					<button type="submit" disabled={loading}>
						{loading ? "Consultando..." : "Evaluar"}
					</button>
				</form>
			</section>

			<section className="card">
				<h2>Resultado</h2>
				{error && <p className="error">{error}</p>}
				{!result && !error && <p>Completa el formulario para obtener recomendaciones.</p>}
				{result && (
					<div className="result">
						<p><strong>Planta:</strong> {result.plantId}</p>
						<p><strong>Ciudad:</strong> {result.city}</p>
						<p><strong>Época:</strong> {result.season}</p>
						<p><strong>Resumen:</strong> {result.summary}</p>
						<p><strong>Recomendación:</strong> {result.recommendation}</p>
						<p><strong>¿Interior?</strong> {result.indoorFriendly ? "Sí" : "No"}</p>
					</div>
				)}
			</section>
		</main>
	);
}
