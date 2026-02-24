import Home from "./pages/Home.jsx";
import { Route, Routes } from "react-router-dom";
import PlantPage from "./pages/PlantPage.jsx";

export default function App() {
	return (
		<div className="app-shell">
			<header className="app-header">
				<h1>Plantitas</h1>
				<p>Cuidados de plantas según tu ciudad y clima</p>
			</header>
			<Routes>
				<Route path="/" element={<Home />} />
				<Route path="/planta/:id" element={<PlantPage />} />
			</Routes>
		</div>
	);
}
