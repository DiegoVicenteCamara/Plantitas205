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

export async function fetchPlantCare(payload) {
	const response = await fetch(`${API_BASE_URL}/api/plant-care`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json"
		},
		body: JSON.stringify(payload)
	});

	if (!response.ok) {
		const message = await resolveErrorMessage(response, "No se pudo obtener la recomendación.");
		throw new Error(message);
	}

	return response.json();
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

	if (!response.ok) {
		throw new Error("Request failed");
	}

	return response.json();
}

export async function fetchPlantSuggestions(prefix) {
	const response = await fetch(`${API_BASE_URL}/api/plants/suggestions?prefix=${encodeURIComponent(prefix)}`);

	if (!response.ok) {
		throw new Error("Request failed");
	}

	return response.json();
}

export async function fetchPlantById(id) {
	const response = await fetch(`${API_BASE_URL}/api/plants/${encodeURIComponent(id)}`);

	if (!response.ok) {
		if (response.status === 404) {
			throw new Error("PLANT_NOT_FOUND");
		}
		throw new Error("REQUEST_FAILED");
	}

	return response.json();
}
