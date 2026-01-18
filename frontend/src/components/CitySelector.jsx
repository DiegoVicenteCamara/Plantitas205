export default function CitySelector({ value, onChange }) {
	return (
		<label>
			Ciudad
			<input
				type="text"
				value={value}
				onChange={(event) => onChange(event.target.value)}
				placeholder="ej: Madrid"
				required
			/>
		</label>
	);
}
