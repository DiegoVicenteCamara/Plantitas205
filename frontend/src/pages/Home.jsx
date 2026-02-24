import { useEffect, useState } from "react";
import { fetchPlantCare, searchPlants } from "../services/plantCareService.js";
import MapSelector from "../components/MapSelector.jsx";
import { Link, useNavigate } from "react-router-dom";

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
	const [activeSuggestionIndex, setActiveSuggestionIndex] = useState(-1);

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

	useEffect(() => {
		setActiveSuggestionIndex(-1);
	}, [searchResults, showResults]);

	const handleSearchKeyDown = (event) => {
		if (!showResults || searchResults.length === 0) {
			return;
		}

		if (event.key === "ArrowDown") {
			event.preventDefault();
			setActiveSuggestionIndex((previousIndex) =>
				previousIndex < searchResults.length - 1 ? previousIndex + 1 : 0
			);
			return;
		}

		if (event.key === "ArrowUp") {
			event.preventDefault();
			setActiveSuggestionIndex((previousIndex) =>
				previousIndex > 0 ? previousIndex - 1 : searchResults.length - 1
			);
			return;
		}

		if (event.key === "Enter" && activeSuggestionIndex >= 0) {
			event.preventDefault();
			const selectedPlant = searchResults[activeSuggestionIndex];
			if (selectedPlant) {
				setShowResults(false);
				navigate(`/planta/${selectedPlant.id}`);
			}
			return;
		}

		if (event.key === "Escape") {
			setShowResults(false);
		}
	};

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
			const latitude = location.lat;
			const longitude = location.lng;
			const response = await fetchPlantCare({ plantId, season, latitude, longitude });
			setResult(response);
		} catch (err) {
			setError(err?.message || "No se pudo obtener la recomendación.");
			setResult(null);
		} finally {
			setLoading(false);
		}
	};

	return (
		<main className="home-page">
			<section className="home-hero card">
				<h2>Tu jardín en contexto real</h2>
				<p>Busca plantas, elige tu ubicación en el mapa y recibe recomendaciones claras para cada temporada.</p>
			</section>

			<div className="home-layout">
				<section className="card home-card home-card--search">
					<h2>Buscador de plantas</h2>
					<p className="home-section-subtitle">Explora por nombre común o especie científica.</p>
					<div className="form">
						<label>
							Nombre o especie
							<div className="search-input-wrapper">
								<span className="search-input-icon" aria-hidden="true">⌕</span>
								<input
									type="text"
									className="search-input"
									value={searchQuery}
									onChange={(event) => {
										setSearchQuery(event.target.value);
										setShowResults(true);
										setActiveSuggestionIndex(-1);
									}}
									onFocus={() => setShowResults(true)}
									onBlur={() => {
										setTimeout(() => setShowResults(false), 120);
									}}
									onKeyDown={handleSearchKeyDown}
									placeholder="ej: fern"
									required
									role="combobox"
									aria-expanded={showResults && searchResults.length > 0}
									aria-controls="plant-search-suggestions"
									aria-autocomplete="list"
									aria-activedescendant={activeSuggestionIndex >= 0 ? `plant-suggestion-${searchResults[activeSuggestionIndex]?.id}` : undefined}
								/>
							</div>
						</label>
						{showResults && searchResults.length > 0 && (
							<ul className="suggestions-list" id="plant-search-suggestions" role="listbox">
								{searchResults.map((plant, index) => (
									<li key={plant.id}>
										<button
											type="button"
											id={`plant-suggestion-${plant.id}`}
											className={`suggestion-item ${activeSuggestionIndex === index ? "suggestion-item--active" : ""}`}
											onMouseEnter={() => setActiveSuggestionIndex(index)}
											onMouseDown={(event) => event.preventDefault()}
											onClick={() => {
												setShowResults(false);
												navigate(`/planta/${plant.id}`);
											}}
											role="option"
											aria-selected={activeSuggestionIndex === index}
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
						<p className="home-muted">No se encontraron plantas.</p>
					)}
					{!searchQuery.trim() && !searchLoading && !searchError && (
						<p className="home-muted">Empieza escribiendo para descubrir plantas disponibles.</p>
					)}
				</section>

				<section className="card home-card home-card--map">
					<h2>Mapa y contexto climático</h2>
					<p className="home-section-subtitle">Selecciona planta, ubicación y temporada para recibir recomendaciones.</p>
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
					<button type="submit" className="btn btn--primary" disabled={loading || !location}>
						{loading ? "Consultando..." : "Evaluar"}
					</button>
				</form>
				</section>

				<section className="card home-card home-card--plants">
					<h2>Tarjetas de plantas</h2>
					<p className="home-section-subtitle">Resultados visuales del buscador para navegar rápido al detalle.</p>
					{searchResults.length > 0 ? (
						<div className="plant-list">
							{searchResults.slice(0, 8).map((plant) => (
								<Link key={plant.id} to={`/planta/${plant.id}`} className="plant-card-link">
									<article className="plant-card">
										{plant.image_url && (
											<img src={plant.image_url} alt={plant.common_name ?? plant.scientific_name} />
										)}
										<div>
											<p className="plant-title">{plant.common_name ?? "Sin nombre común"}</p>
											<p className="plant-meta">{plant.scientific_name}</p>
										</div>
									</article>
								</Link>
							))}
						</div>
					) : (
						<p className="home-muted">Las tarjetas aparecerán aquí cuando realices una búsqueda.</p>
					)}
				</section>

				<section className="card home-card home-card--result">
					<h2>Resultado</h2>
					{error && <p className="error">{error}</p>}
					{!result && !error && <p>Completa el formulario para obtener recomendaciones.</p>}
					{result && (
						<div className="result">
							<p><strong>Planta:</strong> {result.plantId}</p>
							<p><strong>Ciudad:</strong> {result.city}</p>
							<p><strong>Época:</strong> {result.season}</p>
							{typeof result.temperature === "number" && (
								<p><strong>Temperatura:</strong> {result.temperature.toFixed(1)} °C</p>
							)}
							{typeof result.humidity === "number" && (
								<p><strong>Humedad:</strong> {result.humidity}%</p>
							)}
							{typeof result.altitude === "number" && (
								<p><strong>Altitud:</strong> {Math.round(result.altitude)} m</p>
							)}
							{result.dataQuality && (
								<p><strong>Calidad de datos:</strong> {result.dataQuality}</p>
							)}
							<p><strong>Resumen:</strong> {result.summary}</p>
							<p><strong>Recomendación:</strong> {result.recommendation}</p>
							<p><strong>¿Interior?</strong> {result.indoorFriendly ? "Sí" : "No"}</p>
						</div>
					)}
				</section>
			</div>
		</main>
	);
}
