import { useCallback, useEffect, useRef, useState } from "react";
import { searchPlants } from "../services/plantCareService.js";

const DEBOUNCE_MS = 300;

export default function usePlantSearch(filters) {
	const [query, setQuery] = useState("");
	const [results, setResults] = useState([]);
	const [loading, setLoading] = useState(false);
	const [error, setError] = useState("");
	const [activeIndex, setActiveIndex] = useState(-1);
	const cancelledRef = useRef(false);

	useEffect(() => {
		setActiveIndex(-1);
	}, [results]);

	useEffect(() => {
		const trimmed = query.trim();
		const hasAnyFilter = Object.values(filters).some(
			(value) => typeof value === "string" && value.trim() !== ""
		);

		if (!trimmed && !hasAnyFilter) {
			setResults([]);
			setLoading(false);
			setError("");
			return;
		}

		cancelledRef.current = false;
		const timeoutId = setTimeout(async () => {
			setLoading(true);
			setError("");
			try {
				const response = await searchPlants(trimmed, filters);
				if (!cancelledRef.current) {
					setResults(response.data ?? []);
				}
			} catch {
				if (!cancelledRef.current) {
					setResults([]);
					setError("No se pudo buscar en la base local.");
				}
			} finally {
				if (!cancelledRef.current) {
					setLoading(false);
				}
			}
		}, DEBOUNCE_MS);

		return () => {
			cancelledRef.current = true;
			clearTimeout(timeoutId);
		};
	}, [query, filters]);

	const handleKeyDown = useCallback(
		(event, { onSelect, onEscape }) => {
			if (!results.length) return;

			if (event.key === "ArrowDown") {
				event.preventDefault();
				setActiveIndex((prev) => (prev < results.length - 1 ? prev + 1 : 0));
				return;
			}

			if (event.key === "ArrowUp") {
				event.preventDefault();
				setActiveIndex((prev) => (prev > 0 ? prev - 1 : results.length - 1));
				return;
			}

			if (event.key === "Enter" && activeIndex >= 0) {
				event.preventDefault();
				const selected = results[activeIndex];
				if (selected) onSelect(selected);
				return;
			}

			if (event.key === "Escape") {
				onEscape();
			}
		},
		[results, activeIndex]
	);

	return {
		query,
		setQuery,
		results,
		loading,
		error,
		activeIndex,
		setActiveIndex,
		handleKeyDown,
	};
}
