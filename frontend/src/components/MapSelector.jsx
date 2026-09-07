import { useMemo } from "react";
import { MapContainer, Marker, TileLayer, useMapEvents } from "react-leaflet";
import L from "leaflet";
import markerIcon2x from "leaflet/dist/images/marker-icon-2x.png";
import markerIcon from "leaflet/dist/images/marker-icon.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";

const DEFAULT_CENTER = [40.4168, -3.7038];
const DEFAULT_ZOOM = 5;

function MapClickHandler({ onSelect }) {
	useMapEvents({
		click(event) {
			const { lat, lng } = event.latlng;
			onSelect({ lat, lng });
		}
	});

	return null;
}

export default function MapSelector({ value, onChange }) {
	const markerPosition = value ? [value.lat, value.lng] : null;

	const markerIconConfig = useMemo(
		() =>
			L.icon({
				iconRetinaUrl: markerIcon2x,
				iconUrl: markerIcon,
				shadowUrl: markerShadow,
				iconSize: [25, 41],
				iconAnchor: [12, 41],
				popupAnchor: [1, -34],
				shadowSize: [41, 41]
			}),
		[]
	);

	return (
		<div className="map-selector">
			<p className="map-selector__label">Ubicación exacta</p>
			<MapContainer
				center={markerPosition ?? DEFAULT_CENTER}
				zoom={markerPosition ? 13 : DEFAULT_ZOOM}
				scrollWheelZoom
				className="map-selector__map"
			>
				<TileLayer
					attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
					url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
				/>
				<MapClickHandler onSelect={onChange} />
				{markerPosition && <Marker position={markerPosition} icon={markerIconConfig} />}
			</MapContainer>
			<p className="map-selector__hint">
				{value
					? `Lat: ${value.lat.toFixed(6)} · Lng: ${value.lng.toFixed(6)}`
					: "Haz clic en el mapa para seleccionar la ubicación."}
			</p>
		</div>
	);
}
