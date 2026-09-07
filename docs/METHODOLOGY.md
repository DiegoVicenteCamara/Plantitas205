# Development Methodology

## Table of Contents

- [Git Workflow](#git-workflow)
- [Test-Driven Development](#test-driven-development)
- [Agent-Assisted Development](#agent-assisted-development)
- [Coding Standards](#coding-standards)
- [Architecture Patterns](#architecture-patterns)

---

## Git Workflow

### Branch Strategy

The project uses **feature branches** with **pull request (PR) merges** into `main`.

```
main (production-ready)
  |
  +-- 29-rediseño-visual
  +-- 18-enriquecimiento-de-datos
  +-- 17-personalización-de-uiux-temas
  +-- 15-rediseño-de-navegación-y-búsqueda
  +-- 16-páginas-dinámicas-de-plantas
  +-- copilot/sub-pr-24
  +-- copilot/sub-pr-24-again
  +-- copilot/sub-pr-24-another-one
  ...
```

### Branch Naming Convention

Branches are named using the pattern `{issue-number}-{brief-description}`:

| Branch | Issue | Description |
|--------|-------|-------------|
| `29-rediseño-visual` | #29 | Visual redesign and UI/UX styles |
| `18-enriquecimiento-de-datos` | #18 | Data enrichment and DB/API refactoring |
| `17-personalización-de-uiux-temas` | #17 | UI/UX theme customization (dark mode) |
| `15-rediseño-de-navegación-y-búsqueda` | #15 | Navigation and search redesign |
| `16-páginas-dinámicas-de-plantas` | #16 | Dynamic plant detail pages |

### Commit Conventions

Commits use a mix of Spanish and English. The pattern is:

```
{Imperative verb} {concise description of change}

Examples:
  Add resilience4j retry support and improve logging and error handling
  Diseñar y maquetar componente de Filtros Avanzados
  Crear endpoint dinámico para búsqueda con filtros
  Ampliar modelo de datos para categorías y atributos físicos
  Refactor plant attributes for consistency and clarity
  Implement dark mode toggle and refactor CSS variables for theming
```

### Pull Request Workflow

1. **Create feature branch** from `main`
2. **Develop** with commits following the convention
3. **Open PR** with issue reference
4. **Review** and merge into `main`
5. **Delete** feature branch after merge

Copilot-assisted sub-PRs (`copilot/sub-pr-*`) are used for incremental changes within a feature branch.

### Issue-Driven Development

Each feature or fix is tied to a numbered GitHub issue. Branches, commits, and PRs all reference these issues, providing traceability from requirement to implementation.

---

## Test-Driven Development

### Testing Pyramid

```
           +-----------+
           |    E2E    |  (manual / future)
           +-----------+
          /             \
         | Integration   |  @SpringBootTest + MockMvc
         |               |  @DataJpaTest
         +---------------+
        /                 \
       |     Unit Tests    |  Mockito + JUnit 5
       |                   |  MockRestServiceServer
       +-------------------+
```

### Test Files

| Test File | Package | Type | Count | Framework |
|-----------|---------|------|-------|-----------|
| `PlantitasApplicationTests` | `com.plantitas` | Integration | 1 | `@SpringBootTest` |
| `PlantCareServiceTest` | `com.plantitas.service` | Unit | 22 | `@ExtendWith(MockitoExtension.class)` |
| `OpenMeteoWeatherClientHttpTest` | `com.plantitas.service` | HTTP Mock | 1 | `MockRestServiceServer` |
| `OpenStreetMapReverseGeocodingClientHttpTest` | `com.plantitas.service` | HTTP Mock | 2 | `MockRestServiceServer` |
| `PlantRepositoryTest` | `com.plantitas.repository` | Integration | 4 | `@DataJpaTest` |
| `PlantControllerTest` | `com.plantitas.controller` | Unit | 9 | `@WebMvcTest` + MockMvc |
| `PlantSearchIntegrationTest` | `com.plantitas.controller` | Integration | 2 | `@SpringBootTest` + MockMvc |
| `DtoRecordsTest` | `com.plantitas.dto` | Unit | 4 | JUnit 5 |
| `PlantTest` | `com.plantitas.model` | Unit | 1 | JUnit 5 |

### AAA Pattern (Arrange-Act-Assert)

All tests follow the AAA pattern as enforced by the `test-master` skill:

```java
@Test
void getPlantCare_prioritizesCoordinatesOverCity() {
    // Arrange
    PlantCareRequest request = new PlantCareRequest("1", null, 40.41, -3.70, "verano");

    // Act
    PlantCareResponse response = service.getPlantCare(request);

    // Assert
    assertThat(response.city()).isEqualTo("Madrid");
    assertThat(response.dataQuality()).isEqualTo("full");
}
```

### Test Naming Convention

Test methods use descriptive underscore-separated names that describe the scenario:

```
{method}_{scenario}_{expected}
```

Examples:
- `getPlantCare_prioritizesCoordinatesOverCity`
- `searchPlants_withCategoryFilter_returnsOnlyMatchingPlants`
- `getCurrentWeather_onApiFailure_returnsEmptyWeatherData`
- `resolveCity_fromCoordinates_returnsCityName`

### Mocking Strategy

- **Service layer:** Mockito mocks for `PlantRepository`, `WeatherClient`, `ReverseGeocodingClient`
- **Controller layer:** `@WebMvcTest` with `MockMvc` and mocked service
- **HTTP layer:** `MockRestServiceServer` to verify exact URL params and caching behavior
- **Entity testing:** Reflection-based field setting (Plant has no setters)

### Coverage

- **Tool:** JaCoCo Maven plugin (0.8.12)
- **Goal:** `prepare-agent` + `report` phases
- **Report:** `backend/target/site/jacoco/index.html`
- **Command:** `mvnw verify`

---

## Agent-Assisted Development

### Skills Framework

The project uses a modular skills framework (`.agents/skills/`) to enforce consistent standards across AI-assisted code generation and refactoring.

```
.agents/skills/
├── coding-standards/       # KISS, DRY, YAGNI, style conventions
├── css-styling-expert/     # BEM, Grid, Flexbox, accessibility
├── enhance-prompt/         # UI prompt structuring
├── find-docs/              # Library documentation lookup
├── find-skills/            # Skill discovery and installation
├── tailwindcss-advanced-layouts/  # CSS Grid/Flexbox patterns
├── tailwindcss-animations/        # Animation patterns
├── test-master/            # TDD, unit/integration/E2E testing
├── vercel-react-best-practices/   # React performance optimization
├── web-design-guidelines/         # Web Interface Guidelines audit
└── writing-clearly-and-concisely/ # Technical writing standards
```

### Copilot Instructions

`.github/copilot-instructions.md` provides a project scaffolding checklist and execution guidelines for GitHub Copilot. It enforces:

- Systematic progress tracking via todo lists
- Concise communication (no verbose explanations)
- Current directory (`.`) as working directory
- No unsolicited media or external links
- Feature branches with PR-based merging

### How Agents Are Used

1. **Code generation:** Copilot generates code following the skill-defined standards
2. **Refactoring:** Skills provide rules for performance optimization (Vercel React best practices) and code quality (coding standards)
3. **Testing:** `test-master` skill enforces AAA pattern, descriptive names, and coverage requirements
4. **Documentation:** `writing-clearly-and-concisely` ensures clear technical writing
5. **CSS:** `css-styling-expert` enforces BEM methodology, design tokens, and accessibility

---

## Coding Standards

### Core Principles

| Principle | Description |
|-----------|-------------|
| **KISS** | Keep It Simple, Stupid -- prefer straightforward solutions |
| **DRY** | Don't Repeat Yourself -- extract shared logic |
| **YAGNI** | You Ain't Gonna Need It -- don't add features until required |
| **Readability** | Code is read more than written -- optimize for clarity |

### Java Backend Standards

- **Constructor injection** for all dependencies (no field injection)
- **Records** for DTOs (immutable, auto-generated methods)
- **Enums** for fixed categories and levels
- **Specification pattern** for dynamic queries
- **Strategy pattern** for external API clients
- **Centralized exception handling** via `@RestControllerAdvice`
- **No `@Autowired` on fields** -- constructor injection only

### React Frontend Standards

- **Functional components** only (no class components)
- **Custom hooks** for data fetching and reusable logic
- **CSS custom properties** for theming (no CSS-in-JS libraries)
- **Native fetch API** for HTTP (no axios)
- **BEM-inspired naming** for CSS classes
- **ARIA attributes** for accessibility
- **Keyboard navigation** support in interactive components
- **AbortController** for request cancellation in hooks

### CSS Architecture

- **Design tokens** via CSS custom properties (30+ tokens)
- **Two themes:** light (default) and dark (`.dark-theme`)
- **BEM naming:** `.block__element--modifier`
- **Responsive:** mobile-first media queries
- **No external CSS frameworks** -- custom design system

---

## Architecture Patterns

### Strategy Pattern (WeatherClient / ReverseGeocodingClient)

External API clients are defined as interfaces with concrete implementations:

```
WeatherClient (interface)
  └── OpenMeteoWeatherClient (@Component)

ReverseGeocodingClient (interface)
  └── OpenStreetMapReverseGeocodingClient (@Component)
```

This enables:
- Easy swapping of API providers
- Mock-based testing of the service layer
- Clear separation of concerns

### Specification Pattern (PlantSpecifications)

Dynamic queries are built by composing `Specification<Plant>` predicates:

```java
Specification<Plant> spec = Specification
    .where(PlantSpecifications.commonOrScientificNameContains(query))
    .and(PlantSpecifications.hasCategory(category))
    .and(PlantSpecifications.lightRequirementEquals(level));
```

Each method returns `null` for optional filters, so only active filters are applied.

### Builder Pattern (PlantDetailResponse)

Complex DTOs with many fields use an inner Builder class:

```java
PlantDetailResponse response = new PlantDetailResponse.Builder()
    .id(plant.getId())
    .slug(plant.getSlug())
    .commonName(plant.getCommonName())
    .build();
```

### DTO Pattern

All API boundaries use dedicated DTOs (Java records) to decouple the internal domain model from the external API contract:

```
Plant (entity)  -->  PlantSearchItem (DTO)  -->  JSON response
                -->  PlantDetailResponse (DTO) --> JSON response
PlantCareRequest (DTO)  -->  PlantCareService  -->  PlantCareResponse (DTO)
```

### Resilience Patterns

1. **Retry with exponential backoff:** Resilience4j `@Retry` on external API calls
2. **Caching:** `RoundedCoordinateCache` with configurable TTL and precision
3. **Fallback:** Graceful degradation when APIs are unavailable (data quality indicators)
4. **Circuit breaking:** Available via Resilience4j configuration (currently retry-only)
