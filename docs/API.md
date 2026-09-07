# API Reference

REST API endpoints exposed by the Spring Boot backend at `http://localhost:8080`.

## Base URL

```
http://localhost:8080/api
```

## CORS Configuration

```
Allowed Origin: http://localhost:5173 (Vite dev server)
```

---

## Endpoints

### POST /api/plant-care

Get a personalized plant care recommendation based on plant, location, and season.

**Request Body:**

```json
{
  "plantId": "monstera",
  "city": "Madrid",
  "latitude": 40.4168,
  "longitude": -3.7038,
  "season": "verano"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `plantId` | String | Yes | Plant ID (numeric), slug, or name |
| `city` | String | No* | City name (*required if lat/lng not provided) |
| `latitude` | Double | No* | Geographic latitude (*required if city not provided) |
| `longitude` | Double | No* | Geographic longitude (*required with latitude) |
| `season` | String | Yes | Season in Spanish: `primavera`, `verano`, `otoño`, `invierno` |

**Validation:**
- If only one of `latitude`/`longitude` is provided, returns `400 Bad Request`
- If neither `city` nor `latitude`/`longitude` is provided, weather data will be unavailable (fallback)

**Response (200 OK):**

```json
{
  "plantId": "monstera",
  "city": "Madrid",
  "season": "verano",
  "summary": "Cuidado de Monstera deliciosa en Madrid durante verano",
  "recommendation": "Riego: Mantener el sustrato ligeramente húmedo. Luz: Indirecta brillante. Verano: En verano, aumenta la frecuencia de riego y coloca en luz indirecta brillante. Temperatura actual: 28C. Humedad: 45%. Recomendacion: Aumenta la humedad ambiental con un humidificador o bandeja de guijarros.",
  "indoorFriendly": true,
  "temperature": 28.0,
  "humidity": 45,
  "altitude": 667.0,
  "dataQuality": "full"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `plantId` | String | Echoed plant identifier |
| `city` | String | Resolved city name |
| `season` | String | Normalized season |
| `summary` | String | Short summary |
| `recommendation` | String | Full care recommendation text |
| `indoorFriendly` | boolean | Indoor suitability |
| `temperature` | Double | Current temperature (Celsius), null if unavailable |
| `humidity` | Integer | Current humidity (%), null if unavailable |
| `altitude` | Double | Elevation (meters), null if unavailable |
| `dataQuality` | String | Data completeness indicator |

**Data Quality Values:**

| Value | Meaning |
|-------|---------|
| `full` | All data sources available (plant + weather + geocoding) |
| `geocode-fallback` | Weather available, geocoding failed |
| `weather-fallback` | Geocoding available, weather API failed |

**Error Responses:**

| Status | Condition | Body |
|--------|-----------|------|
| `400` | Only one of lat/lng provided | `{ "error": "Bad Request", "message": "..." }` |
| `400` | Missing required params | `{ "error": "Bad Request", "message": "..." }` |
| `404` | Plant not found | `{ "error": "Not Found", "message": "..." }` |
| `500` | Unexpected error | `{ "error": "Internal Error", "message": "..." }` |

---

### GET /api/plants/search

Search plants by name with optional filters.

**Query Parameters:**

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `q` | String | No | Free-text search (common or scientific name) |
| `category` | String | No | Filter by category (see values below) |
| `light` | String | No | Filter by light requirement |
| `water` | String | No | Filter by water requirement |
| `humidity` | String | No | Filter by humidity requirement |

**Category Values:**

```
SUCULENTA | CACTUS | ARBOL | HELECHO | TREPADORA | FLOR | AROMATICA | INTERIOR | TROPICAL | ORQUIDEA
```

**Requirement Level Values:**

```
LOW | MEDIUM | HIGH
```

**Example Request:**

```
GET /api/plants/search?q=mon&category=TROPICAL&light=HIGH
```

**Response (200 OK):**

```json
{
  "data": [
    {
      "id": 1,
      "common_name": "Monstera",
      "scientific_name": "Monstera deliciosa",
      "image_url": "https://images.unsplash.com/photo-..."
    }
  ]
}
```

---

### GET /api/plants/suggestions

Get autocomplete suggestions for plant names by prefix.

**Query Parameters:**

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `prefix` | String | Yes | Name prefix to match |

**Example Request:**

```
GET /api/plants/suggestions?prefix=Ma
```

**Response (200 OK):**

```json
["Magenta", "Magnolia", "Malva", "Manzanilla", "Margherita", "Maranta", "Menta"]
```

Returns up to 10 distinct common names that start with the given prefix (case-insensitive).

---

### GET /api/plants/{id}

Get full details for a single plant.

**Path Parameters:**

| Param | Type | Description |
|-------|------|-------------|
| `id` | Long | Plant ID (numeric) |

**Example Request:**

```
GET /api/plants/1
```

**Response (200 OK):**

```json
{
  "id": 1,
  "slug": "monstera",
  "common_name": "Monstera",
  "scientific_name": "Monstera deliciosa",
  "image_url": "https://images.unsplash.com/photo-...",
  "indoor_friendly": true,
  "watering_recommendation": "Mantener el sustrato ligeramente húmedo...",
  "light_recommendation": "Indirecta brillante...",
  "ideal_climate": "Tropical humedo",
  "ideal_temperature": "20-28 C",
  "ideal_humidity": "60-80%",
  "toxicidad": "Toxica para mascotas"
}
```

**Error Responses:**

| Status | Condition | Body |
|--------|-----------|------|
| `404` | Plant not found | `{ "error": "Not Found", "message": "..." }` |

---

## Error Response Format

All errors follow a consistent JSON format:

```json
{
  "error": "Error Type",
  "message": "Human-readable description of what went wrong"
}
```

**Error Types:**

| Type | Status | Description |
|------|--------|-------------|
| `Not Found` | 404 | Requested resource does not exist |
| `Bad Request` | 400 | Invalid request parameters |
| `Internal Error` | 500 | Unexpected server error |

---

## H2 Console

For development, the H2 database console is available at:

```
http://localhost:8080/h2-console
```

| Field | Value |
|-------|-------|
| JDBC URL | `jdbc:h2:mem:plantitasdb` |
| User Name | `sa` |
| Password | *(empty)* |
