import { useEffect, useState } from "react";
import Home from "./pages/Home.jsx";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import PlantPage from "./pages/PlantPage.jsx";

const THEME_STORAGE_KEY = "plantitas-theme";

export default function App() {
	const [isDarkMode, setIsDarkMode] = useState(
		window.localStorage.getItem(THEME_STORAGE_KEY) === "dark"
	);

	useEffect(() => {
		window.localStorage.setItem(THEME_STORAGE_KEY, isDarkMode ? "dark" : "light");

		if (typeof document !== "undefined") {
			document.documentElement.classList.toggle("dark-theme", isDarkMode);
		}
	}, [isDarkMode]);

	return (
		<BrowserRouter>
			<div className={`app-root ${isDarkMode ? "dark-theme" : ""}`}>
				<div className="app-shell">
					<header className="app-header">
						<div className="app-header__top">
							<h1>Plantitas</h1>
							<button
								type="button"
								className="theme-toggle btn btn--secondary"
								onClick={() => setIsDarkMode((previousValue) => !previousValue)}
								aria-label="Cambiar tema"
							>
								{isDarkMode ? "Modo claro" : "Modo oscuro"}
							</button>
						</div>
						<p>Cuidados de plantas según tu ciudad y clima</p>
					</header>
					<Routes>
						<Route path="/" element={<Home />} />
						<Route path="/planta/:id" element={<PlantPage />} />
					</Routes>
				</div>
			</div>
		</BrowserRouter>
	);
}
