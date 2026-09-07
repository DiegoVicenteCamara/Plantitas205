# Plantitas205

A full-stack plant care assistant that provides personalized watering, lighting, and seasonal recommendations based on real-time weather data and geographic location.

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Backend | Java + Spring Boot | 21 / 3.3.6 |
| ORM | Spring Data JPA + Hibernate | - |
| Database | H2 (in-memory) | - |
| Resilience | Resilience4j (retry + cache) | 2.2.0 |
| Frontend | React + Vite | 18 / 5.4 |
| Maps | Leaflet + react-leaflet | 1.9 / 4.2 |
| Styling | CSS Custom Properties (design tokens) | - |
| Build | Maven / npm | 3.9 / 10 |
| Testing | JUnit 5 + Mockito + MockMvc (backend), Vitest + React Testing Library (frontend) | - |
| Coverage | JaCoCo | 0.8.12 |
| AI Agents | Copilot + `.agents/skills/` framework | - |

## Quick Start

### Backend

```bash
cd backend
./mvnw spring-boot:run        # Linux/macOS
mvnw.cmd spring-boot:run      # Windows
```

- API: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:plantitasdb`, user: `sa`, no password)

### Frontend

```bash
cd frontend
npm install
npm run dev
```

- App: `http://localhost:5173`

### Tests & Coverage

**Backend:**
```bash
cd backend
./mvnw verify                  # Linux/macOS
mvnw.cmd verify                # Windows
```

**Frontend:**
```bash
cd frontend
npm test                        # single run
npm run test:watch              # watch mode
```

Coverage report: `backend/target/site/jacoco/index.html`

## Features

- **Plant catalog** with 30 pre-seeded species (Monstera, Lavender, Aloe vera, etc.)
- **Personalized care recommendations** combining plant requirements, season, and live weather
- **Real-time weather** via Open-Meteo API with retry and caching
- **Reverse geocoding** via Nominatim (OpenStreetMap) for city resolution
- **Advanced search** with filters by category, light, water, and humidity levels
- **Interactive map** (Leaflet) for location selection
- **Dark mode** with CSS custom properties theming
- **Responsive design** with mobile-first approach

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/plant-care` | Get care recommendation for a plant + location + season |
| `GET` | `/api/plants/search` | Search plants with optional filters |
| `GET` | `/api/plants/suggestions` | Autocomplete plant names by prefix |
| `GET` | `/api/plants/{id}` | Get full plant details |

See [docs/API.md](docs/API.md) for full endpoint documentation.

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_API_BASE_URL` | `http://localhost:8080` | Backend API base URL (frontend) |

## Documentation

- [Architecture & Data Flow](docs/ARCHITECTURE.md) -- System diagrams, integration patterns, resilience strategies
- [Class Reference](docs/CLASSES.md) -- Full class-by-class reference with field/method listings and relationship diagrams
- [Development Methodology](docs/METHODOLOGY.md) -- Git workflow, TDD, agent-assisted development, coding standards
- [API Reference](docs/API.md) -- Endpoint details, request/response schemas, error handling

## License

This project is for educational purposes.
