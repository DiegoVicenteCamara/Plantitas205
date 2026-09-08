import { useState } from "react";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import AdvancedFilters from "./AdvancedFilters.jsx";

const EMPTY_FILTERS = {
	category: "",
	light: "",
	water: "",
	humidity: ""
};

function Harness({ initialValue = EMPTY_FILTERS, onReset, onFilterChange: externalOnFilterChange }) {
	const [value, setValue] = useState(initialValue);
	const handleChange = (nextValue) => {
		if (externalOnFilterChange) {
			externalOnFilterChange(nextValue);
		}
		setValue(nextValue);
	};
	return (
		<AdvancedFilters
			value={value}
			onFilterChange={handleChange}
			onReset={onReset}
		/>
	);
}

function defaultProps(overrides = {}) {
	return {
		onFilterChange: vi.fn(),
		onReset: vi.fn(),
		...overrides
	};
}

async function openPanel(user) {
	const toggle = screen.getByRole("button", { name: "Filtros" });
	if (toggle.getAttribute("aria-expanded") === "false") {
		await user.click(toggle);
	}
}

describe("AdvancedFilters", () => {
	describe("renders_withPanelClosedByDefault", () => {
		it("shows only a collapsed small filter button on the left", () => {
			render(<AdvancedFilters {...defaultProps()} />);

			const toggle = screen.getByRole("button", { name: "Filtros" });
			expect(toggle).toHaveAttribute("aria-expanded", "false");
			expect(screen.queryByRole("group", { name: "Categoría" })).not.toBeInTheDocument();
			expect(screen.queryByRole("button", { name: "Limpiar filtros" })).not.toBeInTheDocument();
		});
	});

	describe("togglesPanelVisibility_whenToggleClicked", () => {
		it("expands the panel on first click and collapses on second", async () => {
			const user = userEvent.setup();
			render(<AdvancedFilters {...defaultProps()} />);

			const toggle = screen.getByRole("button", { name: "Filtros" });
			await user.click(toggle);

			expect(toggle).toHaveAttribute("aria-expanded", "true");
			expect(screen.getByRole("group", { name: "Categoría" })).toBeVisible();
			expect(screen.getByRole("region", { name: "Filtros avanzados de búsqueda" })).toBeVisible();

			await user.click(toggle);
			expect(toggle).toHaveAttribute("aria-expanded", "false");
			expect(screen.queryByRole("group", { name: "Categoría" })).not.toBeInTheDocument();
		});
	});

	describe("selectingCategoryChip_triggersCallbackWithFullState", () => {
		it("calls onFilterChange with the full filter dictionary", async () => {
			const onFilterChange = vi.fn();
			const user = userEvent.setup();

			render(
				<Harness
					initialValue={{ category: "interior", light: "", water: "", humidity: "" }}
					onFilterChange={onFilterChange}
				/>
			);
			await openPanel(user);

			const cactusChip = screen.getByRole("radio", { name: "Cactus" });
			await user.click(cactusChip);

			expect(onFilterChange).toHaveBeenCalledWith({
				category: "cactus",
				light: "",
				water: "",
				humidity: ""
			});
			expect(cactusChip).toHaveAttribute("aria-checked", "true");
		});
	});

	describe("selectingCategoryTodas_clearsCategory", () => {
		it("resets category to empty string when 'Todas' is clicked", async () => {
			const onFilterChange = vi.fn();
			const user = userEvent.setup();
			const initialValue = {
				category: "cactus",
				light: "",
				water: "",
				humidity: ""
			};

			render(
				<AdvancedFilters
					{...defaultProps({ onFilterChange })}
					value={initialValue}
				/>
			);
			await openPanel(user);

			const todasChip = screen.getByRole("radio", { name: "Todas" });
			await user.click(todasChip);

			expect(onFilterChange).toHaveBeenCalledWith({
				category: "",
				light: "",
				water: "",
				humidity: ""
			});
		});
	});

	describe("changingLightSelect_triggersCallbackWithFullState", () => {
		it("calls onFilterChange with the selected light level", async () => {
			const onFilterChange = vi.fn();
			const user = userEvent.setup();
			render(<AdvancedFilters {...defaultProps({ onFilterChange })} />);
			await openPanel(user);

			const lightSelect = screen.getByLabelText("Nivel de luz");
			await user.selectOptions(lightSelect, "high");

			expect(onFilterChange).toHaveBeenCalledWith({
				category: "",
				light: "high",
				water: "",
				humidity: ""
			});
		});
	});

	describe("changingWaterAndHumidity_triggersCallbackWithFullState", () => {
		it("includes previously-set values when changing a different filter", async () => {
			const onFilterChange = vi.fn();
			const user = userEvent.setup();
			const initialValue = {
				category: "",
				light: "high",
				water: "",
				humidity: ""
			};

			render(
				<AdvancedFilters
					{...defaultProps({ onFilterChange })}
					value={initialValue}
				/>
			);
			await openPanel(user);

			const waterSelect = screen.getByLabelText("Necesidad de agua");
			await user.selectOptions(waterSelect, "medium");

			expect(onFilterChange).toHaveBeenCalledWith({
				category: "",
				light: "high",
				water: "medium",
				humidity: ""
			});
		});
	});

	describe("clickingReset_callsOnReset", () => {
		it("invokes onReset when reset button is clicked with active filters", async () => {
			const onReset = vi.fn();
			const user = userEvent.setup();
			const initialValue = {
				category: "cactus",
				light: "high",
				water: "",
				humidity: ""
			};

			render(
				<AdvancedFilters
					{...defaultProps({ onReset })}
					value={initialValue}
				/>
			);
			await openPanel(user);

			const resetButton = screen.getByRole("button", { name: "Limpiar filtros" });
			await user.click(resetButton);

			expect(onReset).toHaveBeenCalledTimes(1);
		});
	});

	describe("clickingReset_withoutOnResetProp_fallsBackToDefaultFilterPayload", () => {
		it("calls onFilterChange with default empty filters when onReset is not provided", async () => {
			const onFilterChange = vi.fn();
			const user = userEvent.setup();
			const initialValue = {
				category: "cactus",
				light: "",
				water: "",
				humidity: ""
			};

			render(
				<AdvancedFilters
					{...defaultProps({ onFilterChange, onReset: undefined })}
					value={initialValue}
				/>
			);
			await openPanel(user);

			const resetButton = screen.getByRole("button", { name: "Limpiar filtros" });
			await user.click(resetButton);

			expect(onFilterChange).toHaveBeenCalledWith({
				category: "",
				light: "",
				water: "",
				humidity: ""
			});
		});
	});

	describe("resetButtonDisabled_whenAllFiltersDefault", () => {
		it("renders the reset button disabled when no filter is set", async () => {
			const user = userEvent.setup();
			render(<AdvancedFilters {...defaultProps()} />);
			await openPanel(user);

			const resetButton = screen.getByRole("button", { name: "Limpiar filtros" });
			expect(resetButton).toBeDisabled();
		});

		it("renders the reset button enabled when any filter is active", async () => {
			const user = userEvent.setup();
			const initialValue = {
				category: "",
				light: "",
				water: "low",
				humidity: ""
			};

			render(
				<AdvancedFilters
					{...defaultProps()}
					value={initialValue}
				/>
			);
			await openPanel(user);

			const resetButton = screen.getByRole("button", { name: "Limpiar filtros" });
			expect(resetButton).toBeEnabled();
		});
	});
});