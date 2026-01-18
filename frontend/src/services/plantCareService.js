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
