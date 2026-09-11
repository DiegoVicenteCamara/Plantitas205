# Class Reference

Full class-by-class reference organized by package. Each class includes purpose, fields, methods, and relationships.

## Table of Contents

- [Package: com.plantitas](#package-complantitas)
- [Package: com.plantitas.model](#package-complantitasmodel)
- [Package: com.plantitas.dto](#package-complantitasdto)
- [Package: com.plantitas.repository](#package-complantitasrepository)
- [Package: com.plantitas.service](#package-complantitasservice)
- [Package: com.plantitas.controller](#package-complantitascontroller)
- [Package: com.plantitas.exception](#package-complantitasexception)
- [Frontend Components](#frontend-components)
- [Frontend Hooks](#frontend-hooks)
- [Frontend Services](#frontend-services)
- [Relationship Diagrams](#relationship-diagrams)

---

## Package: com.plantitas

### PlantitasApplication

**File:** `backend/src/main/java/com/plantitas/PlantitasApplication.java`
**Type:** Class | **Annotations:** `@SpringBootApplication`

Standard Spring Boot entry point. Bootstraps the application context and scans the `com.plantitas` package tree for components.

```
Methods:
  main(String[] args)   -- Launches SpringApplication.run()
```

**Relationships:** Implicitly configures all beans. No direct dependencies.

---

## Package: com.plantitas.model

### Plant

**File:** `backend/src/main/java/com/plantitas/model/Plant.java`
**Type:** JPA Entity | **Table:** `plants`

Core domain object representing a plant in the catalog. Immutable from the application's perspective (getters only, no setters). Fields are set via JPA reflection on entity hydration.

```
Fields:
  +---------------------+------------------+------------------------------------------+
  | Field               | Type             | Column / Notes                           |
  +---------------------+------------------+------------------------------------------+
  | id                  | Long             | @Id @GeneratedValue(IDENTITY)            |
  | slug                | String           | NOT NULL, UNIQUE, URL-friendly           |
  | commonName          | String           | NOT NULL, "common_name"                  |
  | scientificName      | String           | NOT NULL, "scientific_name"              |
  | imageUrl            | String           | nullable, "image_url"                    |
  | indoorFriendly      | boolean          | NOT NULL, "indoor_friendly"              |
  | wateringRecommendation | String        | NOT NULL, "watering_recommendation"      |
  | lightRecommendation | String           | NOT NULL, "light_recommendation"         |
  | idealClimate        | String           | nullable, "ideal_climate"                |
  | idealHumidity       | String           | nullable, "ideal_humidity"               |
  | idealTemperature    | String           | nullable, "ideal_temperature"            |
  | toxicidad           | String           | nullable, "toxicidad"                    |
  | category            | PlantCategory    | @Enumerated(STRING), NOT NULL            |
  | lightRequirement    | RequirementLevel | @Enumerated(STRING), NOT NULL            |
  | waterRequirement    | RequirementLevel | @Enumerated(STRING), NOT NULL            |
  | humidityRequirement | RequirementLevel | @Enumerated(STRING), NOT NULL            |
  +---------------------+------------------+------------------------------------------+

Methods:
  (All getters only -- no setters)
  getId(), getSlug(), getCommonName(), getScientificName(), getImageUrl()
  isIndoorFriendly(), getWateringRecommendation(), getLightRecommendation()
  getIdealClimate(), getIdealHumidity(), getIdealTemperature(), getToxicidad()
  getCategory(), getLightRequirement(), getWaterRequirement(), getHumidityRequirement()
```

**Relationships:**
- Contains `PlantCategory` enum (1 field)
- Contains `RequirementLevel` enum (3 fields)
- Used by `PlantRepository`, `PlantCareService`, `PlantSpecifications`
- Mapped to `PlantDetailResponse` and `PlantSearchItem` DTOs

---

### PlantCategory

**File:** `backend/src/main/java/com/plantitas/model/PlantCategory.java`
**Type:** Enum

Plant classification taxonomy used across the database schema, search filters, and data seeding.

```
Values:
  SUCULENTA | CACTUS | ARBOL | HELECHO | TREPADORA
  FLOR | AROMATICA | INTERIOR | TROPICAL | ORQUIDEA
```

**Relationships:** Referenced by `Plant.category`, `PlantSearchCriteria`, `PlantSpecifications.hasCategory()`

---

### RequirementLevel

**File:** `backend/src/main/java/com/plantitas/model/RequirementLevel.java`
**Type:** Enum

Represents the intensity level for light, water, and humidity requirements.

```
Values:
  LOW | MEDIUM | HIGH
```

**Relationships:** Used by `Plant.lightRequirement`, `Plant.waterRequirement`, `Plant.humidityRequirement`, `PlantSearchCriteria`, `PlantSpecifications`

---

## Package: com.plantitas.dto

All DTOs are Java **records** (immutable data carriers with auto-generated `equals()`, `hashCode()`, `toString()`).

### PlantCareRequest

**File:** `backend/src/main/java/com/plantitas/dto/PlantCareRequest.java`
**Type:** Record

Inbound request body for `POST /api/plant-care`. Accepts either a city name OR latitude/longitude coordinates, plus a season.

```
Fields:
  plantId   | String  | Plant identifier (ID, slug, or name)
  city      | String  | City name (optional if lat/lng provided)
  latitude  | Double  | Geographic latitude (optional if city provided)
  longitude | Double  | Geographic longitude (optional if city provided)
  season    | String  | Season name in Spanish
```

**Relationships:** Consumed by `PlantController.postPlantCare()`, processed by `PlantCareService.getPlantCare()`

---

### PlantCareResponse

**File:** `backend/src/main/java/com/plantitas/dto/PlantCareResponse.java`
**Type:** Record

Outbound response for the plant care endpoint. Contains the recommendation text, weather data, and data quality indicator.

```
Fields:
  plantId          | String  | Plant identifier
  city             | String  | Resolved city name
  season           | String  | Normalized season
  summary          | String  | Short summary text
  recommendation   | String  | Full care recommendation
  indoorFriendly   | boolean | Whether plant suits indoors
  temperature      | Double  | Current temperature (Celsius)
  humidity         | Integer | Current humidity (%)
  altitude         | Double  | Elevation (meters)
  dataQuality      | String  | "full" | "geocode-fallback" | "weather-fallback"
```

**Relationships:** Produced by `PlantCareService.getPlantCare()`, returned by `PlantController.postPlantCare()`

---

### PlantSearchCriteria

**File:** `backend/src/main/java/com/plantitas/dto/PlantSearchCriteria.java`
**Type:** Record

Encapsulates search filter parameters. Defines the search contract for the specification-based query builder.

```
Fields:
  query               | String          | Free-text search term
  category            | PlantCategory   | Category filter
  lightRequirement    | RequirementLevel | Light level filter
  waterRequirement    | RequirementLevel | Water level filter
  humidityRequirement | RequirementLevel | Humidity level filter
```

**Relationships:** References `PlantCategory` and `RequirementLevel` enums. Used internally by `PlantCareService`.

---

### PlantSearchItem

**File:** `backend/src/main/java/com/plantitas/dto/PlantSearchItem.java`
**Type:** Record

Lightweight DTO for search results. Uses `@JsonProperty` for snake_case JSON serialization.

```
Fields:
  id             | Long   | Plant ID
  commonName     | String | @JsonProperty("common_name")
  scientificName | String | @JsonProperty("scientific_name")
  imageUrl       | String | @JsonProperty("image_url")
```

**Relationships:** Produced by `PlantCareService.searchPlants()`, wrapped by `PlantSearchResponse`

---

### PlantSearchResponse

**File:** `backend/src/main/java/com/plantitas/dto/PlantSearchResponse.java`
**Type:** Record

Wrapper response for the search results endpoint.

```
Fields:
  data | List<PlantSearchItem> | Search results
```

**Relationships:** Created by `PlantController.searchPlants()`, wraps `PlantSearchItem` list

---

### PlantDetailResponse

**File:** `backend/src/main/java/com/plantitas/dto/PlantDetailResponse.java`
**Type:** Record with inner Builder class

Detailed plant response for `GET /api/plants/{id}`. Includes all plant attributes. Uses the Builder pattern for construction.

```
Fields:
  id                      | Long     | Plant ID
  slug                    | String   | URL-friendly identifier
  commonName              | String   | @JsonProperty("common_name")
  scientificName          | String   | @JsonProperty("scientific_name")
  imageUrl                | String   | @JsonProperty("image_url")
  indoorFriendly          | boolean  | @JsonProperty("indoor_friendly")
  wateringRecommendation  | String   | @JsonProperty("watering_recommendation")
  lightRecommendation     | String   | @JsonProperty("light_recommendation")
  idealClimate            | String   | @JsonProperty("ideal_climate")
  idealTemperature        | String   | @JsonProperty("ideal_temperature")
  idealHumidity           | String   | @JsonProperty("ideal_humidity")
  toxicidad               | String   | Pet toxicity info

Builder Methods:
  .id(Long) .slug(String) .commonName(String) .scientificName(String)
  .imageUrl(String) .indoorFriendly(boolean) .wateringRecommendation(String)
  .lightRecommendation(String) .idealClimate(String) .idealTemperature(String)
  .idealHumidity(String) .toxicidad(String) .build()
```

**Relationships:** Produced by `PlantCareService.getPlantById()`, consumed by frontend `PlantDetail` component

---

## Package: com.plantitas.repository

### PlantRepository

**File:** `backend/src/main/java/com/plantitas/repository/PlantRepository.java`
**Type:** Interface

Spring Data JPA repository for `Plant` entity. Extends both `JpaRepository` (CRUD) and `JpaSpecificationExecutor` (dynamic queries via Specifications).

```
Extends:
  JpaRepository<Plant, Long>
  JpaSpecificationExecutor<Plant>

Custom Methods:
  findById(Long id)
    -- Find by primary key

  findBySlugIgnoreCase(String slug)
    -- Find plant by slug (case-insensitive)

  findTop10ByCommonNameContainingIgnoreCaseOrderByCommonNameAsc(String name)
    -- Autocomplete: top 10 matches by common name

  findTop10ByCommonNameContainingIgnoreCaseOrScientificNameContainingIgnoreCaseOrderByCommonNameAsc(
      String commonName, String scientificName)
    -- Search by common OR scientific name (top 10)

  findTop10ByCommonNameStartingWithIgnoreCaseOrderByCommonNameAsc(String prefix)
    -- Prefix-based autocomplete

  findByCommonNameContainingIgnoreCaseOrScientificNameContainingIgnoreCase(
      String commonNameQuery, String scientificNameQuery)
    -- Full-text search by name (no limit)
```

**Relationships:** Depends on `Plant` entity. Used by `PlantCareService`. The `JpaSpecificationExecutor` interface enables `PlantSpecifications`-based queries.

---

### PlantSpecifications

**File:** `backend/src/main/java/com/plantitas/repository/PlantSpecifications.java`
**Type:** Final utility class (private constructor)

JPA Specification factory for building dynamic search queries. Implements the Specification pattern for composable, type-safe query criteria. Each method returns a `Specification<Plant>` or `null` (for optional filters).

```
Static Methods:
  commonOrScientificNameContains(String query)
    -- LIKE search on commonName AND scientificName (case-insensitive)
    -- Returns null if query is blank

  hasCategory(PlantCategory category)
    -- Exact match on category field
    -- Returns null if null

  lightRequirementEquals(RequirementLevel level)
    -- Exact match on lightRequirement
    -- Returns null if null

  waterRequirementEquals(RequirementLevel level)
    -- Exact match on waterRequirement
    -- Returns null if null

  humidityRequirementEquals(RequirementLevel level)
    -- Exact match on humidityRequirement
    -- Returns null if null

Private Helpers:
  requirementEquals(String fieldName, RequirementLevel level)
    -- Generic requirement spec builder
```

**Relationships:** Used by `PlantCareService.buildSearchSpecification()`. Works with `Plant` entity fields. References `PlantCategory` and `RequirementLevel`.

---

## Package: com.plantitas.service

### WeatherClient (Interface)

**File:** `backend/src/main/java/com/plantitas/service/WeatherClient.java`
**Type:** Interface (Strategy pattern)

Abstraction for weather data retrieval. Decouples the service layer from the specific weather API provider.

```
Methods:
  WeatherData getCurrentWeather(double latitude, double longitude)
```

**Relationships:** Implemented by `OpenMeteoWeatherClient`. Consumed by `PlantCareService`.

---

### ReverseGeocodingClient (Interface)

**File:** `backend/src/main/java/com/plantitas/service/ReverseGeocodingClient.java`
**Type:** Interface (Strategy pattern)

Abstraction for reverse geocoding (coordinates to city name).

```
Methods:
  String resolveCity(double latitude, double longitude)
```

**Relationships:** Implemented by `OpenStreetMapReverseGeocodingClient`. Consumed by `PlantCareService`.

---

### WeatherData

**File:** `backend/src/main/java/com/plantitas/service/WeatherData.java`
**Type:** Record

Immutable value object representing weather conditions at a location.

```
Fields:
  temperature   | Double  | Temperature in Celsius
  humidity      | Integer | Relative humidity (%)
  altitude      | Double  | Elevation in meters
  precipitation | Double  | Precipitation amount
  weatherCode   | Integer | WMO weather code

Static Methods:
  empty()   -- Returns WeatherData with all null fields (fallback)
```

**Relationships:** Produced by `WeatherClient` implementations. Consumed by `PlantCareService`. Returned as fallback by `OpenMeteoWeatherClient.fallbackWeather()`.

---

### OpenMeteoWeatherClient

**File:** `backend/src/main/java/com/plantitas/service/OpenMeteoWeatherClient.java`
**Type:** Class | **Annotations:** `@Component` | **Implements:** `WeatherClient`

HTTP client that fetches current weather from the Open-Meteo API. Implements retry with Resilience4j and coordinate-based caching.

```
Fields:
  restClient | RestClient | Spring RestClient for HTTP calls
  cache      | RoundedCoordinateCache<WeatherData> | TTL-based cache

Constructor:
  Injects RestClient.Builder, reads config from application.properties

Methods:
  getCurrentWeather(double lat, double lng)
    @Retry(name="weatherClient", fallbackMethod="fallbackWeather")
    -- Delegates to cache.getOrCompute()

  fallbackWeather(double lat, double lng, Exception ex)
    -- Returns WeatherData.empty() on retry exhaustion

  fetchCurrentWeather(double lat, double lng)
    -- Actual HTTP call to Open-Meteo API
    -- Parses OpenMeteoResponse

Inner Records:
  OpenMeteoResponse(Double elevation, CurrentWeather current)
  CurrentWeather(Double temperature_2m, Integer relative_humidity_2m,
                 Double precipitation, Integer weather_code)
```

**Relationships:** Implements `WeatherClient`. Uses `RoundedCoordinateCache<WeatherData>`. Configured via `weather.api.*` properties.

---

### OpenStreetMapReverseGeocodingClient

**File:** `backend/src/main/java/com/plantitas/service/OpenStreetMapReverseGeocodingClient.java`
**Type:** Class | **Annotations:** `@Component` | **Implements:** `ReverseGeocodingClient`

HTTP client that resolves coordinates to city names using the Nominatim API. Implements retry with Resilience4j and coordinate-based caching.

```
Fields:
  restClient | RestClient | With User-Agent: PlantitasApp/1.0 header
  cache      | RoundedCoordinateCache<String> | TTL-based cache

Constructor:
  Injects RestClient.Builder, reads config from geocoding.api.* properties

Methods:
  resolveCity(double lat, double lng)
    @Retry(name="geocodingClient", fallbackMethod="fallbackCity")
    -- Delegates to cache.getOrCompute()

  fallbackCity(double lat, double lng, Exception ex)
    -- Returns null on retry exhaustion

  fetchCity(double lat, double lng)
    -- Actual HTTP call to Nominatim API
    -- Parses ReverseGeocodingResponse

  resolveCityFromAddress(Address address)
    -- Priority chain: city -> town -> village -> municipality

  hasText(String value)
    -- Null/blank check helper

Inner Records:
  ReverseGeocodingResponse(Address address)
  Address(String city, String town, String village, String municipality)
```

**Relationships:** Implements `ReverseGeocodingClient`. Uses `RoundedCoordinateCache<String>`.

---

### RoundedCoordinateCache\<T\>

**File:** `backend/src/main/java/com/plantitas/service/RoundedCoordinateCache.java`
**Type:** Generic class (package-private)

In-memory TTL cache that rounds lat/lng to a configurable precision before keying. Prevents redundant API calls for nearby coordinates. Thread-safe via `ConcurrentHashMap`.

```
Type Parameters:
  T -- Cached value type

Fields:
  ttlMillis     | long              | Time-to-live in milliseconds
  precision     | int               | Decimal places for coordinate rounding
  cacheEntries  | ConcurrentHashMap | Map<String, CacheEntry<T>>

Methods:
  getOrCompute(double lat, double lng, Supplier<T> compute)
    -- Returns cached value if valid, otherwise computes, caches, purges expired

  purgeExpiredEntries()
    -- Removes stale entries on each access

  buildKey(double lat, double lng)
    -- Rounds coordinates and formats as "lat|lng" string

  round(double value)
    -- Rounds to `precision` decimal places

Inner Record:
  CacheEntry<T>(T value, long expiresAtMillis)
```

**Relationships:** Used by `OpenMeteoWeatherClient` (for `WeatherData`) and `OpenStreetMapReverseGeocodingClient` (for `String` city names).

---

### PlantCareService

**File:** `backend/src/main/java/com/plantitas/service/PlantCareService.java`
**Type:** Class | **Annotations:** `@Service`

Core business logic service. Orchestrates plant lookup, weather fetching, reverse geocoding, seasonal advice generation, and plant search/suggestions.

```
Fields:
  plantRepository        | PlantRepository        | Data access
  weatherClient          | WeatherClient          | Weather data
  reverseGeocodingClient | ReverseGeocodingClient | City resolution

Constructor:
  PlantCareService(PlantRepository, WeatherClient, ReverseGeocodingClient)

Public Methods:
  getPlantCare(PlantCareRequest request) -> PlantCareResponse
    Main workflow: resolve plant, resolve location, fetch weather,
    determine data quality, build recommendation text

  searchPlants(String query, String category, String light,
               String water, String humidity) -> PlantSearchResponse
    Builds JPA Specification from filter params, executes query,
    maps results to PlantSearchItem DTOs

  searchPlants(String query) -> PlantSearchResponse
    Convenience overload (no filters)

  suggestPlantNames(String prefix) -> List<String>
    Returns distinct common names starting with prefix

  getPlantById(Long id) -> PlantDetailResponse
    Returns plant details or throws ResourceNotFoundException

Private Methods:
  resolveWeatherData(request) -> WeatherResolution
    Fetches weather or returns empty WeatherData with fallback flag

  resolveLocationForClimate(request) -> LocationResolution
    Reverse geocodes coordinates or uses city name

  determineDataQuality(boolean geocodeFallback, boolean weatherFallback) -> String
    Returns "full", "geocode-fallback", or "weather-fallback"

  resolvePlant(String plantId) -> Plant
    Tries: numeric ID -> findBySlug -> findByCommonName/ScientificName

  findPlantByIdOrSlugOrName(String) -> Optional<Plant>
    Chain of Optional lookups

  normalizeSeason(String) -> String
    Maps Spanish season names: "otono"/"otoño" -> "otoño", etc.

  normalizeText(String, String defaultValue) -> String
    Trims and defaults

  parseCategory(String) -> PlantCategory
    Converts to PlantCategory enum

  parseRequirementLevel(String, String) -> RequirementLevel
    Converts to RequirementLevel enum

  buildRecommendation(Plant, String season, WeatherData) -> String
    Concatenates: watering + light + seasonal tip + weather tip

  buildWeatherTip(WeatherData) -> String
    Context-specific advice for high/low temp, high/low humidity, precipitation

Inner Records:
  LocationResolution(String city, boolean geocodeFallback)
  WeatherResolution(WeatherData weatherData, boolean weatherFallback)
```

**Relationships:** Depends on `PlantRepository`, `WeatherClient`, `ReverseGeocodingClient`. Consumed by `PlantController`. Uses `PlantSpecifications`, `PlantCategory`, `RequirementLevel`, `WeatherData`, and all DTOs.

---

## Package: com.plantitas.controller

### PlantController

**File:** `backend/src/main/java/com/plantitas/controller/PlantController.java`
**Type:** Class | **Annotations:** `@RestController`, `@RequestMapping("/api")`, `@CrossOrigin`

REST API controller exposing all HTTP endpoints. CORS configured to allow the Vite dev server.

```
Fields:
  plantCareService | PlantCareService | (final, constructor-injected)

Endpoints:
  POST /api/plant-care
    getPlantCare(@RequestBody PlantCareRequest request)
    -- Validates lat/lng pair, delegates to service
    -- Returns: PlantCareResponse

  GET /api/plants/search
    searchPlants(@RequestParam q, category, light, water, humidity)
    -- All params optional
    -- Returns: PlantSearchResponse

  GET /api/plants/suggestions
    getPlantSuggestions(@RequestParam prefix)
    -- Returns: List<String>

  GET /api/plants/{id}
    getPlantById(@PathVariable Long id)
    -- Returns: PlantDetailResponse

Private Methods:
  validateLocationRequest(PlantCareRequest)
    -- Throws IllegalArgumentException if only one of lat/lng is provided
```

**Relationships:** Depends on `PlantCareService`. Returns DTOs. CORS allows `http://localhost:5173`.

---

### GlobalExceptionHandler

**File:** `backend/src/main/java/com/plantitas/controller/GlobalExceptionHandler.java`
**Type:** Class | **Annotations:** `@RestControllerAdvice`

Centralized exception handling for all REST controllers. Converts exceptions to consistent JSON error responses.

```
Handlers:
  handleNotFound(ResourceNotFoundException ex) -> ErrorResponse
    -- 404 NOT_FOUND

  handleIllegalArgument(IllegalArgumentException ex) -> ErrorResponse
    -- 400 BAD_REQUEST

  handleMissingParam(MissingServletRequestParameterException ex) -> ErrorResponse
    -- 400 BAD_REQUEST

  handleTypeMismatch(MethodArgumentTypeMismatchException ex) -> ErrorResponse
    -- 400 BAD_REQUEST

  handleUnexpected(Exception ex) -> ErrorResponse
    -- 500 INTERNAL_ERROR (catch-all)

Inner Record:
  ErrorResponse(String error, String message)
```

**Relationships:** Handles exceptions from `PlantController` and `PlantCareService`.

---

## Package: com.plantitas.exception

### ResourceNotFoundException

**File:** `backend/src/main/java/com/plantitas/exception/ResourceNotFoundException.java`
**Type:** Class | **Extends:** `RuntimeException`

Custom exception thrown when a requested resource (plant) is not found.

```
Constructor:
  ResourceNotFoundException(String message)
```

**Relationships:** Thrown by `PlantCareService.resolvePlant()` and `getPlantById()`. Caught by `GlobalExceptionHandler`.

---

## Frontend Components

### App

**File:** `frontend/src/App.jsx`

Root component. Manages dark mode state (persisted to `localStorage`), defines routes, and renders the app shell with header.

```
State:
  isDarkMode | boolean | Persisted to localStorage ("plantitas-theme")

Routes:
  /             -> Home
  /planta/:id   -> PlantPage

Features:
  - Dark mode toggle button in header
  - Toggles "dark-theme" class on document.documentElement
```

---

### Home

**File:** `frontend/src/pages/Home.jsx`

Main page with plant search, map selection, season picker, and care recommendation results.

```
State:
  plantId          | String  | Selected plant ID
  location         | Object  | { lat, lng } from map
  season           | String  | Selected season
  result           | Object  | API response
  loading          | boolean | Request in progress
  error            | String  | Error message
  advancedFilters  | Object  | { category, light, water, humidity }

Hooks Used:
  usePlantSearch(advancedFilters) -- debounced search with keyboard nav

Features:
  - Advanced filters panel (collapsible)
  - Plant search combobox with autocomplete
  - Leaflet map for location selection
  - Season dropdown (primavera/verano/otoño/invierno)
  - Results display with data quality indicator
  - Navigation to /planta/{id} on result click
```

---

### PlantDetail

**File:** `frontend/src/components/PlantDetail.jsx`

Full plant detail card with image, care badges, and climate recommendation.

```
Hooks Used:
  usePlantDetail(id) -- fetches plant by ID from URL params

Features:
  - Plant image (or placeholder emoji)
  - Common name, scientific name, indoor-friendly status
  - Care badges: watering, light, temperature
  - Climate recommendation text (built from ideal conditions)
  - Ideal conditions sidebar (climate, temp, humidity, toxicity)
  - Back button to Home
```

---

### AdvancedFilters

**File:** `frontend/src/components/AdvancedFilters.jsx`

Collapsible filter panel with category chips and requirement level dropdowns.

```
Props:
  value     | Object  | Current filter state
  onChange  | Function | Partial update callback
  idPrefix  | String  | HTML ID prefix for ARIA

Features:
  - Auto-collapses on mobile (<900px)
  - Category chip buttons (radio group): 11 categories
  - Three dropdown selects: light, water, humidity levels
  - ARIA: radiogroup, radio, aria-checked, aria-expanded
```

---

### MapSelector

**File:** `frontend/src/components/MapSelector.jsx`

Leaflet map with click-to-select location. Default center: Spain (40.4168, -3.7038).

```
Props:
  value   | Object  | Selected { lat, lng }
  onChange | Function | Location selection callback

Features:
  - Click to place marker
  - Custom Leaflet marker icon
  - Displays selected coordinates
```

---

### CitySelector

**File:** `frontend/src/components/CitySelector.jsx`

Simple text input for city name. Currently not used in the active UI (replaced by MapSelector).

```
Props:
  value   | String  | Current city name
  onChange | Function | City name change callback
```

---

## Frontend Hooks

### usePlantSearch

**File:** `frontend/src/hooks/usePlantSearch.js`

Debounced search hook with keyboard navigation for the autocomplete combobox.

```
Input:
  filters | Object | { category, light, water, humidity }

State:
  query       | String  | Search input text
  results     | Array   | Search results from API
  loading     | boolean | Search in progress
  error       | String  | Error message
  activeIndex | number  | Currently highlighted suggestion

Features:
  - 300ms debounce on search
  - Cancellation via AbortController (cancelledRef)
  - Keyboard: ArrowDown/ArrowUp/Enter/Escape navigation

Returns:
  { query, setQuery, results, loading, error, activeIndex, setActiveIndex, handleKeyDown }
```

---

### usePlantDetail

**File:** `frontend/src/hooks/usePlantDetail.js`

Single plant fetch hook with loading/error states.

```
Input:
  id | String | Plant ID from URL params

State:
  plant   | Object  | Plant data
  loading | boolean | Fetch in progress
  error   | String  | Error message (or "PLANT_NOT_FOUND" for 404)

Features:
  - Fetches on mount and when id changes
  - Cancellation on unmount via AbortController

Returns:
  { plant, loading, error }
```

---

## Frontend Services

### plantCareService

**File:** `frontend/src/services/plantCareService.js`

API client layer. All functions use the native `fetch` API.

```
Base URL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"

Functions:
  fetchPlantCare(payload)
    POST /api/plant-care with JSON body
    Returns: PlantCareResponse

  searchPlants(query, filters)
    GET /api/plants/search?q=...&category=...&light=...&water=...&humidity=...
    Returns: PlantSearchResponse

  fetchPlantSuggestions(prefix)
    GET /api/plants/suggestions?prefix=...
    Returns: String[]

  fetchPlantById(id)
    GET /api/plants/{id}
    Returns: PlantDetailResponse
    Throws: "PLANT_NOT_FOUND" on 404

Helpers:
  resolveErrorMessage(response, fallback)
    Extracts error message from JSON response body

  handleResponse(response, fallback)
    Checks response.ok, throws on error, returns response.json()
```

---

## Relationship Diagrams

### Backend Class Diagram

```
+------------------+       +-------------------+
| PlantController  |------>| PlantCareService  |
+------------------+       +--------+----------+
                                    |
                  +-----------------+-----------------+
                  |                 |                  |
                  v                 v                  v
        +------------------+ +----------------+ +-------------------+
        | PlantRepository  | | WeatherClient  | | ReverseGeocoding  |
        | (interface)      | | (interface)    | | Client (interface)|
        +--------+---------+ +-------+--------+ +--------+----------+
                 |                   |                     |
                 v                   v                     v
        +------------------+ +----------------+ +-------------------+
        | Plant (Entity)   | | OpenMeteo      | | OpenStreetMap     |
        |                  | | WeatherClient  | | ReverseGeocoding  |
        | Fields:          | |                | | Client            |
        |  - id            | | Fields:        | |                   |
        |  - slug          | |  - restClient  | | Fields:           |
        |  - commonName    | |  - cache       | |  - restClient     |
        |  - scientificName| +-------+--------+ |  - cache          |
        |  - category      |         |          +--------+----------+
        |  - lightReq      |         v                   |
        |  - waterReq      | +----------------+          |
        |  - humidityReq   | | RoundedCoord   |<---------+
        |  - ... (16 cols) | | Cache<T>       |
        +------------------+ +----------------+

+------------------+       +-------------------+
| GlobalException  |<------| ResourceNotFound  |
| Handler          |       | Exception         |
+------------------+       +-------------------+
```

### Frontend Component Tree

```
App
├── Header
│   └── Dark Mode Toggle
├── Routes
│   ├── Home
│   │   ├── AdvancedFilters
│   │   │   ├── Category Chips (radio group)
│   │   │   └── FilterSelect (x3: light, water, humidity)
│   │   ├── usePlantSearch (hook)
│   │   │   └── Suggestions List
│   │   ├── MapSelector
│   │   │   └── MapClickHandler
│   │   ├── Season Dropdown
│   │   └── Results Display
│   │
│   └── PlantPage -> PlantDetail
│       ├── usePlantDetail (hook)
│       ├── Plant Image
│       ├── Care Badges
│       ├── Climate Recommendation
│       └── Ideal Conditions Sidebar
```
