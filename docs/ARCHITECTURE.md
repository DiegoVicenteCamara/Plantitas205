# Architecture

## System Overview

```
+------------------------------------------------------------------+
|                         Frontend (React)                          |
|                     localhost:5173 (Vite)                         |
|  +-------------+  +-------------+  +---------------------------+  |
|  | Home Page   |  | Plant Detail|  | Advanced Filters          |  |
|  | - Search    |  | - Info      |  | - Category chips          |  |
|  | - Map       |  | - Climate   |  | - Light/Water/Humidity    |  |
|  | - Form      |  | - Badges    |  +---------------------------+  |
|  +------+------+  +------+------+                                |
|         |                |                                        |
|  +------+------+                                                        |
|  | usePlantSearch|  usePlantDetail    (custom hooks)                   |
|  +------+-------+                                                       |
|         |                                                               |
|  +------+--------------------------------------------------------+    |
|  |              plantCareService.js (fetch API)                  |    |
|  +------+--------------------------------------------------------+    |
+---------|----------------------------------------------------------------+
          | HTTP (fetch)
          v
+------------------------------------------------------------------+
|                    Backend (Spring Boot)                          |
|                     localhost:8080                                |
|                                                                   |
|  +--------------------------------------------------------------+ |
|  |                  PlantController (@RestController)            | |
|  |  POST /api/plant-care  | GET /search | GET /suggestions      | |
|  |  GET /api/plants/{id}  |                                      | |
|  +-------------------------------+------------------------------+ |
|                                  |                                |
|  +-------------------------------v------------------------------+ |
|  |               PlantCareService (@Service)                    | |
|  |  - getPlantCare(request)                                      | |
|  |  - searchPlants(query, filters)                               | |
|  |  - suggestPlantNames(prefix)                                  | |
|  |  - getPlantById(id)                                           | |
|  +---+------------------+------------------+--------------------+ |
|      |                  |                  |                       |
|  +---v---+      +------v------+    +------v-----------+          |
|  | Plant  |      | Weather     |    | ReverseGeo-     |          |
|  | Repo   |      | Client      |    | codingClient    |          |
|  | (JPA)  |      | (OpenMeteo) |    | (Nominatim)     |          |
|  +---+----+      +------+------+    +------+----------+          |
|      |                  |                  |                       |
|      v                  v                  v                       |
|  +--------+      +-----------+      +-----------+                |
|  |  H2    |      | Open-Meteo|      | Nominatim |                |
|  | Memory |      |   API     |      |   API     |                |
|  +--------+      +-----------+      +-----------+                |
+------------------------------------------------------------------+
```

## Data Flow

### Plant Care Request (POST /api/plant-care)

```
1. Frontend sends: { plantId, city|lat|lng, season }
                                  |
2. PlantController validates request
                                  |
3. PlantCareService orchestrates:
   |
   +--> resolvePlant(plantId)
   |      Tries: numeric ID -> slug -> commonName/scientificName
   |
   +--> resolveLocation(request)
   |      If lat/lng provided:
   |        -> ReverseGeocodingClient.resolveCity(lat, lng)
   |        -> Returns city name + fallback flag
   |      If city provided:
   |        -> Use directly, no geocoding needed
   |
   +--> resolveWeather(request)
   |      If lat/lng available:
   |        -> WeatherClient.getCurrentWeather(lat, lng)
   |        -> Returns temperature, humidity, altitude
   |      If city only:
   |        -> Returns empty WeatherData (weather fallback)
   |
   +--> determineDataQuality(geocodeFallback, weatherFallback)
   |      Returns: "full" | "geocode-fallback" | "weather-fallback"
   |
   +--> buildRecommendation(plant, season, weatherData)
          Combines:
          - Watering advice (from plant data)
          - Light advice (from plant data)
          - Seasonal tip (based on season)
          - Weather tip (based on temperature/humidity)

4. Response sent to frontend
```

### Plant Search (GET /api/plants/search)

```
1. Frontend sends: ?q=monstera&category=TROPICAL&light=HIGH&water=MEDIUM&humidity=HIGH
                                  |
2. PlantController extracts query params
                                  |
3. PlantCareService.buildSearchSpecification()
   |
   +--> PlantSpecifications.commonOrScientificNameContains(query)
   +--> PlantSpecifications.hasCategory(category)
   +--> PlantSpecifications.lightRequirementEquals(level)
   +--> PlantSpecifications.waterRequirementEquals(level)
   +--> PlantSpecifications.humidityRequirementEquals(level)
                                  |
4. PlantRepository.findAll(specification)
                                  |
5. Results mapped to PlantSearchItem DTOs
                                  |
6. Response: { data: [{ id, common_name, scientific_name, image_url }, ...] }
```

## External Integrations

### Open-Meteo Weather API

- **Endpoint:** `https://api.open-meteo.com/v1/forecast`
- **Parameters:** `latitude`, `longitude`, `current=temperature_2m,relative_humidity_2m,precipitation,weather_code`, `elevation`, `timezone=auto`
- **Resilience:**
  - Retry: 3 attempts, 500ms initial, exponential backoff
  - Cache: `RoundedCoordinateCache` with 2-minute TTL, 3 decimal precision
  - Fallback: Returns empty `WeatherData` on failure

### Nominatim Reverse Geocoding API

