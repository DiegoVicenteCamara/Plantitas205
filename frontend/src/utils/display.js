export function toDisplayValue(value) {
	if (value === null || value === undefined) {
		return "Desconocido";
	}
	if (typeof value === "string") {
		const trimmed = value.trim();
		return trimmed ? trimmed : "Desconocido";
	}
	return String(value);
}

export function toIndoorDisplay(value) {
	if (typeof value === "boolean") {
		return value ? "Sí" : "No";
	}
	return "Desconocido";
}
