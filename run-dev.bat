@echo off
echo Starting Hotel Booking Service in development mode...
echo.
echo Building project...
call mvn clean compile
echo.
echo Starting Quarkus in dev mode...
echo The service will be available at http://localhost:8080
echo Swagger UI will be available at http://localhost:8080/q/swagger-ui/
echo.
call mvn quarkus:dev
