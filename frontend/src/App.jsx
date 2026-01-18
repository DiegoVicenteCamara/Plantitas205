import Home from "./pages/Home.jsx";

export default function App() {
	return (
		<div className="app-shell">
			<header className="app-header">
				<h1>Plantitas</h1>
				<p>Cuidados de plantas según tu ciudad y clima</p>
			</header>
			<Home />
		</div>
	);
}
