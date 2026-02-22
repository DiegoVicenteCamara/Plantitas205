# Plantitas

Aplicación con backend Spring Boot y frontend React para consultar recomendaciones de cuidado de plantas usando un catálogo local en base de datos H2 (memoria).

## Backend (Spring Boot + H2)

1. En una terminal:
   - `cd backend`
   - `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`)
2. API disponible en `http://localhost:8080`.
3. Consola H2 disponible en `http://localhost:8080/h2-console`.
   - JDBC URL: `jdbc:h2:mem:plantitasdb`
   - User: `sa`
   - Password: vacío

## Frontend (React + Vite)

1. En otra terminal:
   - `cd frontend`
   - `npm install`
   - `npm run dev`

## Endpoints principales

- `POST /api/plant-care` para obtener recomendación por planta, ciudad y época.
- `GET /api/plants/search?q=texto` para buscar plantas en el catálogo local.
- `GET /api/plants/suggestions?prefix=Ma` para autocompletado por prefijo.

## Variables de entorno

En frontend puedes definir `VITE_API_BASE_URL`; por defecto es `http://localhost:8080`.
