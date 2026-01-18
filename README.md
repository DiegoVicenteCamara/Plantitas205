# Plantitas

Proyecto base con Spring Boot (backend) y React (frontend) para recomendaciones de cuidado de plantas según ciudad y clima.

## Backend (Spring Boot)

1. En una terminal ubicada en la carpeta del proyecto:
   - Ir a la carpeta `backend`.
   - Ejecutar el proyecto con Maven.

## Frontend (React + Vite)

1. En otra terminal ubicada en la carpeta del proyecto:
   - Ir a la carpeta `frontend`.
   - Instalar dependencias.
   - Iniciar el servidor de desarrollo.

## Variables de entorno

En el frontend puedes definir `VITE_API_BASE_URL` para apuntar al backend. Por defecto usa `http://localhost:8080`.

## Próximos pasos

- Reemplazar los placeholders por llamadas reales a las APIs de cuidados de plantas y meteorología.
- Añadir lógica para evaluar si la planta está mejor en interior o exterior.
