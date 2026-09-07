import { useState } from "react";
import { useNavigate } from "react-router-dom";
import usePlantSearch from "../hooks/usePlantSearch.js";
import MapSelector from "../components/MapSelector.jsx";
import AdvancedFilters from "../components/AdvancedFilters.jsx";
import { fetchPlantCare } from "../services/plantCareService.js";

export default function Home() {
	const navigate = useNavigate();
	const [plantId, setPlantId] = useState("");
	const [location, setLocation] = useState(null);
	const [season, setSeason] = useState("primavera");
	const [result, setResult] = useState(null);
	const [loading, setLoading] = useState(false);
	const [error, setError] = useState("");
	const [advancedFilters, setAdvancedFilters] = useState({
		category: "",
		light: "",
		water: "",
		humidity: "",
	});

	const {
		query: searchQuery,
		setQuery: setSearchQuery,
		results: searchResults,
		loading: searchLoading,
		error: searchError,
		activeIndex,
		setActiveIndex,
		handleKeyDown,
	} = usePlantSearch(advancedFilters);

	const handleAdvancedFiltersChange = (partialFilters) => {
		setAdvancedFilters((prev) => ({ ...prev, ...partialFilters }));
	};

	const handleSearchKeyDown = (event) => {
		handleKeyDown(event, {
			onSelect: (plant) => navigate(`/planta/${plant.id}`),
			onEscape: () => {},
		});
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
			const response = await fetchPlantCare({
				plantId,
				season,
				latitude: location.lat,
				longitude: location.lng,
			});
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
				<div className="home-hero-search">
					<AdvancedFilters value={advancedFilters} onChange={handleAdvancedFiltersChange} />
					<label>
						Buscar planta
						<div className="search-combobox">
							<div className="search-input-wrapper">
								<span className="search-input-icon" aria-hidden="true">⌕</span>
								<input
									type="text"
									className="search-input"
									value={searchQuery}
									onChange={(event) => {
										setSearchQuery(event.target.value);
										setActiveIndex(-1);
									}}
									onFocus={() => setActiveIndex(-1)}
									onKeyDown={handleSearchKeyDown}
									placeholder="ej: fern"
									required
									role="combobox"
									aria-expanded={searchResults.length > 0}
									aria-controls="plant-search-suggestions"
									aria-autocomplete="list"
									aria-activedescendant={
										activeIndex >= 0
											? `plant-suggestion-${searchResults[activeIndex]?.id}`
											: undefined
									}
								/>
							</div>
							{searchResults.length > 0 && (
								<ul className="suggestions-list" id="plant-search-suggestions" role="listbox">
									{searchResults.map((plant, index) => (
										<li key={plant.id}>
											<button
												type="button"
												id={`plant-suggestion-${plant.id}`}
												className={`suggestion-item ${activeIndex === index ? "suggestion-item--active" : ""}`}
												onMouseEnter={() => setActiveIndex(index)}
												onMouseDown={(event) => event.preventDefault()}
												onClick={() => navigate(`/planta/${plant.id}`)}
												role="option"
												aria-selected={activeIndex === index}
											>
												{plant.common_name ?? "Sin nombre común"}
												{plant.scientific_name ? ` · ${plant.scientific_name}` : ""}
											</button>
										</li>
									))}
								</ul>
							)}
						</div>
					</label>
					{searchLoading && <p className="home-muted">Buscando...</p>}
					{searchError && <p className="error">{searchError}</p>}
					{searchQuery.trim() &&
						!searchLoading &&
						searchResults.length === 0 &&
						!searchError && (
							<p className="home-muted">No se encontraron plantas.</p>
						)}
					{!searchQuery.trim() &&
						!Object.values(advancedFilters).some((v) => v) &&
						!searchLoading &&
						!searchError && (
							<p className="home-muted">Empieza escribiendo para descubrir plantas disponibles.</p>
						)}
				</div>
			</section>

			<div className="home-layout">
				<section className="card home-card home-card--context">
					<h2>Contexto y resultado</h2>
					<p className="home-section-subtitle">Configura ubicación y temporada, y revisa el resultado en el mismo bloque.</p>

					<div className="home-context-grid">
						<section className="home-context-block home-context-block--map" aria-label="Mapa y contexto climático">
							<h3>Mapa y contexto climático</h3>
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

						<section className="home-context-block home-context-block--result" aria-label="Resultado de recomendaciones">
							<h3>Resultado</h3>
							{error && <p className="error">{error}</p>}
							{!result && !error && <p>Completa el formulario para obtener recomendaciones.</p>}
							{result && (
								<div className="result">
									<p><strong>Planta:</strong> {result.plantId}</p>
									<p><strong>Ciudad:</strong> {result.city}</p>
									<p><strong>Época:</strong> {result.season}</p>
									{typeof result.temperature === "number" && (
										<p>
											<strong>Temperatura:</strong> {result.temperature.toFixed(1)} °C
											{result.idealTemperature && (
												<span> (ideal: {result.idealTemperature}){result.temperatureInRange != null ? (result.temperatureInRange ? " ✓" : " ✗") : ""}</span>
											)}
										</p>
									)}
									{typeof result.humidity === "number" && (
										<p>
											<strong>Humedad:</strong> {result.humidity}%
											{result.idealHumidity && (
												<span> (ideal: {result.idealHumidity}){result.humidityInRange != null ? (result.humidityInRange ? " ✓" : " ✗") : ""}</span>
											)}
										</p>
									)}
									{typeof result.altitude === "number" && (
										<p><strong>Altitud:</strong> {Math.round(result.altitude)} m</p>
									)}
									{result.dataQuality && (
										<p><strong>Calidad de datos:</strong> {result.dataQuality}</p>
									)}
									<p><strong>Resumen:</strong> {result.summary}</p>
									<p><strong>Recomendación:</strong> {result.recommendation}</p>
								</div>
							)}
						</section>
					</div>
				</section>
			</div>
		</main>
	);
}
