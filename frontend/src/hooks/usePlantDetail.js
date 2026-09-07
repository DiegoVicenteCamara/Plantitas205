import { useEffect, useState } from "react";
import { fetchPlantById } from "../services/plantCareService.js";

export default function usePlantDetail(id) {
	const [plant, setPlant] = useState(null);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState("");

	useEffect(() => {
		let cancelled = false;

		const loadPlant = async () => {
			setLoading(true);
			setError("");
			try {
				const data = await fetchPlantById(id);
				if (!cancelled) setPlant(data);
			} catch (loadError) {
				if (!cancelled) {
					setPlant(null);
					setError(
						loadError.message === "PLANT_NOT_FOUND"
							? "La planta no existe."
							: "No se pudo cargar la información de la planta."
					);
				}
			} finally {
				if (!cancelled) setLoading(false);
			}
		};

		loadPlant();
		return () => { cancelled = true; };
	}, [id]);

	return { plant, loading, error };
}
