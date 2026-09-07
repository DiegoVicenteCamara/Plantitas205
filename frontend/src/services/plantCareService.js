const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

async function resolveErrorMessage(response, fallbackMessage) {
	try {
		const payload = await response.json();
		if (typeof payload?.message === "string" && payload.message.trim()) {
			return payload.message;
		}
		if (typeof payload?.error === "string" && payload.error.trim()) {
			return payload.error;
		}
	} catch {
		// ignore parsing errors and use fallback message
	}
	return fallbackMessage;
}

async function handleResponse(response, fallbackMessage) {
	if (!response.ok) {
		const message = await resolveErrorMessage(response, fallbackMessage);
		throw new Error(message);
	}
	return response.json();
}

export async function fetchPlantCare(payload) {
	const response = await fetch(`${API_BASE_URL}/api/plant-care`, {
		method: "POST",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify(payload),
	});
	return handleResponse(response, "No se pudo obtener la recomendación.");
}

export async function searchPlants(query, filters = {}) {
	const params = new URLSearchParams();

	if (typeof query === "string" && query.trim()) {
		params.set("q", query.trim());
	}

	["category", "light", "water", "humidity"].forEach((key) => {
		const value = filters?.[key];
		if (typeof value === "string" && value.trim()) {
			params.set(key, value.trim());
		}
	});

	const response = await fetch(`${API_BASE_URL}/api/plants/search?${params.toString()}`);
	return handleResponse(response, "No se pudo buscar plantas.");
}

export async function fetchPlantSuggestions(prefix) {
	const response = await fetch(
		`${API_BASE_URL}/api/plants/suggestions?prefix=${encodeURIComponent(prefix)}`
	);
	return handleResponse(response, "No se pudieron obtener sugerencias.");
}

export async function fetchPlantById(id) {
	const response = await fetch(`${API_BASE_URL}/api/plants/${encodeURIComponent(id)}`);

	if (!response.ok) {
		if (response.status === 404) {
			throw new Error("PLANT_NOT_FOUND");
		}
		const message = await resolveErrorMessage(response, "No se pudo cargar la planta.");
		throw new Error(message);
	}

	return response.json();
}
