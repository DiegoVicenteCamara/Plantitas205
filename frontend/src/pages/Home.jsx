import { useEffect, useState } from "react";
import { fetchPlantCare, fetchPlantSuggestions, searchPlants } from "../services/plantCareService.js";
import CitySelector from "../components/CitySelector.jsx";

export default function Home() {
	const [plantId, setPlantId] = useState("");
	const [city, setCity] = useState("");
	const [season, setSeason] = useState("primavera");
	const [result, setResult] = useState(null);
	const [loading, setLoading] = useState(false);
	const [error, setError] = useState("");
	const [searchQuery, setSearchQuery] = useState("");
	const [searchResult, setSearchResult] = useState(null);
	const [searchLoading, setSearchLoading] = useState(false);
	const [searchError, setSearchError] = useState("");
	const [suggestions, setSuggestions] = useState([]);
	const [showSuggestions, setShowSuggestions] = useState(false);

	useEffect(() => {
		const prefix = searchQuery.trim();
		if (!prefix || !showSuggestions) {
			setSuggestions([]);
			return;
		}

		let cancelled = false;
		const timeoutId = setTimeout(async () => {
			try {
				const response = await fetchPlantSuggestions(prefix);
				if (!cancelled) {
					setSuggestions(response);
				}
			} catch (error) {
				if (!cancelled) {
					setSuggestions([]);
				}
			}
		}, 180);

		return () => {
			cancelled = true;
			clearTimeout(timeoutId);
		};
	}, [searchQuery, showSuggestions]);

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

	const handleSearch = async (event) => {
		event.preventDefault();
		if (!searchQuery.trim()) {
			return;
		}
		setShowSuggestions(false);
		setSearchError("");
		setSearchLoading(true);
		try {
			const response = await searchPlants(searchQuery.trim());
			setSearchResult(response);
		} catch (err) {
			setSearchError("No se pudo buscar en la base local.");
			setSearchResult(null);
		} finally {
			setSearchLoading(false);
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
				<h2>Búsqueda en catálogo local</h2>
				<form onSubmit={handleSearch} className="form">
					<label>
						Nombre o especie
						<input
							type="text"
							value={searchQuery}
							onChange={(event) => {
								setSearchQuery(event.target.value);
								setShowSuggestions(true);
							}}
							onFocus={() => setShowSuggestions(true)}
							placeholder="ej: fern"
							required
						/>
					</label>
					{showSuggestions && suggestions.length > 0 && (
						<ul className="suggestions-list">
							{suggestions.map((name) => (
								<li key={name}>
									<button
										type="button"
										className="suggestion-item"
										onMouseDown={(event) => event.preventDefault()}
										onClick={() => {
											setSearchQuery(name);
											setShowSuggestions(false);
											setSuggestions([]);
										}}
									>
										{name}
									</button>
								</li>
							))}
						</ul>
					)}
					<button type="submit" disabled={searchLoading}>
						{searchLoading ? "Buscando..." : "Buscar"}
					</button>
				</form>

				{searchError && <p className="error">{searchError}</p>}
				{searchResult?.data?.length === 0 && !searchError && (
					<p>No se encontraron plantas.</p>
				)}
				{searchResult?.data?.length > 0 && (
					<div className="plant-list">
						{searchResult.data.slice(0, 8).map((plant) => (
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
