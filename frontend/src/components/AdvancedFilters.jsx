import { useEffect, useMemo, useState } from "react";

const CATEGORY_OPTIONS = [
	{ value: "", label: "Todas" },
	{ value: "suculenta", label: "Suculentas" },
	{ value: "cactus", label: "Cactus" },
	{ value: "interior", label: "Interior" },
	{ value: "arbol", label: "Árbol" },
	{ value: "helecho", label: "Helecho" },
	{ value: "tropical", label: "Tropical" },
	{ value: "aromatica", label: "Aromática" },
	{ value: "flor", label: "Flor" },
	{ value: "trepadora", label: "Trepadora" },
	{ value: "orquidea", label: "Orquídea" }
];

const LEVEL_OPTIONS = [
	{ value: "", label: "Cualquiera" },
	{ value: "low", label: "Bajo" },
	{ value: "medium", label: "Medio" },
	{ value: "high", label: "Alto" }
];

const DEFAULT_FILTERS = {
	category: "",
	light: "",
	water: "",
	humidity: ""
};

export default function AdvancedFilters({
	value = DEFAULT_FILTERS,
	onChange,
	idPrefix = "advanced-filters"
}) {
	const filters = useMemo(() => ({ ...DEFAULT_FILTERS, ...value }), [value]);
	const [isOpen, setIsOpen] = useState(() => {
		if (typeof window === "undefined") {
			return true;
		}
		return !window.matchMedia("(max-width: 899px)").matches;
	});

	useEffect(() => {
		const mediaQuery = window.matchMedia("(max-width: 899px)");
		const updateExpandedState = () => setIsOpen(!mediaQuery.matches);
		updateExpandedState();
		mediaQuery.addEventListener("change", updateExpandedState);
		return () => mediaQuery.removeEventListener("change", updateExpandedState);
	}, []);

	const updateFilter = (key, nextValue) => {
		onChange?.({
			[key]: nextValue
		});
	};

	const panelId = `${idPrefix}-panel`;

	return (
		<section className="advanced-filters" aria-label="Filtros avanzados de búsqueda">
			<button
				type="button"
				className="btn btn--secondary advanced-filters__toggle"
				onClick={() => setIsOpen((previous) => !previous)}
				aria-expanded={isOpen}
				aria-controls={panelId}
			>
				Filtros Avanzados
				<span aria-hidden="true">{isOpen ? "▴" : "▾"}</span>
			</button>

			{isOpen && (
				<div
					id={panelId}
					className="advanced-filters__panel advanced-filters__panel--open"
				>
				<div className="advanced-filters__group" role="group" aria-labelledby={`${idPrefix}-category-label`}>
					<p id={`${idPrefix}-category-label`} className="advanced-filters__label">Categoría</p>
					<div className="advanced-filters__chips" role="radiogroup" aria-label="Categoría">
						{CATEGORY_OPTIONS.map((option) => (
							<button
								type="button"
								key={option.value || "all"}
								className={`advanced-filters__chip ${filters.category === option.value ? "advanced-filters__chip--active" : ""}`}
								onClick={() => updateFilter("category", option.value)}
								role="radio"
								aria-checked={filters.category === option.value}
							>
								{option.label}
							</button>
						))}
					</div>
				</div>

				<div className="advanced-filters__grid">
					<FilterSelect
						label="Nivel de luz"
						name="light"
						value={filters.light}
						onChange={(nextValue) => updateFilter("light", nextValue)}
						options={LEVEL_OPTIONS}
					/>
					<FilterSelect
						label="Necesidad de agua"
						name="water"
						value={filters.water}
						onChange={(nextValue) => updateFilter("water", nextValue)}
						options={LEVEL_OPTIONS}
					/>
					<FilterSelect
						label="Humedad"
						name="humidity"
						value={filters.humidity}
						onChange={(nextValue) => updateFilter("humidity", nextValue)}
						options={LEVEL_OPTIONS}
					/>
				</div>
				</div>
			)}
		</section>
	);
}

function FilterSelect({ label, name, value, onChange, options }) {
	return (
		<label htmlFor={`advanced-filter-${name}`} className="advanced-filters__field">
			{label}
			<select
				id={`advanced-filter-${name}`}
				name={name}
				value={value}
				onChange={(event) => onChange(event.target.value)}
			>
				{options.map((option) => (
					<option key={option.value || "any"} value={option.value}>{option.label}</option>
				))}
			</select>
		</label>
	);
}
