import { useMemo, useState } from "react";

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

const PRESET_OPTIONS = [
	{ value: "", label: "Cualquiera" },
	{ value: "low-maintenance", label: "Bajo mantenimiento" },
	{ value: "moderate", label: "Moderado" },
	{ value: "high-maintenance", label: "Alto mantenimiento" },
	{ value: "tropical", label: "Tropical" },
	{ value: "dry", label: "Seco" }
];

const PRESET_MAP = {
	"low-maintenance": { light: "low", water: "low", humidity: "low" },
	"moderate": { light: "medium", water: "medium", humidity: "medium" },
	"high-maintenance": { light: "high", water: "high", humidity: "high" },
	"tropical": { light: "medium", water: "medium", humidity: "high" },
	"dry": { light: "high", water: "low", humidity: "low" }
};

const DEFAULT_FILTERS = {
	category: "",
	light: "",
	water: "",
	humidity: ""
};

export default function AdvancedFilters({
	value = DEFAULT_FILTERS,
	onFilterChange,
	onReset,
	idPrefix = "advanced-filters"
}) {
	const filters = useMemo(() => ({ ...DEFAULT_FILTERS, ...value }), [value]);
	const [isOpen, setIsOpen] = useState(false);

	const hasActiveFilters = Object.values(filters).some((filterValue) => filterValue !== "");

	const updateFilter = (key, nextValue) => {
		onFilterChange?.({
			...filters,
			[key]: nextValue
		});
	};

	const handleReset = () => {
		if (onReset) {
			onReset();
			return;
		}
		onFilterChange?.(DEFAULT_FILTERS);
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
				<span className="advanced-filters__icon" aria-hidden="true">
					<svg
						width="14"
						height="14"
						viewBox="0 0 24 24"
						fill="none"
						stroke="currentColor"
						strokeWidth="2"
						strokeLinecap="round"
						strokeLinejoin="round"
					>
						<polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3" />
					</svg>
				</span>
				Filtros
				<span className="advanced-filters__chevron" aria-hidden="true">
					{isOpen ? "▴" : "▾"}
				</span>
			</button>

			<div className="advanced-filters__row">
				<FilterSelect
					label="Categoría"
					name="category"
					value={filters.category}
					onChange={(v) => updateFilter("category", v)}
					options={CATEGORY_OPTIONS}
					idPrefix={idPrefix}
				/>
				<FilterSelect
					label="Perfil ambiental"
					name="preset"
					value={currentPreset === "__custom" ? "" : currentPreset}
					onChange={handlePresetChange}
					options={PRESET_OPTIONS}
					idPrefix={idPrefix}
				/>
			</div>

			{currentPreset === "__custom" && (
				<div className="advanced-filters__row advanced-filters__row--detail">
					<FilterSelect
						label="Luz"
						name="light"
						value={filters.light}
						onChange={(v) => updateFilter("light", v)}
						options={LEVEL_OPTIONS}
						idPrefix={idPrefix}
					/>
					<FilterSelect
						label="Agua"
						name="water"
						value={filters.water}
						onChange={(v) => updateFilter("water", v)}
						options={LEVEL_OPTIONS}
						idPrefix={idPrefix}
					/>
					<FilterSelect
						label="Humedad"
						name="humidity"
						value={filters.humidity}
						onChange={(v) => updateFilter("humidity", v)}
						options={LEVEL_OPTIONS}
						idPrefix={idPrefix}
					/>
				</div>

				<div className="advanced-filters__footer">
					<button
						type="button"
						className="btn advanced-filters__reset"
						onClick={handleReset}
						disabled={!hasActiveFilters}
					>
						Limpiar filtros
					</button>
				</div>
				</div>
			)}
		</section>
	);
}

function FilterSelect({ label, name, value, onChange, options, idPrefix = "advanced-filters" }) {
	return (
		<label htmlFor={`${idPrefix}-${name}`} className="advanced-filters__field">
			<span className="advanced-filters__field-label">{label}</span>
			<select
				id={`${idPrefix}-${name}`}
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
