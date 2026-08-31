@echo off
echo ============================================================
echo   Starting Pune Bazar Standalone Production Server...
echo ============================================================
echo.
if not exist "target\puna-bazar-app-1.0.0.jar" (
    echo Production JAR not found. Building project...
    call mvnw.cmd clean package -DskipTests
)
echo.
echo Launching Application on http://localhost:8086 ...
echo Database File Location: ./data/punabazardb.mv.db
echo Press Ctrl+C to stop server.
echo.
java -jar target\puna-bazar-app-1.0.0.jar
pause
