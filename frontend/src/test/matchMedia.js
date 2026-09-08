export function createMatchMedia(matches) {
	return (query) => ({
		matches: Boolean(matches),
		media: query,
		onchange: null,
		addEventListener() {},
		removeEventListener() {},
		addListener() {},
		removeListener() {},
		dispatchEvent() {
			return false;
		}
	});
}

export function installMatchMedia(matches) {
	window.matchMedia = createMatchMedia(matches);
}