- **Endpoint:** `https://nominatim.openstreetmap.org/reverse`
- **Parameters:** `lat`, `lon`, `format=json`, `zoom=10`
- **Headers:** `User-Agent: PlantitasApp/1.0`
- **Resilience:**
  - Retry: 3 attempts, 500ms initial, exponential backoff
  - Cache: `RoundedCoordinateCache` with 5-minute TTL, 3 decimal precision
  - Fallback: Returns `null` on failure
  - City resolution priority: `city` -> `town` -> `village` -> `municipality`

## Resilience Patterns

### RoundedCoordinateCache

An in-memory TTL cache that rounds latitude/longitude to a configurable precision before keying. Prevents redundant API calls for nearby coordinates.

```
Coordinate Input (40.41683, -3.70379)
         |
         v
  Round to 3 decimals (40.417, -3.704)
         |
         v
  Build key: "40.417|-3.704"
         |
         v
  Check ConcurrentHashMap:
    - Hit & not expired? Return cached value
    - Miss or expired? Compute, store, return
```

### Data Quality Indicators

The system tracks data completeness via a `dataQuality` field:

| Value | Meaning |
|-------|---------|
| `full` | All data sources available (plant + weather + geocoding) |
| `geocode-fallback` | Weather available, but reverse geocoding failed (city resolved from request) |
| `weather-fallback` | Geocoding available, but weather API failed (no weather-based tips) |

## Frontend Architecture

### Component Hierarchy

```
App.jsx
├── Header (dark mode toggle)
├── Routes
│   ├── "/" -> Home.jsx
│   │   ├── AdvancedFilters (category chips + level selects)
│   │   ├── usePlantSearch (debounced search hook)
│   │   │   └── suggestions list (autocomplete)
│   │   ├── MapSelector (Leaflet click-to-select)
│   │   ├── Season dropdown
│   │   └── Results display
│   │
│   └── "/planta/:id" -> PlantPage.jsx
│       └── PlantDetail.jsx
│           ├── usePlantDetail (fetch by ID)
│           ├── Plant image
│           ├── Care badges (water/light/temp)
│           ├── Climate recommendation
│           └── Ideal conditions sidebar
```

### State Management

- **Local state** via `useState` in each component
- **Custom hooks** encapsulate data fetching logic:
  - `usePlantSearch(filters)` -- debounced search with keyboard navigation
  - `usePlantDetail(id)` -- single plant fetch with loading/error states
- **Dark mode** persisted to `localStorage` (`plantitas-theme` key)
- **No global state library** -- keeps bundle small, state is page-scoped

### CSS Design System

- **30+ CSS custom properties** as design tokens (colors, spacing, radii, shadows)
- **Two themes:** light (default) and dark (`.dark-theme` class on root)
- **BEM-inspired** naming: `.plant-detail__header`, `.home-page__hero`, etc.
- **Responsive:** mobile-first media queries, container-based layouts
- **Fonts:** Inter (Google Fonts)

## Class Dependency Diagram

```
                        +---------------------+
                        | PlantController      |
                        +----------+----------+
                                   |
                                   v
                        +---------------------+
                        | PlantCareService     |
                        +----------+----------+
                                   |
              +--------------------+--------------------+
              |                    |                     |
              v                    v                     v
   +------------------+  +------------------+  +-------------------+
   | PlantRepository  |  | WeatherClient    |  | ReverseGeocoding  |
   | (JPA)            |  | (interface)      |  | Client (interface)|
   +------------------+  +--------+---------+  +--------+----------+
              |                    |                     |
              v                    v                     v
   +------------------+  +------------------+  +-------------------+
   | Plant (Entity)   |  | OpenMeteo        |  | OpenStreetMap     |
   | PlantCategory    |  | WeatherClient    |  | ReverseGeocoding  |
   | RequirementLevel |  | + RoundedCache   |  | Client + Rounded  |
   +------------------+  +------------------+  | Cache             |
                                               +-------------------+
```

## Database Schema

Single `plants` table with 16 columns:

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGINT | IDENTITY, PK | Auto-increment primary key |
| `slug` | VARCHAR | NOT NULL, UNIQUE | URL-friendly identifier |
| `common_name` | VARCHAR | NOT NULL | Human-readable name |
| `scientific_name` | VARCHAR | NOT NULL | Latin binomial |
| `image_url` | VARCHAR | nullable | Unsplash image URL |
| `indoor_friendly` | BOOLEAN | NOT NULL | Indoor suitability |
| `watering_recommendation` | VARCHAR | NOT NULL | Watering advice text |
| `light_recommendation` | VARCHAR | NOT NULL | Lighting advice text |
| `ideal_climate` | VARCHAR | nullable | e.g., "Tropical humedo" |
| `ideal_humidity` |VARCHAR | nullable | e.g., "60-80%" |
| `ideal_temperature` | VARCHAR | nullable | e.g., "20-28 C" |
| `toxicidad` | VARCHAR | nullable | Pet toxicity info |
| `category` | VARCHAR | NOT NULL | Enum: SUCULENTA, CACTUS, etc. |
| `light_requirement` | VARCHAR | NOT NULL | Enum: LOW, MEDIUM, HIGH |
| `water_requirement` | VARCHAR | NOT NULL | Enum: LOW, MEDIUM, HIGH |
| `humidity_requirement` | VARCHAR | NOT NULL | Enum: LOW, MEDIUM, HIGH |

30 plants are seeded on startup via `data.sql`.
