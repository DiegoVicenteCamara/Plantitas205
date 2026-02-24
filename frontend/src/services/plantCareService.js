const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export async function fetchPlantCare(payload) {
	const response = await fetch(`${API_BASE_URL}/api/plant-care`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json"
		},
		body: JSON.stringify(payload)
	});

	if (!response.ok) {
		throw new Error("Request failed");
	}

	return response.json();
}

export async function searchPlants(query) {
	const response = await fetch(`${API_BASE_URL}/api/plants/search?q=${encodeURIComponent(query)}`);

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
