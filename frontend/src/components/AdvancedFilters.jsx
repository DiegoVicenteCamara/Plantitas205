import { useMemo } from "react";

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
	onChange,
	idPrefix = "advanced-filters"
}) {
	const filters = useMemo(() => ({ ...DEFAULT_FILTERS, ...value }), [value]);

	const activeCount = useMemo(() => {
		let count = 0;
		if (filters.category) count++;
		if (filters.light) count++;
		if (filters.water) count++;
		if (filters.humidity) count++;
		return count;
	}, [filters]);

	const currentPreset = useMemo(() => {
		const { light, water, humidity } = filters;
		if (!light && !water && !humidity) return "";
		for (const [key, values] of Object.entries(PRESET_MAP)) {
			if (values.light === light && values.water === water && values.humidity === humidity) {
				return key;
			}
		}
		return "__custom";
	}, [filters]);

	const updateFilter = (key, nextValue) => {
		onChange?.({ [key]: nextValue });
	};

	const handlePresetChange = (presetKey) => {
		if (!presetKey || presetKey === "__custom") {
			onChange?.({ light: "", water: "", humidity: "" });
			return;
		}
		const values = PRESET_MAP[presetKey];
		if (values) {
			onChange?.(values);
		}
	};

	const clearAll = () => {
		onChange?.({ category: "", light: "", water: "", humidity: "" });
	};

	return (
		<section className="advanced-filters" aria-label="Filtros de búsqueda">
			<div className="advanced-filters__header">
				<span className="advanced-filters__title">
					Filtros
					{activeCount > 0 && (
						<span className="advanced-filters__badge">{activeCount}</span>
					)}
				</span>
				{activeCount > 0 && (
					<button
						type="button"
						className="btn btn--ghost advanced-filters__clear"
						onClick={clearAll}
					>
						Limpiar
					</button>
				)}
			</div>

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
