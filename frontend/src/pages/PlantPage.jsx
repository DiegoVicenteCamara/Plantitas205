import { Link, useParams } from "react-router-dom";

export default function PlantPage() {
	const { id } = useParams();

	return (
		<main className="content">
			<section className="card">
				<h2>Detalle de planta</h2>
				<p><strong>ID:</strong> {id}</p>
				<Link to="/">Volver al inicio</Link>
			</section>
		</main>
	);
}
