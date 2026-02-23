import { useEffect, useState } from "react";
import { fetchPlantCare, searchPlants } from "../services/plantCareService.js";
import MapSelector from "../components/MapSelector.jsx";
import { useNavigate } from "react-router-dom";

export default function Home() {
	const navigate = useNavigate();
	const [plantId, setPlantId] = useState("");
	const [location, setLocation] = useState(null);
	const [season, setSeason] = useState("primavera");
	const [result, setResult] = useState(null);
	const [loading, setLoading] = useState(false);
	const [error, setError] = useState("");
	const [searchQuery, setSearchQuery] = useState("");
	const [searchResults, setSearchResults] = useState([]);
	const [searchLoading, setSearchLoading] = useState(false);
	const [searchError, setSearchError] = useState("");
	const [showResults, setShowResults] = useState(false);

	useEffect(() => {
		const query = searchQuery.trim();
		if (!query) {
			setSearchResults([]);
			setSearchLoading(false);
			setSearchError("");
			return;
		}

		let cancelled = false;
		const timeoutId = setTimeout(async () => {
			setSearchLoading(true);
			setSearchError("");
			try {
				const response = await searchPlants(query);
				if (!cancelled) {
					setSearchResults(response.data ?? []);
				}
			} catch (error) {
				if (!cancelled) {
					setSearchResults([]);
					setSearchError("No se pudo buscar en la base local.");
				}
			} finally {
				if (!cancelled) {
					setSearchLoading(false);
				}
			}
		}, 300);

		return () => {
			cancelled = true;
			clearTimeout(timeoutId);
		};
	}, [searchQuery]);

	const handleSubmit = async (event) => {
		event.preventDefault();
		if (!location) {
			setError("Selecciona una ubicación en el mapa.");
			setResult(null);
			return;
		}
		setError("");
		setLoading(true);
		try {
			const city = `${location.lat.toFixed(6)},${location.lng.toFixed(6)}`;
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
					<MapSelector value={location} onChange={setLocation} />
					<label>
						Época del año
						<select value={season} onChange={(event) => setSeason(event.target.value)}>
							<option value="primavera">Primavera</option>
							<option value="verano">Verano</option>
							<option value="otono">Otoño</option>
							<option value="invierno">Invierno</option>
						</select>
					</label>
					<button type="submit" disabled={loading || !location}>
						{loading ? "Consultando..." : "Evaluar"}
					</button>
				</form>
			</section>

			<section className="card">
				<h2>Búsqueda en catálogo local</h2>
				<div className="form">
					<label>
						Nombre o especie
						<input
							type="text"
							value={searchQuery}
							onChange={(event) => {
								setSearchQuery(event.target.value);
								setShowResults(true);
							}}
							onFocus={() => setShowResults(true)}
							onBlur={() => {
								setTimeout(() => setShowResults(false), 120);
							}}
							placeholder="ej: fern"
							required
						/>
					</label>
					{showResults && searchResults.length > 0 && (
						<ul className="suggestions-list">
							{searchResults.map((plant) => (
								<li key={plant.id}>
									<button
										type="button"
										className="suggestion-item"
										onMouseDown={(event) => event.preventDefault()}
										onClick={() => {
											setShowResults(false);
											navigate(`/planta/${plant.id}`);
										}}
									>
										{plant.common_name ?? "Sin nombre común"}
										{plant.scientific_name ? ` · ${plant.scientific_name}` : ""}
									</button>
								</li>
							))}
						</ul>
					)}
					{searchLoading && <p>Buscando...</p>}
				</div>

				{searchError && <p className="error">{searchError}</p>}
				{searchQuery.trim() && !searchLoading && searchResults.length === 0 && !searchError && (
					<p>No se encontraron plantas.</p>
				)}
				{searchResults.length > 0 && (
					<div className="plant-list">
						{searchResults.slice(0, 8).map((plant) => (
							<article key={plant.id} className="plant-card">
								{plant.image_url && (
									<img src={plant.image_url} alt={plant.common_name ?? plant.scientific_name} />
								)}
								<div>
									<p className="plant-title">{plant.common_name ?? "Sin nombre común"}</p>
									<p className="plant-meta">{plant.scientific_name}</p>
								</div>
							</article>
						))}
					</div>
				)}
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
