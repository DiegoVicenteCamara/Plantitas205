import { useEffect, useState } from "react";
import Home from "./pages/Home.jsx";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import PlantPage from "./pages/PlantPage.jsx";

const THEME_STORAGE_KEY = "plantitas-theme";

export default function App() {
	const [isDarkMode, setIsDarkMode] = useState(() => {
		if (typeof window === "undefined") {
			return false;
		}
		return window.localStorage.getItem(THEME_STORAGE_KEY) === "dark";
	});

	useEffect(() => {
		window.localStorage.setItem(THEME_STORAGE_KEY, isDarkMode ? "dark" : "light");
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
								className="theme-toggle"
								onClick={() => setIsDarkMode((previousValue) => !previousValue)}
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
